package kibaan.android.abstracthttp

import kibaan.android.abstracthttp.examples.R

enum class ExampleType : ExampleItem {
    SIMPLEST,
    GET_JSON,
    COMMON_REQUEST_SPEC,
    INDICATOR,
    LISTENER,
    RETRY,
    MOCK,
    POLLING,
    CANCEL,
    TOKEN_REFRESH,
    CONVENIENT;

    override val displayTitle: String
        get() = when (this) {
            SIMPLEST -> "最小構成"
            GET_JSON -> "JSON取得"
            COMMON_REQUEST_SPEC -> "リクエスト仕様の共通化"
            INDICATOR -> "通信インジケーターの表示"
            LISTENER -> "各種リスナーのサンプル"
            RETRY -> "通信のリトライ"
            MOCK -> "通信処理のカスタマイズ・モック化"
            POLLING -> "ポーリング（自動更新）"
            CANCEL -> "通信キャンセル"
            TOKEN_REFRESH -> "トークンリフレッシュ"
            CONVENIENT -> "簡易インターフェース"
        }

    override val actionId: Int
        get() = when (this) {
            SIMPLEST -> R.id.action_to_simplest
            GET_JSON -> R.id.action_to_get_json
            COMMON_REQUEST_SPEC -> R.id.action_to_common_request_spec
            INDICATOR -> R.id.action_to_indicator
            LISTENER -> R.id.action_to_listener
            RETRY -> R.id.action_to_retry
            MOCK -> R.id.action_to_mock
            POLLING -> R.id.action_to_polling
            CANCEL -> R.id.action_to_cancel
            TOKEN_REFRESH -> R.id.action_to_token_refresh
            CONVENIENT -> R.id.action_to_convenient
        }
}