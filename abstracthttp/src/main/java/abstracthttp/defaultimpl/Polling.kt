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

    var timerTask: TimerTask? = null

    var connection: Connection<*>? = null

    override fun onStart(connection: Connection<*>, request: Request) {
        stop()
        this.connection = connection
    }

    override fun onEnd(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError?) {
        if (error == null || error.type == ConnectionErrorType.network) {
            timerTask = Timer().schedule(delaySeconds * 1000) {
                timerTask?.cancel()
                timerTask = null
                callback()
            }
        } else if (error.type == ConnectionErrorType.canceled) {
            timerTask?.cancel()
            timerTask = null
        }
        this.connection = null
    }

    fun stop() {
        timerTask?.cancel()
        timerTask = null
        connection?.cancel()
        connection = null
    }
}
