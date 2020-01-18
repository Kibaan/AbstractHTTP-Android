package abstracthttp.enumtype

/**
 * HTTP通信のエラー種別
 */
enum class ConnectionErrorType {
    /** 不正なURL */
    invalidURL,

    /** オフライン、タイムアウトなどのネットワークエラー */
    network,

    /** レスポンスデータが規定ではない */
    invalidResponse,

    /** レスポンスのパースに失敗 */
    parse,

    /** バリデーションエラー */
    validation,

    /** キャンセル */
    canceled;

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
