package kibaan.android.abstracthttp.core

import kibaan.android.abstracthttp.entity.Request
import kibaan.android.abstracthttp.entity.Response


/**
 * HTTP通信の実行処理
 */
interface HTTPConnector {

    /**
     * HTTP通信を行う
     *
     * @param request URL、HTTPメソッド、ヘッダー、パラメーターなどを含むリクエスト情報
     * @param complete 通信完了時に実行されるコールバック。コールバックの引数にはレスポンスの情報と発生したエラーを渡す
     */
    fun execute(request: Request, complete: (Response?, Exception?) -> Unit)

    /**
     * 実行中の通信をキャンセルする
     */
    fun cancel()
}
