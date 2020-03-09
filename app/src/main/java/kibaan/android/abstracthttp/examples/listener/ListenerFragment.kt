package kibaan.android.abstracthttp.examples.listener

import abstracthttp.core.Connection
import abstracthttp.core.ConnectionResponseListener
import abstracthttp.defaultimpl.DefaultHTTPConnector
import abstracthttp.entity.Response
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import kibaan.android.abstracthttp.commonspec.WaitableAPISpec
import kibaan.android.abstracthttp.examples.R

class ListenerFragment : Fragment() {

    lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_listener, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textView = view.findViewById(R.id.textView)
        view.findViewById<Button>(R.id.successButton).setOnClickListener { successAction() }
        view.findViewById<Button>(R.id.timeoutButton).setOnClickListener { timeoutAction() }
        view.findViewById<Button>(R.id.cancelButton).setOnClickListener { cancelAction() }
        view.findViewById<Button>(R.id.interruptButton).setOnClickListener { interruptAction() }
    }

    override fun onResume() {
        super.onResume()
        clear()
    }

    // 通信成功
    private fun successAction() {
        val listener = ConnectionLogger(print = ::pushLine)

        clear()
        Connection(WaitableAPISpec()) { response ->
            this.pushLine("(SUCCESS callback)")
        }
            .addListener(listener)
            .addResponseListener(listener)
            .addErrorListener(listener)
            .start()
    }

    // 通信タイムアウト
    private fun timeoutAction() {

        clear()
        val connection = Connection(WaitableAPISpec(waitSeconds = 3)) { response ->
            this.pushLine("(SUCCESS callback)")
        }

        (connection.httpConnector as? DefaultHTTPConnector)?.timeoutInterval = 1.0

        val listener = ConnectionLogger(print = ::pushLine)
        connection
            .addListener(listener)
            .addResponseListener(listener)
            .addOnError { _, _, _ ->
                this.pushLine("OnError before error listener")
            }
            .addErrorListener(listener)
            .addOnError { _, _, _ ->
                this.pushLine("OnError after error listener")
            }
            .start()
    }

    // 通信キャンセル
    private fun cancelAction() {

        clear()
        val connection = Connection(WaitableAPISpec(waitSeconds = 3)) { response ->
            this.pushLine("(SUCCESS callback)")
        }

        val listener = ConnectionLogger(print = ::pushLine)
        connection
            .addListener(listener)
            .addResponseListener(listener)
            .addErrorListener(listener)
            .start()

        Handler().postDelayed({ connection.cancel() }, 500)
    }

    private fun interruptAction() {
        clear()

        val connection = Connection(WaitableAPISpec(waitSeconds = 1)) { response ->
            this.pushLine("(SUCCESS callback)")
        }

        val logger = ConnectionLogger(print = ::pushLine)
        connection
            .addListener(logger)
            .addResponseListener(logger)
            .addErrorListener(logger)
            .addResponseListener(ResponseInterruptor())
            .start()
    }

    private fun clear() {
        textView.text = null
    }

    @SuppressLint("SetTextI18n")
    private fun pushLine(text: String) {
        this.textView.text = this.textView.text.toString() + text + "\n"
    }
}

class ResponseInterruptor : ConnectionResponseListener {

    override fun onReceived(connection: Connection<*>, response: Response): Boolean {
        connection.interrupt()
        return true
    }

    override fun onReceivedModel(connection: Connection<*>, responseModel: Any?): Boolean = true

    override fun afterSuccess(connection: Connection<*>, responseModel: Any?) {}
}

