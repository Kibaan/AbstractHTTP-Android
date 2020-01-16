package kibaan.android.abstracthttp.core

import kibaan.android.abstracthttp.entity.ConnectionError
import kibaan.android.abstracthttp.entity.Response
import kibaan.android.abstracthttp.enumtype.ConnectionErrorType
import kibaan.android.abstracthttp.enumtype.ConnectionErrorType.*


/**
 * Connection.addOnError により追加されるエラー処理
 */
class OnError<ResponseModel>: ConnectionErrorListener {
    val onError: (ConnectionError, Response?, ResponseModel?) -> Unit

    constructor(onError: (ConnectionError, Response?, ResponseModel?) -> Unit) {
        this.onError = onError
    }

    override fun onNetworkError(connection: Connection<*>, error: Exception?) {
        callError(type = network, nativeError = error)
    }

    override fun onResponseError(connection: Connection<*>, response: Response) {
        callError(type = invalidResponse, response = response)
    }

    override fun onParseError(connection: Connection<*>, response: Response, error: Exception) {
        callError(type = parse, nativeError = error, response = response)
    }

    override fun onValidationError(connection: Connection<*>, response: Response, responseModel: Any) {
        callError(type = validation, response = response, responseModel = responseModel)
    }

    override fun onCanceled(connection: Connection<*>) {
        callError(type = canceled)
    }

    override fun afterError(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError) {}

    private fun callError(type: ConnectionErrorType, nativeError: Exception? = null, response: Response? = null, responseModel: Any? = null) {
        val error = ConnectionError(type = type, nativeError = nativeError)
        onError(error, response, responseModel as? ResponseModel)
    }
}
