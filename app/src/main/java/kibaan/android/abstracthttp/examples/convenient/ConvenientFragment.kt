package kibaan.android.abstracthttp.examples.convenient

import abstracthttp.convenient.HTTP
import abstracthttp.entity.URLQuery
import abstracthttp.enumtype.HTTPMethod
import android.annotation.SuppressLint
import android.widget.TextView
import androidx.fragment.app.Fragment
import kibaan.android.abstracthttp.examples.listener.ConnectionLogger

class ConvenientFragment : Fragment() {

    lateinit var responseTextView: TextView
    lateinit var logTextView: TextView
    private val logger by lazy {
        ConnectionLogger(print = ::pushLine)
    }

    override fun onResume() {
        super.onResume()
        clear()
    }

    fun minimumGetAction(sender: Any) {
        clear()
        HTTP("https://reqres.in/api/users/2").asString {
            this.printResponse(it)
        }
    }

    fun listenerAction(sender: Any) {
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

    fun jsonAction(sender: Any) {
        clear()
        // TODO JSONのパースをどうする？
//        HTTP("https://jsonplaceholder.typicode.com/users/1")
//            .asDecodable(type = User.self) { user ->
//                this.printResponse(user.stringValue)
//            }
    }

    fun postAction(sender: Any) {
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

    fun timeoutAction(sender: Any) {
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