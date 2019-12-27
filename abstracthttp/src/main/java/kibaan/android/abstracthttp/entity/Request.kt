package kibaan.android.abstracthttp.entity

import kibaan.android.abstracthttp.enum_type.HTTPMethod
import java.net.URL

/**
 * HTTPのリクエスト
 */
class Request public constructor(
    /** リクエストするURL */
    val url: URL,
    /** HTTPリクエストメソッド */
    val method: HTTPMethod,
    /** HTTPリクエストボディ */
    val body: ByteArray?,
    /** HTTPヘッダー */
    val headers: MutableMap<String, String>
)
