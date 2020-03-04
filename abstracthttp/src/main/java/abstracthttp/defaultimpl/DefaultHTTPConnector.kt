package abstracthttp.defaultimpl

import abstracthttp.core.HTTPConnector
import abstracthttp.entity.Request
import abstracthttp.entity.Response
import abstracthttp.enumtype.HTTPMethod
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

    /** 通信中の[HTTPTask] */
    private var httpTask: HTTPTask? = null

    override fun execute(request: Request, complete: (Response?, Exception?) -> Unit) {
        httpTask = HTTPTask(complete)
        httpTask?.execute(request)
    }

    override fun cancel() {
        httpTask?.cancel()
    }

    // TODO CoroutineScopeはHTTPTaskから切り出す
    private inner class HTTPTask(
        private val complete: (Response?, Exception?) -> Unit
    ) : CoroutineScope {

        private var job: Job? = null

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO

        private val cookieManager by lazy {
            var manager = CookieHandler.getDefault()
            if (manager == null) {
                manager = CookieManager()
                CookieHandler.setDefault(manager)
            }
            return@lazy manager
        }

        fun execute(request: Request) {
            job = launch {
                try {
                    val response = connect(request = request)
                    complete(response, null)
                } catch (e: Exception) {
                    complete(null, e)
                }
            }
        }

        fun cancel() {
            job?.cancel()
        }

        private fun connect(request: Request): Response? {
            var connection: HttpURLConnection? = null

            try {
                // 接続
                connection = makeURLConnection(request)
                connection.connect()

                // キャンセル済みの場合は以降の処理は実行しない
                if (!isActive) return null

                // Cookieの保存
                if (isCookieEnabled) {
                    cookieManager.put(request.url.toURI(), connection.headerFields)
                }
                // リダイレクト
                val location = getRedirectLocation(connection)
                if (location != null) {
                    val isSeeOther = connection.responseCode == 303
                    val body = if (isSeeOther) null else request.body
                    val method = if (isSeeOther) HTTPMethod.get else request.method
                    return connect(request = Request(url = location, method = method, body = body, headers = request.headers))
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
            connection.connectTimeout = (timeoutInterval * 1000.0).toInt()
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
            if (isRedirectEnabled && statusCode.toString().startsWith("3") && location != null) {
                return URL(location)
            }
            return null
        }

        private fun makeResponse(connection: HttpURLConnection, inputStream: InputStream?): Response? {
            val responseData = inputStream?.readBytes() ?: return null
            val headers = mutableMapOf<String, String>()
            connection.headerFields.filter { it.key != null }.forEach {
                headers[it.key] = it.value.joinToString(", ")
            }
            return Response(data = responseData, statusCode = connection.responseCode, headers = headers, nativeResponse = null)
        }
    }
}