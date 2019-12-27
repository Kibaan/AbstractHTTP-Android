package kibaan.android.abstracthttp.entity

/**
 * HTTPレスポンスの情報
 * レスポンスボディの他、ステータスコード、ヘッダーの情報を持つ
 */
data class Response(
    /** レスポンスデータ */
    val data: ByteArray,
    /** HTTPステータスコード */
    val statusCode: Int,
    /** レスポンスヘッダー */
    val headers: Map<String, String>,
    // TODO:Androidの場合は？
    /**
     * ネイティブSDKのレスポンスオブジェクト。
     * iOSの場合、URLResponseが入る。
     */
    val nativeResponse: Any?
)
