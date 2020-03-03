package abstracthttp.defaultimpl

import abstracthttp.core.HTTPConnector
import abstracthttp.entity.Request
import abstracthttp.entity.Response
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.net.CookieHandler
import java.net.CookieManager
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext

/**
 * HTTP通信の標準実装
 * TODO まだ精査できていない
 *
 */
class DefaultHTTPConnector : HTTPConnector {

    /** データ転送のタイムアウト期間（秒）。この期間データ転送が中断するとタイムアウトする。 */
    var timeoutInterval: Double = 15.0

    /** 自動でリダイレクトするか */
    var isRedirectEnabled = true

    /** Cookieを有効にするか */
    var isCookieEnabled = true

    /** ログ出力するか */
    var isLogEnabled = false

    /** 通信中の[HTTPTask] */
    private var httpTask: HTTPTask? = null

    override fun execute(request: Request, complete: (Response?, Exception?) -> Unit) {
        val config = HTTPConfig(
            timeout = (timeoutInterval * 1000.0).toInt(),
            isRedirectEnabled = isRedirectEnabled,
            isCookieEnabled = isCookieEnabled,
            isLogEnabled = isLogEnabled
        )
        httpTask = HTTPTask(config, complete)
        httpTask?.execute(request)
    }

    override fun cancel() {
        httpTask?.cancel()
    }

    class HTTPTask(private val config: HTTPConfig, private val complete: (Response?, Exception?) -> Unit) : CoroutineScope {

        private val logTag = javaClass.simpleName

        private val job = Job()

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job

        private val cookieManager by lazy {
            var manager = CookieHandler.getDefault()
            if (manager == null) {
                manager = CookieManager()
                CookieHandler.setDefault(manager)
            }
            return@lazy manager
        }

        fun execute(request: Request) {
            launch(Dispatchers.IO) {
                try {
                    val response = connect(request = request)
                    onComplete(response, null)
                } catch (e: Exception) {
                    onComplete(null, e)
                }
            }
        }

        fun cancel() {
            job.cancel()
            log("cancel.")
        }

        private fun connect(request: Request): Response? {
            var connection: HttpURLConnection? = null

            try {
                // 接続
                connection = makeURLConnection(request)
                connection.connect()
                log("connect.")

                // Cookieの保存
                if (config.isCookieEnabled) {
                    cookieManager.put(request.url.toURI(), connection.headerFields)
                }
                // リダイレクト
                val location = getRedirectLocation(connection)
                if (location != null) {
                    val body = if (connection.responseCode == 303) null else request.body
                    return connect(request = Request(url = location, method = request.method, body = body, headers = request.headers))
                }
                return makeResponse(connection, connection.inputStream)
            } catch (e1: IOException) {
                if (connection == null) throw e1
                try {
                    // 404などの場合Exceptionが発生してもレスポンスがあるので読み取り
                    return makeResponse(connection, connection.errorStream)
                } catch (e2: IOException) {
                    throw e2
                }
            } finally {
                connection?.disconnect()
                log("disconnect.")
            }
        }

        private fun onComplete(response: Response?, exception: Exception?) {
            if (isActive) {
                complete(response, exception)
                log("complete.")
            }
        }

        private fun makeURLConnection(request: Request): HttpURLConnection {
            val connection = request.url.openConnection() as HttpURLConnection

            // メソッド指定
            connection.requestMethod = request.method.stringValue
            // 自動的でリダイレクトはしないように設定する（独自実装で制御）
            connection.instanceFollowRedirects = false
            // 接続タイムアウト指定
            // TODO iOS版と動きが異なるので、要検討
            connection.connectTimeout = config.timeout
            // ヘッダー付与
            request.headers.forEach {
                connection.setRequestProperty(it.key, it.value)
            }
            // リクエストボディの設定
            if (request.body != null) {
                connection.doOutput = true
                connection.outputStream.write(request.body)
            }
            return connection
        }

        private fun getRedirectLocation(connection: HttpURLConnection): URL? {
            val statusCode = connection.responseCode
            val location = connection.headerFields["Location"]?.firstOrNull()
            if (config.isRedirectEnabled && statusCode.toString().startsWith("3") && location != null) {
                return URL(location)
            }
            return null
        }

        private fun makeResponse(connection: HttpURLConnection, inputStream: InputStream?): Response? {
            val responseData = inputStream?.readBytes() ?: return null
            val headers = mutableMapOf<String, String>()
            connection.headerFields.filter { it.key != null }.forEach {
                headers[it.key] = it.value.joinToString(" ")
            }
            return Response(data = responseData, statusCode = connection.responseCode, headers = headers, nativeResponse = null)
        }

        private fun log(message: String) {
            if (config.isLogEnabled) {
                Log.d(logTag, message)
            }
        }
    }

    data class HTTPConfig(
        val timeout: Int,
        val isRedirectEnabled: Boolean,
        val isCookieEnabled: Boolean,
        val isLogEnabled: Boolean
    )
}