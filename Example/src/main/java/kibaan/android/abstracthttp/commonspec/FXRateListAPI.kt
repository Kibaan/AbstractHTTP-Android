package kibaan.android.abstracthttp.commonspec

import abstracthttp.android.core.ConnectionSpec
import abstracthttp.android.entity.Response
import abstracthttp.android.entity.URLQuery
import abstracthttp.android.enumtype.HTTPMethod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FXRateListAPI : ConnectionSpec<FXRateList> {
    override val url: String
        get() = "https://www.gaitameonline.com/rateaj/getrate"

    override val httpMethod: HTTPMethod
        get() = HTTPMethod.get

    override val headers: Map<String, String>
        get() = mapOf()

    override val urlQuery: URLQuery?
        get() = null

    override val body: ByteArray?
        get() = null

    override fun validate(response: Response): Boolean = response.statusCode == 200

    override fun parseResponse(response: Response): FXRateList {
        val json = response.data.toString(Charsets.UTF_8)
        val type = TypeToken.getParameterized(FXRateList::class.java).type
        return Gson().fromJson(json, type)
    }
}

data class FXRateList(val quotes: List<FXRate>) {}

data class FXRate(
    val currencyPairCode: String,
    val open: String?,
    val high: String?,
    val low: String?,
    val bid: String?,
    val ask: String?
) {
    val stringValue: String
        get() {
            val bidText = bid ?: "--"
            val askText = ask ?: "--"
            return "[${currencyPairCode}]  $bidText - $askText"
        }
}
