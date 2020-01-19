package abstracthttp.core

import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Response

/**
 * 通信エラーを受け取るリスナー。
 * afterError以外のエラーコールバックは、Connectionに渡したエラーコールバックの実行前にバックグラウンドスレッドで呼ばれる。
 *
 * バックグラウンドスレッドから呼び出されるため、UIの操作を行う場合はメインスレッドに切り替える必要がある
 */
interface ConnectionErrorListener {
    /**
     * 通信エラー時に呼ばれる
     *
     * @param connection 通信オブジェクト
     * @param error エラー情報
     */
    fun onNetworkError(connection: Connection<*>, error: Exception?)

    /**
     * レスポンス内容のパース前のバリデーションエラー時に呼ばれる。
     * 具体的には、ResponseSpec.validate で `false` が返却された場合に呼ばれる
     *
     * @param connection 通信オブジェクト
     * @param response HTTPレスポンスの情報
     */
    fun onResponseError(connection: Connection<*>, response: Response)

    /**
     * パースエラー時に呼ばれる
     *
     * @param connection 通信オブジェクト
     * @param response HTTPレスポンスの情報
     * @param error エラー情報
     */
    fun onParseError(connection: Connection<*>, response: Response, error: Exception)

    /**
     * レスポンスモデルのバリデーションエラー時に呼ばれる。
     * 具体的には、ConnectionResponseListener.onReceivedModel で `false` が返却された場合に呼ばれる
     *
     * @param connection 通信オブジェクト
     * @param response HTTPレスポンスの情報
     * @param responseModel パースされたレスポンスデータモデル
     */
    fun onValidationError(connection: Connection<*>, response: Response, responseModel: Any)

    /**
     * Connection.startの引数に渡したエラーコールバックの実行直後に呼ばれる
     * Connection.callbackInMainThread がtrueの場合はメインスレッド、falseの場合はバックグラウンドスレッドから呼ばれる
     *
     * @param connection 通信オブジェクト
     * @param response HTTPレスポンスの情報
     * @param responseModel パースされたレスポンスデータモデル
     * @param error エラー情報
     */
    fun afterError(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError)

    /**
     * 通信キャンセル時に呼ばれる
     *
     * @param connection 通信オブジェクト
     */
    fun onCanceled(connection: Connection<*>)
}
