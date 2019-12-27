package kibaan.android.abstracthttp.enum_type

/**
 * 通信完了後イベントの続行・停止ステータス
 */
enum class EventChain(val rawValue: String) {
    /** 次の処理に進める */
    proceed("proceed"),
    /** 次の処理を停止する */
    stop("stop"),
    /** 同一フェーズの処理も含めただちに処理を停止する */
    stopImmediately("stopImmediately");
}