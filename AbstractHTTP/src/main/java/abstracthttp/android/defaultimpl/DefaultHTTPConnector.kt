package abstracthttp.android.defaultimpl

import abstracthttp.android.core.HTTPConnector
import abstracthttp.android.entity.Request
import abstracthttp.android.entity.Response
import abstracthttp.android.enumtype.HTTPMethod
import java.io.IOException
import java.io.InputStream
import java.net.*
import kotlin.concurrent.thread

/**
 * スレッドを使ったHTTP通信の実装
 */
class DefaultHTTPConnector : HTTPConnector {

    companion object {
        val cookieManager: CookieManager = CookieManager()
    }

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

    private inner class HTTPTask(private val complete: (Response?, Exception?) -> Unit) {

        private var thread: Thread? = null

        fun execute(request: Request) {
            thread = thread {
                try {
                    val response = connect(request = request)
                    complete(response, null)
                } catch (e: Exception) {
                    complete(null, e)
                }
            }
        }

        fun cancel() {
            thread?.interrupt()
        }

        private fun connect(request: Request): Response? {
            var connection: HttpURLConnection? = null
            var headerFields: Map<String?, List<String>>? = null

            try {
                // 接続
                connection = makeURLConnection(request)
                connection.connect()

                // キャンセル済みの場合は以降の処理は実行しない
                assertNotCancelled()

                // ヘッダーの取得
                headerFields = connection.headerFields

                // Cookieの保存
                if (isCookieEnabled) {
                    cookieManager.put(request.url.toURI(), headerFields)
                }
                // リダイレクト
                val location = getRedirectLocation(connection)
                if (location != null) {
                    val isSeeOther = connection.responseCode == 303
                    val body = if (isSeeOther) null else request.body
                    val method = if (isSeeOther) HTTPMethod.get else request.method

                    // NOTE 再帰的に呼び出しをしている為、無限にリダイレクトする可能性がある
                    return connect(request = Request(url = location, method = method, body = body, headers = request.headers))
                }
                val response = makeResponse(connection.responseCode, headerFields, connection.inputStream, connection)

                // キャンセル済みの場合は以降の処理は実行しない
                assertNotCancelled()

                return response
            } catch (e: IOException) {
                if (connection == null) throw e

                // 404などの場合Exceptionが発生してもレスポンスがあるので読み取り
                return makeResponse(connection.responseCode, headerFields, connection.errorStream, connection) ?: throw e
            } finally {
                connection?.disconnect()
            }
        }

        private fun assertNotCancelled() {
            if (thread?.isInterrupted == true) throw IOException("The connection is cancelled by program.")
        }

        private fun makeURLConnection(request: Request): HttpURLConnection {
            val connection = request.url.openConnection() as HttpURLConnection

            // メソッド指定
            connection.requestMethod = request.method.stringValue
            // 自動的でリダイレクトはしないように設定する（独自実装で制御）
            connection.instanceFollowRedirects = false
            // 接続タイムアウト指定
            connection.connectTimeout = (timeoutInterval * 1000).toInt()
            connection.readTimeout = (timeoutInterval * 1000).toInt()
            // ヘッダー付与
            request.headers.forEach {
                connection.setRequestProperty(it.key, it.value)
            }
            // クッキー付与
            if (isCookieEnabled) {
                cookieManager.cookieStore.get(request.url.toURI()).forEach {
                    connection.setRequestProperty("Cookie", it.toString())
                }
            }
            // リクエストボディの設定
            if (request.body != null) {
                // Content-Typeがない場合にデフォルト値を設定
                val headerContentType = request.headers.keys.firstOrNull { it.toLowerCase() == "content-type" }
                if (headerContentType == null) {
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }
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

        private fun makeResponse(statusCode: Int, headerFields: Map<String?, List<String>>?, inputStream: InputStream?, connection: HttpURLConnection): Response? {
            val headerFields = headerFields ?: return null
            val responseData = inputStream?.readBytes() ?: return null
            val headers = mutableMapOf<String, String>()
            headerFields.filter { it.key != null }.forEach {
                val key = it.key ?: return@forEach
                headers[key] = it.value.joinToString(", ")
            }
            return Response(data = responseData, statusCode = statusCode, headers = headers, nativeResponse = connection)
        }
    }
}