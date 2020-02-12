package abstracthttp.defaultimpl

import abstracthttp.core.HTTPConnector
import abstracthttp.entity.Request
import abstracthttp.entity.Response
import abstracthttp.enumtype.HTTPMethod
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
    var isCancelled: Boolean = false

//    private class CookieJarImpl: CookieJar {
//
//        override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
//            HTTPCookieStorage.shared.setCookies(url, cookies)
//        }
//
//        override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
//            return HTTPCookieStorage.shared.getCookies(url).toMutableList()
//        }
//    }

    override fun execute(request: Request, complete: (Response?, Exception?) -> Unit) {
        isCancelled = false

        val client = httpClient.newBuilder()
            .connectTimeout(timeoutInterval.toLong(), TimeUnit.SECONDS)
//            .cookieJar(CookieJarImpl())
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

        if (request.body != null) {
            val headerContentType = request.headers.first { it.key.toLowerCase() == "content-type" }?.value
            val contentType = headerContentType ?: "application/octet-stream"
            val body = RequestBody.create(MediaType.parse(contentType), request.body)
            // TODO 要修正！POST固定になっている
            requestBuilder.post(body)
        }

        // ヘッダー付与
        for ((key, value) in request.headers) {
            requestBuilder.addHeader(key, value)
        }
        return requestBuilder.build()
    }

    private fun <K, T> Map<K, T>.first(where: (Map.Entry<K, T>) -> Boolean): Map.Entry<K, T>? {
        entries.forEach {
            if (where(it)) {
                return it
            }
        }
        return null
    }
}