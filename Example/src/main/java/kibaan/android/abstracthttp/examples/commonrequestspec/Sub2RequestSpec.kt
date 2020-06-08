package kibaan.android.abstracthttp.examples.commonrequestspec

import abstracthttp.android.enumtype.HTTPMethod
import com.google.gson.reflect.TypeToken
import kibaan.android.abstracthttp.entity.Post
import java.lang.reflect.Type


/**
 * 個別のAPIで異なるプロパティのみ定義し、他のプロパティはCommonRequestSpecのものを使う
 */
class Sub2RequestSpec(val postId: Int) : CommonRequestSpec<Post>() {

    override val type: Type?
        get() = TypeToken.getParameterized(Post::class.java).type

    override val path: String
        get() = "posts/${postId}"

    override val httpMethod: HTTPMethod
        get() = HTTPMethod.get
}
