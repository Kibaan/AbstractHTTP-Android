package kibaan.android.abstracthttp.examples.commonrequestspec

import abstracthttp.android.core.ConnectionSpec
import abstracthttp.android.entity.Response
import abstracthttp.android.entity.URLQuery
import abstracthttp.android.enumtype.HTTPMethod
import com.google.gson.Gson
import java.lang.reflect.Type


/**
 * 複数APIで共通のリクエスト・レスポンス仕様
 * 個別のAPI仕様はこのクラスを継承して作る
 */
open class CommonRequestSpec<T : Any> : ConnectionSpec<T> {

    // このクラスのparseResponseを利用する場合にTypeToken.getParameterizedで指定する
    open val type: Type? = null

    override val url: String
        get() = "https://jsonplaceholder.typicode.com/${path}"

    open val path: String
        get() {
            throw Exception("Override by sub classes")
        }

    override val httpMethod: HTTPMethod
        get() {
            throw Exception("Override by sub classes")
        }
    override val headers: Map<String, String>
        get() = mapOf("User-Agent" to "AbstractHttpExample")

    override val urlQuery: URLQuery?
        get() = null

    override val body: ByteArray?
        get() = null

    override fun validate(response: Response): Boolean {
        return true
    }

    override fun parseResponse(response: Response): T {
        val json = response.data.toString(Charsets.UTF_8)
        return Gson().fromJson(json, type)
    }
}
