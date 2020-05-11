package abstracthttp.android.defaultimpl

import abstracthttp.android.core.Connection
import abstracthttp.android.core.ConnectionListener
import abstracthttp.android.entity.ConnectionError
import abstracthttp.android.entity.Request
import abstracthttp.android.entity.Response
import abstracthttp.android.enumtype.ConnectionErrorType
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
