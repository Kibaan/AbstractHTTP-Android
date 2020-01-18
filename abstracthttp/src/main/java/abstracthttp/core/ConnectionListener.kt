package abstracthttp.core

import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Request
import abstracthttp.entity.Response


/**
 * 通信の開始と終了の通知を受け取るリスナー
 */
interface ConnectionListener {
    /**
     * 通信の開始イベント
     *
     * @param connection 通信オブジェクト
     * @param request HTTPリクエスト情報
     */
    fun onStart(connection: Connection<*>, request: Request)

    /**
     * 通信の終了イベント。通信の成否に関わらず終了時に必ず呼び出される。
     * 成功やエラーのコールバックが実行された後に呼び出されるため、
     * Connection.callbackInMainThreadが `true` の場合メインスレッドでの実行、`false` の場合バックグラウンドスレッドでの実行になる。
     * またキャンセル時はConnection.cancelの呼び出しスレッドでそのまま呼び出される。
     *
     * @param connection 通信オブジェクト
     * @param response HTTPレスポンス情報
     * @param responseModel パースしたレスポンスオブジェクト
     * @param error エラーの情報
     */
    fun onEnd(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError?)
}
