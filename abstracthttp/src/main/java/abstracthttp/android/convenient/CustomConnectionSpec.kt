package abstracthttp.android.convenient

import abstracthttp.android.core.ConnectionSpec
import abstracthttp.android.entity.Response
import abstracthttp.android.entity.URLQuery
import abstracthttp.android.enumtype.HTTPMethod


class CustomConnectionSpec<T> : ConnectionSpec<T> {

    override val url: String
    override val httpMethod: HTTPMethod
    override val headers: Map<String, String>
    override val urlQuery: URLQuery?
    override val body: ByteArray?

    val validate: ((Response) -> Boolean)?
    val parse: (Response) -> T

    constructor(url: String,
                httpMethod: HTTPMethod,
                headers: Map<String, String>,
                urlQuery: URLQuery?,
                body: ByteArray?,
                validate: ((Response) -> Boolean)?,
                parse: (Response) -> T) {

        this.url = url
        this.httpMethod = httpMethod
        this.headers = headers
        this.urlQuery = urlQuery
        this.body = body
        this.validate = validate
        this.parse = parse
    }

    override fun validate(response: Response) : Boolean =
        validate?.invoke(response) ?: true

    override fun parseResponse(response: Response): T =
        parse(response)
}
