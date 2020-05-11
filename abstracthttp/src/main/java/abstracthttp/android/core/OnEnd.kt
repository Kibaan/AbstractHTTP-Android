package abstracthttp.android.core

import abstracthttp.android.entity.ConnectionError
import abstracthttp.android.entity.Request
import abstracthttp.android.entity.Response

class OnEnd: ConnectionListener {

    val onEndFunc: (Response?, Any?, ConnectionError?) -> Unit

    constructor(onEnd: (Response?, Any?, ConnectionError?) -> Unit) {
        this.onEndFunc = onEnd
    }

    override fun onStart(connection: Connection<*>, request: Request) {}

    override fun onEnd(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError?) {
        onEndFunc(response, responseModel, error)
    }
}