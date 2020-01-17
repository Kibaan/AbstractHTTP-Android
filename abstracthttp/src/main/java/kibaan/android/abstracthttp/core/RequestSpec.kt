package kibaan.android.abstracthttp.core

import kibaan.android.abstracthttp.enumtype.HTTPMethod
import kibaan.android.abstracthttp.entity.URLQuery

/**
 * HTTPリクエストの仕様
 * URL、HTTPメソッド、ヘッダー、パラメーターなどリクエストの内容を決める
 *
 * Specification of a HTTP request.
 * This specify contents of a request including url, HTTP method, headers and parameters.
 */
interface RequestSpec {
    /** リクエスト先のURL */
    val url: String
    /** HTTPメソッド */
    val httpMethod: HTTPMethod
    /** リクエストヘッダー */
    val headers: Map<String, String>
    /** URLに付与するクエリパラメーター */
    val urlQuery: URLQuery?

    /**
     * リクエストボディを作成する
     */
    fun makeBody(): ByteArray?
}
