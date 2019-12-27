package kibaan.android.abstracthttp.core

import kibaan.android.abstracthttp.entity.Response

/**
 * HTTPレスポンスの仕様
 * HTTPレスポンスをassociated typeに指定された型に変換する
 * また、HTTPステータスコードを見てエラーかどうかを判定する
 */
interface ResponseSpec<ResponseModel> {

    /**
     * パース前のレスポンスデータのバリデーションを行う
     * `false` を返すとエラーのコールバックが呼ばれる
     * 200系以外のHTTPステータスコードを弾いたりするのに使う場合が多い
     *
     * @param response HTTPのレスポンス情報
     * @return レスポンスデータが正常の場合 `true`、エラーの場合 `false`
     */
    fun isValidResponse(response: Response): Boolean

    /**
     * HTTPレスポンスをassociated typeに指定した型に変換する
     * 変換に失敗した場合、何らかのErrorをthrowするとパースエラーとして扱われる
     */
    fun parseResponse(response: Response): ResponseModel
}
