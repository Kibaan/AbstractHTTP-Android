package kibaan.android.abstracthttp.default_impl

import kibaan.android.abstracthttp.core.URLEncoder


/**
 * URLエンコードの標準実装
 */
class DefaultURLEncoder : URLEncoder {

    override fun encode(text: String): String {
        return java.net.URLEncoder.encode(text, "UTF-8")
    }
}
