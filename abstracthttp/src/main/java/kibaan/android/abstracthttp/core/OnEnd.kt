package kibaan.android.abstracthttp.core

import kibaan.android.abstracthttp.entity.ConnectionError
import kibaan.android.abstracthttp.entity.Request
import kibaan.android.abstracthttp.entity.Response

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