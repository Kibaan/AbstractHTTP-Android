package kibaan.android.abstracthttp.examples.getjson

import abstracthttp.android.core.ConnectionSpec
import abstracthttp.android.entity.Response
import abstracthttp.android.entity.URLQuery
import abstracthttp.android.enumtype.HTTPMethod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kibaan.android.abstracthttp.entity.User

/**
 * JSONを取得してモデルインスタンスにして返す実装
 */
class GetJSONSpec : ConnectionSpec<List<User>> {

    override val url: String
        get() = "https://jsonplaceholder.typicode.com/users"

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

    override fun parseResponse(response: Response): List<User> {
        val json = response.data.toString(Charsets.UTF_8)
        val type = TypeToken.getParameterized(List::class.java, User::class.java).type
        return Gson().fromJson(json, type)
    }
}
