package kibaan.android.abstracthttp.examples.simplest

import abstracthttp.core.ConnectionSpec
import abstracthttp.entity.Response
import abstracthttp.entity.URLQuery
import abstracthttp.enumtype.HTTPMethod

/**
 * 最小構成のConnectionSpec実装
 *
 * GET取得したデータを文字列として返す
 */
class SimplestSpec : ConnectionSpec<String> {

    // リクエスト先のURL
    override val url: String
        get() = "https://reqres.in/api/users/2"
    // リクエストのHTTPメソッド
    override val httpMethod: HTTPMethod
        get() = HTTPMethod.get

    // 送信するリクエストヘッダー
    override val headers: Map<String, String>
        get() = mapOf()

    // URLに付けるクエリパラメーター（URL末尾の`?`以降につけるパラメーター）。不要な場合はnil。
    override val urlQuery: URLQuery?
        get() = null

    // ポストするデータ（リクエストボディ）。不要な場合はnil。
    override val body: ByteArray?
        get() = null

    // ステータスコードの正常判定
    override fun validate(response: Response): Boolean {
        return true
    }

    // 通信レスポンスをデータモデルに変換する
    override fun parseResponse(response: Response): String {
        return response.data.toString(Charsets.UTF_8)
    }
}
