package kibaan.android.abstracthttp.enum_type

/**
 * HTTP通信のエラー種別
 */
sealed class ConnectionErrorType : Exception() {
    /** 不正なURL */
    object invalidURL : ConnectionErrorType()

    /** オフライン、タイムアウトなどのネットワークエラー */
    object network : ConnectionErrorType()

    /** レスポンスデータが規定ではない */
    object invalidResponse : ConnectionErrorType()

    /** レスポンスのパースに失敗 */
    object parse : ConnectionErrorType()

    /** バリデーションエラー */
    object validation : ConnectionErrorType()

    /** キャンセル */
    object canceled : ConnectionErrorType()

    val description: String
        get() {
            return when (this) {
                invalidURL -> "リクエスト先のURLが不正です。"
                network -> "通信エラーが発生しました。 通信環境が不安定か、接続先が誤っている可能性があります。"
                invalidResponse -> "レスポンスデータが不正です。"
                parse -> "レスポンスデータのパースに失敗しました。"
                validation -> "バリデーションエラーです。"
                canceled -> "通信がキャンセルされました。"
            }
        }
}
