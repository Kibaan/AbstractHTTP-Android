package abstracthttp.convenient

import abstracthttp.core.*
import abstracthttp.defaultimpl.DefaultHTTPConnector
import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Response
import abstracthttp.entity.URLQuery
import abstracthttp.enumtype.HTTPMethod
import java.nio.charset.Charset


/// HTTP通信の簡易インターフェース
/// RequestSpec、ResponseSpecなどのプロトコルを実装せず簡易に通信を行いたい場合に使える
/// 内部的には `Connection` クラスを使って通信しているため、`Connection` クラスが持つ機能は全て使える
class HTTP {

    var url: String
    var httpMethod: HTTPMethod = HTTPMethod.get
    var headers: Map<String, String> = mapOf()
    var urlQuery: URLQuery? = null
    var body: ByteArray? = null
    var validate: ((Response) -> Boolean)? = null

    var listeners: MutableList<ConnectionListener> = mutableListOf()
    var responseListeners: MutableList<ConnectionResponseListener> = mutableListOf()
    var errorListeners: MutableList<ConnectionErrorListener> = mutableListOf()

    var httpConnector: HTTPConnector? = null
    var urlEncoder: URLEncoder? = null
    var callbackInMainThread: Boolean? = null
    var isLogEnabled: Boolean? = null
    var setupDefaultHTTPConnector: ((DefaultHTTPConnector) -> Unit)? = null

    var holder: ConnectionHolder? = null

    constructor(url: String) {
        this.url = url
    }

    fun httpMethod(httpMethod: HTTPMethod) : HTTP {
        this.httpMethod = httpMethod
        return this
    }

    fun headers(headers: Map<String, String>) : HTTP {
        this.headers = headers
        return this
    }

    fun urlQuery(urlQuery: URLQuery) : HTTP {
        this.urlQuery = urlQuery
        return this
    }

    fun body(body: ByteArray) : HTTP {
        this.body = body
        return this
    }

    fun validate(validate: (Response) -> Boolean) : HTTP {
        this.validate = validate
        return this
    }

    fun httpConnector(httpConnector: HTTPConnector) {
        this.httpConnector = httpConnector
    }

    fun urlEncoder(urlEncoder: URLEncoder) {
        this.urlEncoder = urlEncoder
    }

    fun callbackInMainThread(callbackInMainThread: Boolean) {
        this.callbackInMainThread = callbackInMainThread
    }

    fun isLogEnabled(isLogEnabled: Boolean) {
        this.isLogEnabled = isLogEnabled
    }

    fun holder(holder: ConnectionHolder) {
        this.holder = holder
    }

    fun asResponse(callback: (Response) -> Unit) : Connection<Response> {
        return start(parse = { it }, callback = callback)
    }

    fun asString(encoding: Charset = Charsets.UTF_8, callback: (String) -> Unit) : Connection<String> {
        return start(parse = { it.data.toString(encoding) }, callback = callback)
    }

    fun asData(callback: (ByteArray) -> Unit) : Connection<ByteArray> {
        return start(parse = { it.data }, callback = callback)
    }

    fun <T: Any> asModel(parse: (Response) -> T, callback: (T) -> Unit) : Connection<T> {
        return start(parse = parse, callback = callback)
    }

    fun addListener(listener: ConnectionListener) : HTTP {
        listeners.add(listener)
        return this
    }

    public fun addResponseListener(listener: ConnectionResponseListener) : HTTP {
        responseListeners.add(listener)
        return this
    }

    public fun addErrorListener(listener: ConnectionErrorListener) : HTTP {
        errorListeners.add(listener)
        return this
    }

    public fun addOnError(onError: (ConnectionError, Response?, Any?) -> Unit) : HTTP =
        addErrorListener(OnError(onError))

    public fun addOnEnd(onEnd: (Response?, Any?, ConnectionError?) -> Unit) : HTTP =
        addListener(OnEnd(onEnd))

    public fun setupDefaultHTTPConnector(setup: (DefaultHTTPConnector) -> Unit) : HTTP {
        this.setupDefaultHTTPConnector = setup
        return this
    }

    private fun <T: Any> start(parse: (Response) -> T, callback: (T) -> Unit) : Connection<T> {
        val spec = CustomConnectionSpec(url = url, httpMethod = httpMethod, headers = headers, urlQuery = urlQuery, body = body, validate = validate, parse = parse)
        val connection = Connection(spec, onSuccess = callback)
        listeners.forEach { connection.addListener(it) }
        responseListeners.forEach { connection.addResponseListener(it) }
        errorListeners.forEach { connection.addErrorListener(it) }
        val httpConnector = httpConnector
        if (httpConnector != null) {
            connection.httpConnector = httpConnector
        }
        val urlEncoder = urlEncoder
        if (urlEncoder != null) {
            connection.urlEncoder = urlEncoder
        }
        val callbackInMainThread = callbackInMainThread
        if (callbackInMainThread != null) {
            connection.callbackInMainThread = callbackInMainThread
        }
        val isLogEnabled = isLogEnabled
        if (isLogEnabled != null) {
            connection.isLogEnabled = isLogEnabled
        }
        val holder = holder
        if (holder != null) {
            connection.holder = holder
        }
        val setupDefaultHTTPConnector = setupDefaultHTTPConnector
        val connector = connection.httpConnector as? DefaultHTTPConnector
        if (setupDefaultHTTPConnector != null && connector != null) {
            setupDefaultHTTPConnector(connector)
        }
        connection.start()
        return connection
    }
}
