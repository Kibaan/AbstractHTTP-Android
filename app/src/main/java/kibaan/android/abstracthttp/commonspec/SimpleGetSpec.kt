package kibaan.android.abstracthttp.commonspec

import abstracthttp.android.core.ConnectionSpec
import abstracthttp.android.entity.Response
import abstracthttp.android.entity.URLQuery
import abstracthttp.android.enumtype.HTTPMethod

/**
 * シンプルなGETの実装。GET取得したデータを文字列として返す
 */
class SimpleGetSpec(override var url: String) : ConnectionSpec<String> {

    override val httpMethod: HTTPMethod
        get() = HTTPMethod.get

    override val headers: Map<String, String>
        get() = mapOf()

    override val urlQuery: URLQuery?
        get() = null

    override val body: ByteArray?
        get() = null

    override fun validate(response: Response): Boolean = true

    override fun parseResponse(response: Response): String {
        return response.data.toString(Charsets.UTF_8)
    }
}
