package abstracthttp.android.core


/**
 * 文字列をURLエンコードする。
 * URLのクエリパラメーターのエンコードに使われる。
 *
 * クエリパラメーターのエンコード方法は標準仕様が存在するが、標準仕様に従っていないシステムもあるためプロトコルにして拡張可能にしている。
 */
interface URLEncoder {
    fun encode(text: String): String
}
