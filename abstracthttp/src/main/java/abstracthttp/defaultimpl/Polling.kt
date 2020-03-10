package abstracthttp.defaultimpl

import abstracthttp.core.Connection
import abstracthttp.core.ConnectionListener
import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Request
import abstracthttp.entity.Response
import abstracthttp.enumtype.ConnectionErrorType
import android.os.Handler
import java.util.*
import kotlin.concurrent.schedule

class Polling(val delaySeconds: Long, val callback: () -> Unit) : ConnectionListener {

    var handler = Handler()
    var runnable = Runnable { callback() }

    var connection: Connection<*>? = null

    override fun onStart(connection: Connection<*>, request: Request) {
        stop()
        this.connection = connection
    }

    override fun onEnd(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError?) {
        if (error == null || error.type == ConnectionErrorType.network) {
            handler.postDelayed(runnable, delaySeconds * 1000)
        } else if (error.type == ConnectionErrorType.canceled) {
            handler.removeCallbacks(runnable)
        }
        this.connection = null
    }

    fun stop() {
        handler.removeCallbacks(runnable)
        connection?.cancel()
        connection = null
    }
}
