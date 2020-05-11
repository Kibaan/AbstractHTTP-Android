package kibaan.android.abstracthttp.examples.tokenrefresh

import abstracthttp.android.core.ConnectionSpec
import abstracthttp.android.entity.Response
import abstracthttp.android.entity.URLQuery
import abstracthttp.android.enumtype.HTTPMethod

/**
 * このAPIで取得したトークンは有効期限が15秒で、期限が切れると401エラーが発生する
 */
class GetTokenSpec(val fail: Boolean = false) : ConnectionSpec<String> {

    override val url: String
        get() {
            if (fail) {
                // fail = trueの場合は存在しないURLにして通信失敗させる
                return "https://sonzaishinaidomain.com"
            }
            return "https://apidemo.altonotes.co.jp/token"
        }
    override val httpMethod: HTTPMethod
        get() = HTTPMethod.get

    override val headers: Map<String, String>
        get() = mapOf()

    override val urlQuery: URLQuery?
        get() = null

    override val body: ByteArray?
        get() = null

    override fun validate(response: Response): Boolean {
        return true
    }

    override fun parseResponse(response: Response): String {
        return response.data.toString(Charsets.UTF_8)
    }
}
