package abstracthttp.android.core

import abstracthttp.android.entity.Response

/**
 * 通信のレスポンスを受け取るリスナー。主に複数の通信で共通の後処理を行うために使う。
 * （個別の通信完了処理は Connection.connect の引数のコールバックで処理する）
 * レスポンスのバリデーターの役割も兼ねており、`onReceived` 、`onReceivedModel` の返り値はエラー判定に用いられる
 *
 * バックグラウンドスレッドから呼び出されるため、UIの操作を行う場合はメインスレッドに切り替える必要がある
 */
interface ConnectionResponseListener {

    /**
     * レスポンスデータの受信イベント
     *
     * @param connection 通信オブジェクト
     * @param response 通信レスポンスデータ
     *
     * @return レスポンスデータが正常の場合 `true`、エラーの場合 `false`
     */
    fun onReceived(connection: Connection<*>, response: Response): Boolean

    /**
     * レスポンスデータモデルの受信イベント
     * `ConnectionResponseSpec`で作られたデータモデルを処理する
     *
     * @param connection 通信オブジェクト
     * @param responseModel 通信レスポンスデータモデル。
     *
     * @return レスポンスデータモデルが正常の場合 `true`、エラーの場合 `false`
     */
    fun onReceivedModel(connection: Connection<*>, responseModel: Any?): Boolean

    /**
     * 成功コールバック実行直後のイベント
     *
     * @param connection 通信オブジェクト
     * @param responseModel 通信レスポンスデータモデル。
     */
    fun afterSuccess(connection: Connection<*>, responseModel: Any?)
}
