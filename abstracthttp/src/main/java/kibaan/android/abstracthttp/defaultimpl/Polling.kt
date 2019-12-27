package kibaan.android.abstracthttp.defaultimpl

import android.os.Handler
import kibaan.android.abstracthttp.core.Connection
import kibaan.android.abstracthttp.core.ConnectionListener
import kibaan.android.abstracthttp.entity.ConnectionError
import kibaan.android.abstracthttp.entity.Request
import kibaan.android.abstracthttp.entity.Response
import kibaan.android.abstracthttp.enumtype.ConnectionErrorType

class Polling(val delay: Long, val callback: () -> Unit) : ConnectionListener {

    var timer: Handler? = null

    override fun onStart(connection: Connection<*>, request: Request) {}

    override fun onEnd(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError?) {
        if (error == null || error.type == ConnectionErrorType.network) {
            timer = Handler()
            timer?.postDelayed(callback, delay)
        } else if (error.type == ConnectionErrorType.canceled) {
            timer?.removeCallbacks(callback)
        }
    }
}
