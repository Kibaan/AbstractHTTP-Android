package abstracthttp.defaultimpl

import abstracthttp.core.Connection
import abstracthttp.core.ConnectionListener
import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Request
import abstracthttp.entity.Response
import abstracthttp.enumtype.ConnectionErrorType
import java.util.*
import kotlin.concurrent.schedule

class Polling(val delaySeconds: Long, val callback: () -> Unit) : ConnectionListener {
    var timer: TimerTask? = null

    var connection: Connection<*>? = null

    override fun onStart(connection: Connection<*>, request: Request) {
        this.connection?.cancel()
        this.connection = connection
    }

    override fun onEnd(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError?) {
        if (error == null || error.type == ConnectionErrorType.network) {
            timer = Timer().schedule(0, delaySeconds * 1000) {
                timer = null
                callback()
            }
        } else if (error.type == ConnectionErrorType.canceled) {
            timer?.cancel()
            timer = null
        }
        this.connection = null
    }

    fun stop() {
        timer?.cancel()
        timer = null
        connection?.cancel()
        connection = null
    }
}
