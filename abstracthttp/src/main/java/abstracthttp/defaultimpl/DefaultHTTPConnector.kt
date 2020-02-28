package abstracthttp.defaultimpl

import abstracthttp.core.HTTPConnector
import abstracthttp.entity.Request
import abstracthttp.entity.Response
import abstracthttp.enumtype.HTTPMethod
import android.os.AsyncTask
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.CookieHandler
import java.net.CookieManager
import java.net.HttpURLConnection


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
        val config = HTTPConfig(
            timeout = (timeoutInterval * 1000.0).toInt(),
            instanceFollowRedirects = isRedirectEnabled,
            isCookieEnabled = isCookieEnabled
        )
        httpTask = HTTPTask(config, complete).execute(request) as? HTTPTask
    }

    override fun cancel() {
        httpTask?.cancel(true)
    }

    class HTTPTask(
        private val config: HTTPConfig,
        private val complete: (Response?, Exception?) -> Unit
    ) : AsyncTask<Request, Void, Response?>() {

        private val cookieManager by lazy {
            val manager = CookieManager()
            CookieHandler.setDefault(manager)
            return@lazy manager
        }

        override fun doInBackground(vararg requests: Request): Response? {
            return connect(request = requests.first())
        }

        override fun onPostExecute(result: Response?) {
            super.onPostExecute(result)
            complete.invoke(result, null)
        }

        private fun connect(request: Request): Response? {
            var connection: HttpURLConnection? = null
            val outputStream: OutputStream?

            try {
                // 接続
                connection = request.url.openConnection() as HttpURLConnection

                // メソッド指定
                connection.requestMethod = request.method.stringValue
                // 自動的でリダイレクトするか
                // TODO HttpからHttpsにリダイレクトする場合は自動的にリダレクトされないが許容するか？
                connection.instanceFollowRedirects = config.instanceFollowRedirects
                // 接続タイムアウト指定
                connection.connectTimeout = config.timeout
                connection.readTimeout = config.timeout
                // ヘッダー付与
                setHeaderToConnection(connection, request.headers)

                // リクエストボディの設定
                if (request.body != null && request.method == HTTPMethod.post) {
                    connection.doOutput = true
                    outputStream = connection.outputStream
                    outputStream.write(request.body)
                } else {
                    connection.doOutput = false
                }
                connection.connect()

                // Cookieの保存
                if (config.isCookieEnabled) {
                    cookieManager.put(request.url.toURI(), connection.headerFields)
                }
                return makeResponse(connection, connection.inputStream)
            } catch (e1: IOException) {
                if (connection == null) {
                    throw e1
                }
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

        private fun makeResponse(connection: HttpURLConnection, inputStream: InputStream?): Response? {
            val responseData = inputStream?.readBytes() ?: return null
            val headers = getHeaderByConnection(connection)
            return Response(data = responseData, statusCode = connection.responseCode, headers = headers, nativeResponse = null)
        }

        private fun setHeaderToConnection(connection: HttpURLConnection, headers: Map<String, String>) {
            headers.keys.forEachIndexed { index, key ->
                if (index == 0) {
                    connection.setRequestProperty(key, headers[key])
                } else {
                    connection.addRequestProperty(key, headers[key])
                }
            }
            // Cookieの設定
            if (config.isCookieEnabled && cookieManager.cookieStore.cookies.isNotEmpty()) {
                connection.setRequestProperty("Cookie", cookieManager.cookieStore.cookies.joinToString(";"))
            }
        }

        private fun getHeaderByConnection(connection: HttpURLConnection): Map<String, String> {
            val headers = mutableMapOf<String, String>()
            connection.headerFields.filter { it.key != null }.forEach {
                headers[it.key] = it.value.joinToString(" ")
            }
            return headers
        }
    }

    data class HTTPConfig(
        val timeout: Int,
        val instanceFollowRedirects: Boolean,
        val isCookieEnabled: Boolean
    )
}