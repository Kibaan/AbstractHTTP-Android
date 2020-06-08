package kibaan.android.abstracthttp.examples.cancel

import abstracthttp.android.core.*
import abstracthttp.android.entity.ConnectionError
import abstracthttp.android.entity.Request
import abstracthttp.android.entity.Response
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import kibaan.android.abstracthttp.ExampleType
import kibaan.android.abstracthttp.commonspec.WaitableAPISpec
import kibaan.android.abstracthttp.examples.R

class CancelFragment : Fragment(), ConnectionListener, ConnectionResponseListener, ConnectionErrorListener {

    val exampleType: ExampleType = ExampleType.SIMPLEST

    lateinit var textView: TextView

    private var latestConnection: Connection<*>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cancel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textView = view.findViewById(R.id.textView)
        view.findViewById<Button>(R.id.connectButton).setOnClickListener { startConnection(it) }
        view.findViewById<Button>(R.id.allCancelButton).setOnClickListener { allCancel(it) }
        view.findViewById<Button>(R.id.singleCancelButton).setOnClickListener { singleCancel(it) }
        view.findViewById<Button>(R.id.clearButton).setOnClickListener { clearAction(it) }

        clear()
    }

    private fun startConnection(@Suppress("UNUSED_PARAMETER") sender: Any) {
        val connection = Connection(WaitableAPISpec(waitSeconds = 3))
        connection.addListener(this)
        connection.addResponseListener(this)
        connection.addErrorListener(this)

        connection.start()
        latestConnection = connection
    }

    private fun allCancel(@Suppress("UNUSED_PARAMETER") sender: Any) {
        // すべての通信をキャンセル
        ConnectionHolder.shared.cancelAll()
    }

    private fun singleCancel(@Suppress("UNUSED_PARAMETER") sender: Any) {
        // 直近の通信をキャンセル
        latestConnection?.cancel()
    }

    private fun clearAction(@Suppress("UNUSED_PARAMETER") sender: Any) {
        clear()
    }

    private fun clear() {
        textView.text = null
    }

    private fun pushLine(text: String) {
        view?.handler?.post {
            textView.text = "${textView.text}${text}\n"
        }
    }

    // ConnectionErrorListener ------------
    override fun onNetworkError(connection: Connection<*>, error: Exception?) {
        pushLine("onNetworkError")
    }

    override fun onResponseError(connection: Connection<*>, response: Response) {
        pushLine("onResponseError")
    }

    override fun onParseError(connection: Connection<*>, response: Response, error: Exception) {
        pushLine("onParseError")
    }

    override fun onValidationError(connection: Connection<*>, response: Response, responseModel: Any) {
        pushLine("onValidationError")
    }

    override fun afterError(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError) {
        pushLine("afterError")
    }

    override fun onCanceled(connection: Connection<*>) {
        pushLine("onCanceled")
    }

    // ConnectionListener ------------

    override fun onStart(connection: Connection<*>, request: Request) {
        pushLine("onStart")
    }

    override fun onEnd(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError?) {
        pushLine("onEnd")
    }

    // ConnectionResponseListener ------------

    override fun onReceived(connection: Connection<*>, response: Response): Boolean {
        pushLine("onReceived")
        return true
    }

    override fun onReceivedModel(connection: Connection<*>, responseModel: Any?): Boolean {
        pushLine("onReceivedModel")
        return true
    }

    override fun afterSuccess(connection: Connection<*>, responseModel: Any?) {
        pushLine("afterSuccess")
    }
}
