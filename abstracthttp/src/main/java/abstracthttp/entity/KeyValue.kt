package abstracthttp.entity

import abstracthttp.core.URLEncoder

/**
 * キーと値のペア
 * クエリパラメーターを構築するのに使う
 */
data class KeyValue(val key: String, val value: String?) {

    /**
     * "キー=値" 形式の文字列。値が `nil` の場合はキーのみになり=がつかない。
     */
    val stringValue: String
        get() {
            val value = value
            return if (value != null) {
                "${key}=${value}"
            } else {
                key
            }
        }

    /**
     * キーと値をURLEncodeして、キー=値の文字列にする
     */
    fun encodedValue(encoder: URLEncoder): String {
        var item = encoder.encode(key)
        val value = value
        if (value != null) {
            item += "=${encoder.encode(value)}"
        }
        return item
    }
}
