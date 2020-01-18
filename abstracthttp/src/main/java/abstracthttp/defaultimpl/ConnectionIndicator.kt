package abstracthttp.defaultimpl

import android.view.View
import abstracthttp.core.Connection
import abstracthttp.core.ConnectionListener
import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Request
import abstracthttp.entity.Response

/**
 * 通信インジケーター。
 * インジケーターは複数の通信で使われることを想定して、
 * 単純な表示/非表示の切り替えではなく、参照カウントを増減してカウントが0になったら非表示にする方式にする。
 */
class ConnectionIndicator(val view: View) : ConnectionListener {
    var referenceCount = 0
        private set

    override fun onStart(connection: Connection<*>, request: Request) {
        referenceCount += 1
        connection.runOnUiThread {
            this.updateView()
        }
    }

    override fun onEnd(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError?) {
        referenceCount -= 1
        connection.runOnUiThread {
            this.updateView()
        }
    }

    fun updateView() {
        view.visibility = if (referenceCount <= 0) View.INVISIBLE else View.VISIBLE
    }
}
