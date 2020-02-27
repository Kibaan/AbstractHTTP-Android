package abstracthttp.defaultimpl

import abstracthttp.core.HTTPConnector
import abstracthttp.entity.Request
import abstracthttp.entity.Response
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP通信の標準実装
 * TODO まだ精査できていない
 *
 */
class DefaultHTTPConnector : HTTPConnector {

    companion object {
        val httpClient = OkHttpClient()
    }

    var httpCall: Call? = null

    /** データ転送のタイムアウト期間（秒）。この期間データ転送が中断するとタイムアウトする。 */
    var timeoutInterval: Double = 15.0

    /** 自動でリダイレクトするか */
    var isRedirectEnabled = true

    /** キャッシュポリシー */
//    public var cachePolicy: NSURLRequest.CachePolicy? = .reloadIgnoringCacheData

    var isCancelled: Boolean = false

    override fun execute(request: Request, complete: (Response?, Exception?) -> Unit) {
        isCancelled = false

        // TODO connectTimeoutではタイムアウトしない・・・
        val client = httpClient.newBuilder()
            .connectTimeout(timeoutInterval.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutInterval.toLong(), TimeUnit.SECONDS)
            .cookieJar(CookieJarImpl())
            .build()

        val httpCall = client.newCall(makeURLRequest(request))
        this.httpCall = httpCall

        httpCall.enqueue(object : Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val response = makeResponse(response = response, data = response.body()?.bytes())
                complete(response, null)
                this@DefaultHTTPConnector.httpCall = null
            }

            override fun onFailure(call: Call, e: IOException) {
                complete(null, e)
                this@DefaultHTTPConnector.httpCall = null
            }
        })
    }

    override fun cancel() {
        httpCall?.cancel()
        isCancelled = true
    }

    private fun makeResponse(response: okhttp3.Response?, data: ByteArray?): Response? {
        val response = response ?: return null
        val headers: Map<String, String> = mapOf()
        val responseHeaders = response.headers()
        val allHeaderFields = mutableMapOf<String, String>()
        responseHeaders.names().forEach {
            val headerValue = responseHeaders[it] ?: return@forEach
            allHeaderFields[it] = headerValue
        }
        return Response(data = data ?: ByteArray(0), statusCode = response.code(), headers = headers, nativeResponse = response)
    }

    private fun makeURLRequest(request: Request): okhttp3.Request {
        val requestBuilder = okhttp3.Request.Builder()
            .url(request.url).cacheControl(CacheControl.Builder().noCache().noStore().build())
        val headerContentType = request.headers.filter { it.key.toLowerCase() == "content-type" }.values.firstOrNull()
        val contentType = headerContentType ?: "application/octet-stream"
        var requestBody: RequestBody? = null
        if (request.body != null) {
            requestBody = RequestBody.create(MediaType.parse(contentType), request.body)
        }
        requestBuilder.method(request.method.stringValue, requestBody)

        // ヘッダー付与
        request.headers.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
        return requestBuilder.build()
    }

    // region -> CookieJar

    private class CookieJarImpl : CookieJar {

        override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
            HTTPCookieStorage.shared.setCookies(url, cookies)
        }

        override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
            return HTTPCookieStorage.shared.getCookies(url).toMutableList()
        }
    }

    // endregion

    // region -> HTTPCookieStorage

    class HTTPCookieStorage {

        companion object {
            private var instance: HTTPCookieStorage? = null
            val shared: HTTPCookieStorage
                get() {
                    var instance = this.instance
                    if (instance != null) {
                        return instance
                    }
                    instance = HTTPCookieStorage()
                    this.instance = instance
                    return instance
                }

        }

        // endregion

        // region -> Variables

        private var cookieMap: MutableMap<String, MutableList<Cookie>> = mutableMapOf()

        // endregion

        // region -> Function

        @Synchronized
        fun setCookies(url: HttpUrl, cookies: MutableList<Cookie>) {
            val targetList = cookieMap[url.host()] ?: mutableListOf()
            cookies.forEach { cookie ->
                targetList.removeAll { it.keyEquals(cookie) }
            }
            targetList.addAll(cookies)
            cookieMap[url.host()] = targetList
        }

        @Synchronized
        fun getCookies(url: HttpUrl): List<Cookie> {
            val host = url.host()
            removeExpiredCookies(host)

            val path = url.encodedPath()
            val targetList = cookieMap[host] ?: return mutableListOf()

            return targetList.filter {
                path.startsWith(it.path()) && it.domain() == host
            }
        }

        @Suppress("MemberVisibilityCanBePrivate")
        private fun removeExpiredCookies(host: String) {
            val targetList = cookieMap[host] ?: return
            targetList.removeAll { it.hasExpired }
            cookieMap[host] = targetList
        }

        @Synchronized
        fun clear() {
            cookieMap.clear()
        }

        private val Cookie.hasExpired: Boolean
            get() = expiresAt() < System.currentTimeMillis()

        private fun Cookie.keyEquals(other: Any?): Boolean {
            if (other !is Cookie) return false
            val that = other as Cookie?
            return that?.name() == name() && that?.domain() == domain() && that?.path() == path()
        }
    }

    // endregion
}