package abstracthttp.android.entity

import abstracthttp.android.core.URLEncoder
import java.net.URLDecoder

/**
 * key=valueの&つなぎ形式のクエリ
 *
 * URLのクエリパラメーターを構築するのに用いる
 */
@Suppress("unused")
open class URLQuery {

    var keyValues: MutableList<KeyValue> = mutableListOf()

    constructor()

    constructor(string: String?) {
        val string = string?.trim() ?: return
        keyValues = string.components(separatedBy = "&").filter { it.isNotEmpty() }.map {
            val pairs = it.components(separatedBy = "=")
            val key = pairs[0].removingPercentEncoding
            val value = if (1 < pairs.size) pairs[1].removingPercentEncoding else null
            KeyValue(key = key, value = value)
        }.toMutableList()
    }

    constructor(vararg elements: Pair<String, String?>) {
        keyValues = elements.map { KeyValue(key = it.first, value = it.second) }.toMutableList()
    }

    constructor(keyValues: List<KeyValue>) {
        this.keyValues = keyValues.toMutableList()
    }

    operator fun get(key: String): String? {
        return keyValues.firstOrNull { it.key == key }?.value
    }

    operator fun set(key: String, value: String?) {
        val keyValue = KeyValue(key = key, value = value)
        val offset = keyValues.indexOfFirst { it.key == key }
        if (offset != -1) {
            keyValues[offset] = keyValue
        } else {
            keyValues.add(keyValue)
        }
    }

    fun stringValue(encoder: URLEncoder): String =
        keyValues.joinToString(separator = "&") { it.encodedValue(encoder = encoder) }

    private val String.removingPercentEncoding: String
        get() = URLDecoder.decode(this, "UTF-8")

    private fun String.components(separatedBy: String): List<String> {
        return split(separatedBy)
    }
}