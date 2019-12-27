package kibaan.android.abstracthttp.examples.simplest

import kibaan.android.abstracthttp.core.ConnectionSpec
import kibaan.android.abstracthttp.entity.Response
import kibaan.android.abstracthttp.entity.URLQuery
import kibaan.android.abstracthttp.enumtype.HTTPMethod

/**
 * 最小構成のConnectionSpec実装
 *
 * GET取得したデータを文字列として返す
 */
class SimplestSpec : ConnectionSpec<String> {

    // リクエスト先のURL
    override val url: String
        get() = "https://altonotes.co.jp/md/coding_guideline.md"

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
    override fun makePostData(): ByteArray? {
        return null
    }

    // ステータスコードの正常判定
    override fun isValidResponse(response: Response): Boolean {
        return true
    }

    override fun parseResponse(response: Response): String {
        return response.data.toString(Charsets.UTF_8)
    }
}
