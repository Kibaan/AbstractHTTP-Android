package kibaan.android.abstracthttp.core

import android.os.Handler
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

    var httpConnector: HTTPConnector = ConnectionConfig.shared.httpConnector()
    var urlEncoder: URLEncoder = ConnectionConfig.shared.urlEncoder()

    var isLogEnabled = ConnectionConfig.shared.isLogEnabled

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

    /**
     * 通信成功時のコールバック
     */
    var onSuccess: ((ResponseModel) -> Unit)? = null

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

    /**
     * エラー処理を追加する。
     * エラー処理は `ConnectionErrorListener` として登録され、このプロトコルを経由して引数の`onError`が実行される。
     *
     */
    fun addOnError(onError: (ConnectionError, Response?, ResponseModel?) -> Unit): Connection<*> {
        addErrorListener(OnError(onError))
        return this
    }

    fun setOnEnd(onEnd: (Response?, Any?, ConnectionError?) -> Unit): Connection<*> {
        addListener(OnEnd(onEnd))
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
    
    /**
     * 処理を開始する
     */
    fun start() {
        connect()
    }

    /**
     * 通信処理を開始する
     *
     * @param implicitly 通信開始のコールバックを呼ばずに再通信する場合は `true` を指定する。
     */
    private fun connect(request: Request? = null, implicitly: Boolean = true) {
        val url = makeURL(baseURL = requestSpec.url, query = requestSpec.urlQuery, encoder = urlEncoder)
        if (url == null) {
            onInvalidURLError()
            return
        }

        // リクエスト作成
        val request = request ?: Request(
            url = url,
            method = requestSpec.httpMethod,
            body = requestSpec.makePostData(),
            headers = requestSpec.headers.toMutableMap()
        )
        if (!implicitly) {
            listeners.forEach {
                it.onStart(connection = this, request = request)
            }
        }

        // このインスタンスが通信完了まで開放されないよう保持する必要がある
        holder.add(connection = this)

        if (isLogEnabled) {
            // TODO ログ出力する
            print("[${requestSpec.httpMethod.stringValue}] $url")
        }

        // 通信する
        httpConnector.execute(request = request, complete = { response, error ->
            this.complete(response = response, error = error)
        })

        latestRequest = request
    }

    /**
     * 通信完了時の処理
     */
    private fun complete(response: Response?, error: Exception?) {
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
        } catch (error: Exception) {
            onParseError(response = response, error = error)
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

    fun onInvalidURLError() {
        handleError(ConnectionErrorType.invalidURL) {
            it.onNetworkError(connection = this, error = null)
        }
    }

    fun onNetworkError(error: Exception?) {
        handleError(ConnectionErrorType.network, error = error) {
            it.onNetworkError(connection = this, error = error)
        }
    }

    fun onResponseError(response: Response) {
        handleError(ConnectionErrorType.invalidResponse, response = response) {
            it.onResponseError(connection = this, response = response)
        }
    }

    fun onParseError(response: Response, error: Exception) {
        handleError(ConnectionErrorType.parse, error = error, response = response) {
            it.onParseError(connection = this, response = response, error = error)
        }
    }

    fun onValidationError(response: Response, responseModel: ResponseModel) {
        handleError(ConnectionErrorType.validation, response = response, responseModel = responseModel) {
            it.onValidationError(connection = this, response = response, responseModel = responseModel)
        }
    }

    /**
     * エラーを処理する
     */
    private fun handleError(type: ConnectionErrorType, error: Exception? = null, response: Response? = null, responseModel: ResponseModel? = null, callListener: (ConnectionErrorListener) -> EventChain) {
        // エラーログ出力
        if (isLogEnabled) {
            val message = error?.toString() ?: ""
            // TODO Log.dにしなくてよいのか？
            print("[ConnectionError] Type= ${type.description}, NativeMessage=${message}")
        }

        callback {
            errorProcess(type, error, response, responseModel, callListener)
        }
    }


    /**
     * エラーを処理する
     */
    private fun errorProcess(type: ConnectionErrorType,
                             error: Exception? = null,
                             response: Response? = null,
                             responseModel: ResponseModel? = null,
                             callListener: (ConnectionErrorListener) -> EventChain) {
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

        afterError(type, error = error, response = response, responseModel = responseModel)
    }

    /**
     * エラー後の処理
     */
    open fun afterError(
        type: ConnectionErrorType,
        error: Exception? = null,
        response: Response? = null,
        responseModel: ResponseModel? = null) {
        
        val connectionError = ConnectionError(type = type, nativeError = error)
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


    private fun end(response: Response?, responseModel: Any?, error: ConnectionError?) {
        listeners.forEach { it.onEnd(connection = this, response = response, responseModel = responseModel, error = error) }
        holder.remove(connection = this)
    }
    
    /**
     * 通信を再実行する
     *
     * @args implicitly 通信開始のコールバックを呼ばずに再通信する場合は `true` を指定する。
     */
    open fun restart(implicitly: Boolean) {
        connect(implicitly = implicitly)
    }

    /**
     * 直近のリクエストを再送信する。
     * `restart` に近いふるまいになるが、リクエスト内容を再構築するか直近と全く同じリクエスト内容を使うかが異なる。
     * 例えばリクエストパラメーターに現在時刻を動的に含める場合、`repeatRequest` では前回リクエストと同時刻になるが `restart` では新しい時刻が設定される。
     *
     * @args implicitly 通信開始のコールバックを呼ばずに再通信する場合は `true` を指定する。
     */
    open fun repeatRequest(implicitly: Boolean) {
        connect(request = latestRequest, implicitly = implicitly)
    }

    /**
     * 通信をキャンセルする
     */
    open fun cancel() {
        // TODO 既に通信コールバックが走っている場合何もしない。通信コールバック内でキャンセルした場合に、onEndが二重で呼ばれないようにする必要がある
        isCancelled = true
        httpConnector.cancel()

        errorListeners.forEach { it.onCanceled(connection = this) }
        val error = ConnectionError(type = ConnectionErrorType.canceled, nativeError = null)
        end(response = null, responseModel = null, error = error)
    }

    open fun callback(function: () -> Unit) {
        if (callbackInMainThread) {
            // TODO Handlerを使わない
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