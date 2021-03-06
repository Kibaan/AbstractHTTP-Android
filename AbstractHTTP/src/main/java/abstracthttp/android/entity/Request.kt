package abstracthttp.android.entity

import abstracthttp.android.enumtype.HTTPMethod
import java.net.URL

/**
 * HTTPのリクエスト
 */
class Request(
    /** リクエストするURL */
    val url: URL,
    /** HTTPリクエストメソッド */
    val method: HTTPMethod,
    /** HTTPリクエストボディ */
    val body: ByteArray?,
    /** HTTPヘッダー */
    val headers: MutableMap<String, String>
)
