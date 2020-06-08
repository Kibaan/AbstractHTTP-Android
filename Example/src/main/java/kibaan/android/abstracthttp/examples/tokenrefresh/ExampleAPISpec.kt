package kibaan.android.abstracthttp.examples.tokenrefresh

import abstracthttp.android.core.ConnectionSpec
import abstracthttp.android.entity.Response
import abstracthttp.android.entity.URLQuery
import abstracthttp.android.enumtype.HTTPMethod

class ExampleAPISpec(private val tokenContainer: TokenContainer) : ConnectionSpec<String> {

    override val url: String
        get() = "https://apidemo.altonotes.co.jp/authexample"

    override val httpMethod: HTTPMethod
        get() = HTTPMethod.get

    override val headers: Map<String, String>
        get() = mapOf("Authorization" to (tokenContainer.token ?: ""))

    override val urlQuery: URLQuery?
        get() = null

    override val body: ByteArray?
        get() = null

    override fun validate(response: Response): Boolean {
        return response.statusCode in 200..299
    }

    override fun parseResponse(response: Response): String {
        return response.data.toString(Charsets.UTF_8)
    }
}
