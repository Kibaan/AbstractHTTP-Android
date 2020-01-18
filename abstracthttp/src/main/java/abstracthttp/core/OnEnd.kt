package abstracthttp.core

import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Request
import abstracthttp.entity.Response

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