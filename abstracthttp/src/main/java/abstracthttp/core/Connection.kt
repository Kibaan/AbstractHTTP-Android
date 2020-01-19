package abstracthttp.core

import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Request
import abstracthttp.entity.Response
import abstracthttp.entity.URLQuery
import abstracthttp.enumtype.ConnectionErrorType
import java.net.URL

/**
 * HTTP通信のライフサイクル
 * 通信に必要な各種モジュールを取りまとめ、通信処理の実行と各種コールバックやリスナーの呼び出しを行う
 *
 * The lifecycle of a HTTP connection.
 */
open class Connection<ResponseModel: Any> {

    /** HTTPリクエストの仕様 */
    var requestSpec: RequestSpec

    /** レスポンスデータの正当性を検証する */
    var validate: (Response) -> Boolean

    /** 通信レスポンスをデータモデルに変換する */
    var parseResponse: (Response) -> ResponseModel

    /** 通信開始と終了のリスナー */
    var listeners: MutableList<ConnectionListener> = mutableListOf()

    /** 通信レスポンス処理のリスナー */
    var responseListeners: MutableList<ConnectionResponseListener> = mutableListOf()

    /** エラーのリスナー */
    var errorListeners: MutableList<ConnectionErrorListener> = mutableListOf()

    /** HTTP通信処理 */
    var httpConnector: HTTPConnector = ConnectionConfig.shared.httpConnector()

    /** URLエンコード処理 */
    var urlEncoder: URLEncoder = ConnectionConfig.shared.urlEncoder()

    /** ログ出力を有効にするか */
    var isLogEnabled = ConnectionConfig.shared.isLogEnabled

    /** コールバックをメインスレッドで呼び出すか */
    var callbackInMainThread = true

    /** 通信成功時のコールバック */
    var onSuccess: ((ResponseModel) -> Unit)? = null

    /** 直近のリクエスト */
    var latestRequest: Request? = null
        private set

    /** 実行中の通信オブジェクトを保持するコンテナ */
    var holder = ConnectionHolder.shared

    /** UIスレッドでの実行 */
    var runOnUiThread: (() -> Unit) -> Unit = ConnectionConfig.shared.runOnUiThread

    /** 実行ID */
    var executionId: ExecutionId? = null
        private set

    /** 中断中の実行ID */
    var interruptedId: ExecutionId? = null
        private set

    constructor(requestSpec: RequestSpec, responseSpec: ResponseSpec<ResponseModel>, onSuccess: ((ResponseModel) -> Unit)? = null) {
        this.requestSpec = requestSpec
        this.parseResponse = responseSpec::parseResponse
        this.validate = responseSpec::validate
        this.onSuccess = onSuccess
    }

    constructor(connectionSpec: ConnectionSpec<ResponseModel>, onSuccess: ((ResponseModel) -> Unit)? = null) {
        this.requestSpec = connectionSpec
        this.parseResponse = connectionSpec::parseResponse
        this.validate = connectionSpec::validate
        this.onSuccess = onSuccess
    }

    /**
     * 通信を開始する
     */
    fun start() {
        connect()
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
        // 既に実行完了している場合何もしない
        val executionId = this.executionId ?: return

        onCancel(executionId)
        httpConnector.cancel()
    }

    /**
     * コールバック処理の実行を中断する
     */
    open fun interrupt() {
        interruptedId = executionId
        executionId = null
        holder.remove(connection = this)
    }


    /**
     * `interrupt()`による中断を終了する
     * キャンセル扱いになり、キャンセル時と同じコールバックが呼ばれる
     */
    open fun breakInterruption() {
        val interruptedId = interruptedId
        if (executionId != null || interruptedId == null) {
            return
        }

        this.executionId = interruptedId
        this.interruptedId = null
        onCancel(executionId = interruptedId)
    }

    /**
     * 通信処理を開始する
     *
     * @param implicitly 通信開始のコールバックを呼ばずに再通信する場合は `true` を指定する。
     */
    private fun connect(request: Request? = null, implicitly: Boolean = true) {
        val executionId = ExecutionId()
        this.executionId = executionId
        this.interruptedId = null

        val url = makeURL(baseURL = requestSpec.url, query = requestSpec.urlQuery, encoder = urlEncoder)
        if (url == null) {
            onInvalidURLError(executionId = executionId)
            return
        }

        // リクエスト作成
        val request = request ?: Request(
            url = url,
            method = requestSpec.httpMethod,
            body = requestSpec.makeBody(),
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
            this.complete(response = response, error = error, executionId = executionId)
        })

        latestRequest = request
    }

    /**
     * 通信完了時の処理
     */
    private fun complete(response: Response?, error: Exception?, executionId: ExecutionId) {
        if (executionId != this.executionId) { return }

        val response = response
        if (response == null || error != null) {
            onNetworkError(error = error, executionId = executionId)
            return
        }

        var listenerResult = true
        responseListeners.forEach {
            listenerResult = listenerResult && it.onReceived(connection = this, response = response)
            if (executionId != this.executionId) { return }
        }

        if (!listenerResult || !validate(response)) {
            onResponseError(response = response, executionId = executionId)
            return
        }

        handleResponse(response = response, executionId = executionId)
    }

    open fun handleResponse(response: Response, executionId: ExecutionId) {

        val responseModel: ResponseModel

        try {
            responseModel = parseResponse(response)
        } catch (error: Exception) {
            onParseError(response = response, error = error, executionId = executionId)
            return
        }

        var listenerResult = true
        responseListeners.forEach {
            listenerResult = listenerResult && it.onReceivedModel(connection = this, responseModel = responseModel)
        }
        if (!listenerResult) {
            onValidationError(response = response, responseModel = responseModel, executionId = executionId)
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

    fun onInvalidURLError(executionId: ExecutionId) {
        handleError(ConnectionErrorType.invalidURL, executionId = executionId) {
            it.onNetworkError(connection = this, error = null)
        }
    }

    fun onNetworkError(error: Exception?, executionId: ExecutionId) {
        handleError(ConnectionErrorType.network, error = error, executionId = executionId) {
            it.onNetworkError(connection = this, error = error)
        }
    }

    fun onResponseError(response: Response, executionId: ExecutionId) {
        handleError(ConnectionErrorType.invalidResponse, response = response, executionId = executionId) {
            it.onResponseError(connection = this, response = response)
        }
    }

    fun onParseError(response: Response, error: Exception, executionId: ExecutionId) {
        handleError(ConnectionErrorType.parse, error = error, response = response, executionId = executionId) {
            it.onParseError(connection = this, response = response, error = error)
        }
    }

    fun onValidationError(response: Response, responseModel: ResponseModel, executionId: ExecutionId) {
        handleError(ConnectionErrorType.validation, response = response, responseModel = responseModel, executionId = executionId) {
            it.onValidationError(connection = this, response = response, responseModel = responseModel)
        }
    }

    fun onCancel(executionId: ExecutionId) {
        handleError(ConnectionErrorType.canceled, executionId = executionId) {
            it.onCanceled(connection = this)
        }
    }

    /**
     * エラーを処理する
     */
    private fun handleError(type: ConnectionErrorType,
                            error: Exception? = null,
                            response: Response? = null,
                            responseModel: ResponseModel? = null,
                            executionId: ExecutionId,
                            callListener: (ConnectionErrorListener) -> Unit) {
        // エラーログ出力
        if (isLogEnabled) {
            val message = error?.toString() ?: ""
            // TODO Log.dにしなくてよいのか？
            print("[ConnectionError] Type= ${type.description}, NativeMessage=${message}")
        }

        callback {
            errorProcess(type, error, response, responseModel, executionId, callListener)
        }
    }


    /**
     * エラーを処理の実行
     */
    private fun errorProcess(type: ConnectionErrorType,
                             error: Exception? = null,
                             response: Response? = null,
                             responseModel: ResponseModel? = null,
                             executionId: ExecutionId,
                             callListener: (ConnectionErrorListener) -> Unit) {
        errorListeners.forEach {
            callListener(it)
            if (executionId != this.executionId) { return }
        }

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
        holder.remove(connection = this)
        executionId = null
        listeners.forEach { it.onEnd(connection = this, response = response, responseModel = responseModel, error = error) }
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

    /**
     * 終了処理を追加する。
     * 終了処理は `ConnectionListener` として登録され、このプロトコルを経由して引数の`onEnd`が実行される
     */
    fun addOnEnd(onEnd: (Response?, Any?, ConnectionError?) -> Unit): Connection<*> {
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

    open fun callback(function: () -> Unit) {
        if (callbackInMainThread) {
            runOnUiThread {
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

class ExecutionId()