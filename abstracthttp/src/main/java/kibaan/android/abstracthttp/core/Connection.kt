package kibaan.android.abstracthttp.core

import android.os.Handler
import kibaan.android.abstracthttp.defaultimpl.DefaultHTTPConnector
import kibaan.android.abstracthttp.defaultimpl.DefaultURLEncoder
import kibaan.android.abstracthttp.entity.ConnectionError
import kibaan.android.abstracthttp.entity.Request
import kibaan.android.abstracthttp.entity.Response
import kibaan.android.abstracthttp.entity.URLQuery
import kibaan.android.abstracthttp.enumtype.ConnectionErrorType
import kibaan.android.abstracthttp.enumtype.EventChain
import java.net.URL

/**
 * HTTP通信のライフサイクル
 * 通信に必要な各種モジュールを取りまとめ、通信処理の実行と各種コールバックやリスナーの呼び出しを行う
 *
 * The lifecycle of a HTTP connection.
 */
open class Connection<ResponseModel: Any> {

    var requestSpec: RequestSpec
    var parseResponse: (Response) -> ResponseModel
    var isValidResponse: (Response) -> Boolean

    var listeners: MutableList<ConnectionListener> = mutableListOf()
    var responseListeners: MutableList<ConnectionResponseListener> = mutableListOf()
    var errorListeners: MutableList<ConnectionErrorListener> = mutableListOf()

    var connector: HTTPConnector = DefaultImplementation.shared.httpConnector()
    var urlEncoder: URLEncoder = DefaultImplementation.shared.urlEncoder()

    /**
     * キャンセルされたかどうか。このフラグが `true` だと通信終了してもコールバックが呼ばれない
     * Cancel後の再通信は想定しない
     */
    var isCancelled = false
        private set

    /**
     * コールバックをメインスレッドで呼び出すか
     */
    var callbackInMainThread = true

    var onSuccess: ((ResponseModel) -> Unit)? = null
    var onError: ((ConnectionError, Response?, ResponseModel?) -> Unit)? = null

    /**
     * 終了コールバック
     * (response: Response?, responseModel: Any?, error: ConnectionError?) -> Void
     */
    var onEnd: ((Response?, Any?, ConnectionError?) -> Unit)? = null

    var latestRequest: Request? = null
        private set

    var holder = ConnectionHolder.shared

    val handler = Handler()

    constructor(requestSpec: RequestSpec, responseSpec: ResponseSpec<ResponseModel>, onSuccess: ((ResponseModel) -> Unit)? = null) {
        this.requestSpec = requestSpec
        this.parseResponse = responseSpec::parseResponse
        this.isValidResponse = responseSpec::isValidResponse
        this.onSuccess = onSuccess
    }

    constructor(connectionSpec: ConnectionSpec<ResponseModel>, onSuccess: ((ResponseModel) -> Unit)? = null) {
        this.requestSpec = connectionSpec
        this.parseResponse = connectionSpec::parseResponse
        this.isValidResponse = connectionSpec::isValidResponse
        this.onSuccess = onSuccess
    }

    fun addListener(listener: ConnectionListener): Connection<*> {
        listeners.add(listener)
        return this
    }

    fun addResponseListener(listener: ConnectionResponseListener): Connection<*> {
        responseListeners.add(listener)
        return this
    }

    fun addErrorListener(listener: ConnectionErrorListener): Connection<*> {
        errorListeners.add(listener)
        return this
    }

    fun removeListener(listener: ConnectionListener) {
        listeners.removeAll { it === listener }
    }

    fun removeResponseListener(listener: ConnectionResponseListener) {
        responseListeners.removeAll { it === listener }
    }

    fun removeErrorListener(listener: ConnectionErrorListener) {
        errorListeners.removeAll { it === listener }
    }

    fun setOnError(onError: (ConnectionError, Response?, ResponseModel?) -> Unit): Connection<*> {
        this.onError = onError
        return this
    }

    fun setOnEnd(onEnd: (Response?, Any?, ConnectionError?) -> Unit): Connection<*> {
        this.onEnd = onEnd
        return this
    }

    /**
     * 処理を開始する
     */
    fun start() {
        connect()
    }

    /**
     * 通信処理を開始する
     *
     * @param shouldNotify 通信開始のコールバックを呼び出す場合は `true`。リスナーに通知せず再通信したい場合に `false` を指定する。
     */
    private fun connect(request: Request? = null, shouldNotify: Boolean = true) {
        val url = makeURL(baseURL = requestSpec.url, query = requestSpec.urlQuery, encoder = urlEncoder)
        if (url == null) {
            handleError(ConnectionErrorType.invalidURL)
            return
        }

        // リクエスト作成
        val request = request ?: Request(
            url = url,
            method = requestSpec.httpMethod,
            body = requestSpec.makePostData(),
            headers = requestSpec.headers.toMutableMap()
        )
        if (shouldNotify) {
            listeners.forEach {
                it.onStart(connection = this, request = request)
            }
        }

        // このインスタンスが通信完了まで開放されないよう保持する必要がある
        holder.add(connection = this)

        print("[${requestSpec.httpMethod.stringValue}] $url")

        // 通信する
        connector.execute(request = request, complete = { response, error ->
            this.complete(response = response, error = error)
            this.holder.remove(connection = this)
        })

        latestRequest = request
    }

    /**
     * 通信完了時の処理
     */
    private fun complete(response: Response?, error: Error?) {
        if (isCancelled) {
            return
        }

        val response = response
        if (response == null || error != null) {
            onNetworkError(error = error)
            return
        }

        var listenerValidationResult = true
        responseListeners.forEach {
            listenerValidationResult = listenerValidationResult && it.onReceived(connection = this, response = response)
        }

        if (!listenerValidationResult || !isValidResponse(response)) {
            onResponseError(response = response)
            return
        }

        handleResponse(response = response)
    }

    open fun handleResponse(response: Response) {

        val responseModel: ResponseModel

        try {
            responseModel = parseResponse(response)
        } catch (e: Exception) {
            onParseError(response = response)
            return
        }

        var listenerValidationResult = true
        responseListeners.forEach {
            listenerValidationResult = listenerValidationResult && it.onReceivedModel(connection = this, responseModel = responseModel)
        }
        if (!listenerValidationResult) {
            onValidationError(response = response, responseModel = responseModel)
            return
        }

        callback {
            this.onSuccess?.invoke(responseModel)
            this.responseListeners.forEach {
                it.afterSuccess(connection = this, responseModel = responseModel)
            }
            this.end(response = response, responseModel = responseModel, error = null)
        }
    }

    fun onNetworkError(error: Error?) {
        controlError(callListener = {
            it.onNetworkError(connection = this, error = error)
        }, callError = {
            this.handleError(ConnectionErrorType.network, error = error)
        })
    }

    fun onResponseError(response: Response) {
        controlError(callListener = {
            it.onResponseError(connection = this, response = response)
        }, callError = {
            this.handleError(ConnectionErrorType.invalidResponse, response = response)
        })
    }

    fun onParseError(response: Response) {
        controlError(callListener = {
            it.onParseError(connection = this, response = response)
        }, callError = {
            this.handleError(ConnectionErrorType.parse, response = response)
        })
    }

    fun onValidationError(response: Response, responseModel: ResponseModel) {
        controlError(callListener = {
            it.onValidationError(connection = this, response = response, responseModel = responseModel)
        }, callError = {
            this.handleError(ConnectionErrorType.validation, response = response, responseModel = responseModel)
        })
    }

    /**
     * エラーを処理する
     */
    private fun controlError(callListener: (ConnectionErrorListener) -> EventChain, callError: () -> Unit) {
        var stopNext = false

        for (i in errorListeners.indices) {
            val result = callListener(errorListeners[i])
            if (result == EventChain.stopImmediately) {
                return
            }
            if (result == EventChain.stop) {
                stopNext = true
            }
        }

        if (stopNext) {
            return
        }

        callback {
            callError()
        }
    }

    /**
     * エラーを処理する
     */
    open fun handleError(
        type: ConnectionErrorType,
        error: Error? = null,
        response: Response? = null,
        responseModel: ResponseModel? = null
    ) {
        val message = error?.toString() ?: ""
        // TODO Releaseの場合に表示されないようにする
        print("[ConnectionError] Type= ${type.description}, NativeMessage=${message}")

        val connectionError = ConnectionError(type = type, nativeError = error)
        onError?.invoke(connectionError, response, responseModel)

        errorListeners.forEach {
            it.afterError(
                connection = this,
                response = response,
                responseModel = responseModel,
                error = connectionError
            )
        }

        end(response = response, responseModel = responseModel, error = connectionError)
    }

    /**
     * 通信を再実行する
     */
    open fun restart(cloneRequest: Boolean, shouldNotify: Boolean) {
        val request = if (cloneRequest) latestRequest else null
        connect(request = request, shouldNotify = shouldNotify)
    }

    /**
     * 通信をキャンセルする
     */
    open fun cancel() {
        // TODO 既に通信コールバックが走っている場合何もしない。通信コールバック内でキャンセルした場合に、onEndが二重で呼ばれないようにする必要がある
        isCancelled = true
        connector.cancel()

        errorListeners.forEach { it.onCanceled(connection = this) }
        val error = ConnectionError(type = ConnectionErrorType.canceled, nativeError = null)
        end(response = null, responseModel = null, error = error)
    }

    private fun end(response: Response?, responseModel: Any?, error: ConnectionError?) {
        listeners.forEach { it.onEnd(connection = this, response = response, responseModel = responseModel, error = error) }
        onEnd?.invoke(response, responseModel, error)
    }

    open fun callback(function: () -> Unit) {
        if (callbackInMainThread) {
            // TODO Handlerは入れたくない
            handler.post {
                function.invoke()
            }
        } else {
            function.invoke()
        }
    }

    open fun makeURL(baseURL: String, query: URLQuery?, encoder: URLEncoder): URL? {
        var urlStr = baseURL

        val query = query
        if (query != null) {
            val separator = if (urlStr.contains("?")) "&" else "?"
            urlStr += separator + query.stringValue(encoder = urlEncoder)
        }

        return URL(urlStr)
    }
}

// TODO この参照方法は少し汚い
data class DefaultImplementation(
    var urlEncoder: () -> URLEncoder = { DefaultURLEncoder() },
    var httpConnector: () -> HTTPConnector = { DefaultHTTPConnector() }
) {
    companion object {
        var shared = DefaultImplementation()
    }
}
