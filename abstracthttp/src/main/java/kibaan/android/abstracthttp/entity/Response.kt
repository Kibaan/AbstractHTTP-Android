package kibaan.android.abstracthttp.entity

/**
 * HTTPレスポンスの情報
 * レスポンスボディの他、ステータスコード、ヘッダーの情報を持つ
 */
class Response(
    /** レスポンスデータ */
    val data: ByteArray,
    /** HTTPステータスコード */
    val statusCode: Int,
    /** レスポンスヘッダー */
    val headers: Map<String, String>,
    /**
     * ネイティブSDKのレスポンスオブジェクト。
     * HTTPConnectorで任意のレスポンスオブジェクトをセットすることが出来る
     */
    val nativeResponse: Any?
)
