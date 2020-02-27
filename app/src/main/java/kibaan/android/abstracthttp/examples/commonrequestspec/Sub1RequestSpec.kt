package kibaan.android.abstracthttp.examples.commonrequestspec

import abstracthttp.enumtype.HTTPMethod
import com.google.gson.reflect.TypeToken
import kibaan.android.abstracthttp.entity.User
import java.lang.reflect.Type

/**
 * 個別のAPIで異なるプロパティのみ定義し、他のプロパティはCommonRequestSpecのものを使う
 */
class Sub1RequestSpec(val userId: Int) : CommonRequestSpec<User>() {

    override val type: Type?
        get() = TypeToken.getParameterized(User::class.java).type

    override val path: String
        get() = "users/${userId}"

    override val httpMethod: HTTPMethod
        get() = HTTPMethod.get
}
