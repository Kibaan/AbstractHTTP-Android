package kibaan.android.abstracthttp.core


/**
 * HTTP通信のリクエストおよびレスポンスの仕様
 *
 * Specification of a HTTP request and response.
 */
interface ConnectionSpec<ResponseModel> : RequestSpec, ResponseSpec<ResponseModel>
