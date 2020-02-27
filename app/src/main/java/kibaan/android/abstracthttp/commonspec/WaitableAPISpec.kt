package kibaan.android.abstracthttp.commonspec

import abstracthttp.core.ConnectionSpec
import abstracthttp.entity.Response
import abstracthttp.entity.URLQuery
import abstracthttp.enumtype.HTTPMethod


/**
 * サーバー側で指定した秒数待機するAPI
 */
class WaitableAPISpec(private val waitSeconds: Int = 1) : ConnectionSpec<String> {

    override val url: String
        get() = "https://apidemo.altonotes.co.jp/timeout-test"

    override val httpMethod: HTTPMethod
        get() = HTTPMethod.get

    override val headers: Map<String, String>
        get() = mapOf()

    override val urlQuery: URLQuery?
        get() = URLQuery("waitSeconds" to "$waitSeconds")

    override val body: ByteArray?
        get() = null

    override fun validate(response: Response): Boolean {
        return true
    }

    override fun parseResponse(response: Response): String {
        return response.data.toString(Charsets.UTF_8)
    }
}
