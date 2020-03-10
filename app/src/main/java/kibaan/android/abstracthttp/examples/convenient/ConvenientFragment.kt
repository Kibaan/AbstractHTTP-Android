package kibaan.android.abstracthttp.examples.convenient

import abstracthttp.convenient.HTTP
import abstracthttp.entity.URLQuery
import abstracthttp.enumtype.HTTPMethod
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import kibaan.android.abstracthttp.examples.R
import kibaan.android.abstracthttp.examples.listener.ConnectionLogger

class ConvenientFragment : Fragment() {

    lateinit var responseTextView: TextView
    lateinit var logTextView: TextView
    private val logger by lazy {
        ConnectionLogger(print = ::pushLine)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_convenient, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        responseTextView = view.findViewById(R.id.responseTextView)
        logTextView = view.findViewById(R.id.logTextView)
        view.findViewById<Button>(R.id.minimumGetActionButton).setOnClickListener { minimumGetAction(it) }
        view.findViewById<Button>(R.id.listenerActionButton).setOnClickListener { listenerAction(it) }
        view.findViewById<Button>(R.id.postActionButton).setOnClickListener { postAction(it) }
        view.findViewById<Button>(R.id.timeoutActionButton).setOnClickListener { timeoutAction(it) }
    }

    override fun onResume() {
        super.onResume()
        clear()
    }

    private fun minimumGetAction(@Suppress("UNUSED_PARAMETER") sender: Any) {
        clear()
        HTTP("https://reqres.in/api/users/2").asString {
            this.printResponse(it)
        }
    }

    private fun listenerAction(@Suppress("UNUSED_PARAMETER") sender: Any) {
        clear()
        HTTP("https://reqres.in/api/users/2")
            .addListener(logger)
            .addResponseListener(logger)
            .addErrorListener(logger)
            .asString {
                this.pushLine("(SUCCESS callback)")
                this.printResponse(it)
            }
    }

    private fun postAction(@Suppress("UNUSED_PARAMETER") sender: Any) {
        clear()
        val body = """
            {
                "name": "yamamoto",
                "job": "ceo"
            }
        """.toByteArray(Charsets.UTF_8)
        HTTP("https://reqres.in/api/users")
            .httpMethod(HTTPMethod.post)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(body)
            .asString {
                this.printResponse(it)
            }
    }

    private fun timeoutAction(@Suppress("UNUSED_PARAMETER") sender: Any) {
        clear()
        HTTP("https://apidemo.altonotes.co.jp/timeout-test")
            .urlQuery(URLQuery("waitSeconds" to "2"))
            .setupDefaultHTTPConnector {
                it.timeoutInterval = 0.5
            }
            .addListener(logger)
            .addResponseListener(logger)
            .addOnError { _, _, _ ->
                this.pushLine("OnError before error listener")
            }
            .addErrorListener(logger)
            .addOnError { _, _, _ ->
                this.pushLine("OnError after error listener")
            }
            .asString {
                this.printResponse(it)
            }
    }

    private fun clear() {
        responseTextView.text = null
        logTextView.text = null
    }

    @SuppressLint("SetTextI18n")
    private fun printResponse(text: String) {
        this.responseTextView.text = this.responseTextView.text.toString() + text
    }

    @SuppressLint("SetTextI18n")
    private fun pushLine(text: String) {
        this.logTextView.text = this.logTextView.text.toString() + text + "\n"
    }
}