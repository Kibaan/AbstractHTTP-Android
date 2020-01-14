package kibaan.android.abstracthttp.defaultimpl

import kibaan.android.abstracthttp.core.Connection
import kibaan.android.abstracthttp.core.ConnectionListener
import kibaan.android.abstracthttp.entity.ConnectionError
import kibaan.android.abstracthttp.entity.Request
import kibaan.android.abstracthttp.entity.Response
import kibaan.android.abstracthttp.enumtype.ConnectionErrorType
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
