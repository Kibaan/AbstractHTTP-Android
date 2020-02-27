package kibaan.android.abstracthttp.examples.listener

import abstracthttp.core.Connection
import abstracthttp.core.ConnectionErrorListener
import abstracthttp.core.ConnectionListener
import abstracthttp.core.ConnectionResponseListener
import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Request
import abstracthttp.entity.Response
import java.lang.Exception


/**
 * 通信リスナーの各種イベントでログ出力する
 */
class ConnectionLogger : ConnectionListener, ConnectionResponseListener, ConnectionErrorListener {
    val printFunc: (String) -> Unit

    constructor(print: (String) -> Unit) {
        this.printFunc = print
    }

    private fun print(text: String) {
        this.printFunc(text)
    }

    override fun onStart(connection: Connection<*>, request: Request) {
        print("onStart")
    }

    override fun onEnd(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError?) {
        print("onEnd")
    }

    override fun onReceived(connection: Connection<*>, response: Response): Boolean {
        print("onReceived")
        return true
    }

    override fun onReceivedModel(connection: Connection<*>, responseModel: Any?): Boolean {
        print("onReceivedModel")
        return true
    }

    override fun afterSuccess(connection: Connection<*>, responseModel: Any?) {
        print("afterSuccess")
    }

    override fun onNetworkError(connection: Connection<*>, error: Exception?) {
        print("onNetworkError")
    }

    override fun onResponseError(connection: Connection<*>, response: Response) {
        print("onResponseError ${response.statusCode}")
    }

    override fun onParseError(connection: Connection<*>, response: Response, error: Exception) {
        print("onParseError")
    }

    override fun onValidationError(connection: Connection<*>, response: Response, responseModel: Any) {
        print("onValidationError")
    }

    override fun afterError(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError) {
        print("afterError")
    }

    override fun onCanceled(connection: Connection<*>) {
        print("onCanceled")
    }
}
