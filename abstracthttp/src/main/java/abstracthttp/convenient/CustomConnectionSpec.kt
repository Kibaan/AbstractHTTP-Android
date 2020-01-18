package abstracthttp.convenient

import abstracthttp.core.ConnectionSpec
import abstracthttp.entity.Response
import abstracthttp.entity.URLQuery
import abstracthttp.enumtype.HTTPMethod


class CustomConnectionSpec<T>: ConnectionSpec<T> {

    override val url: String
    override val httpMethod: HTTPMethod
    override val headers: Map<String, String>
    override val urlQuery: URLQuery?

    val body: ByteArray?
    val isValidResponse: ((Response) -> Boolean)?
    val parse: (Response) -> T

    constructor(url: String,
                httpMethod: HTTPMethod,
                headers: Map<String, String>,
                urlQuery: URLQuery?,
                body: ByteArray?,
                isValidResponse: ((Response) -> Boolean)?,
                parse: (Response) -> T) {

        this.url = url
        this.httpMethod = httpMethod
        this.headers = headers
        this.urlQuery = urlQuery
        this.body = body
        this.isValidResponse = isValidResponse
        this.parse = parse
    }

    override fun makeBody() : ByteArray? =
        body

    override fun isValidResponse(response: Response) : Boolean =
        isValidResponse?.invoke(response) ?: true

    override fun parseResponse(response: Response) : T =
        parse(response)
}
