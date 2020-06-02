package kibaan.android.abstracthttp.examples.tokenrefresh

import abstracthttp.android.core.Connection
import abstracthttp.android.core.ConnectionErrorListener
import abstracthttp.android.entity.ConnectionError
import abstracthttp.android.entity.Response
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

class TokenRefreshFragment : Fragment() {

    private var tokenContainer = TokenContainer()
    private val tokenRefresher by lazy {
        TokenRefresher(tokenContainer = tokenContainer, print = ::pushLine)
    }
    private val badTokenRefresher by lazy {
        TokenRefresher(tokenContainer = tokenContainer, fail = true, print = ::pushLine)
    }
    private val logger by lazy {
        ConnectionLogger(print = ::pushLine)
    }

    lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_token_refresh, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView = view.findViewById(R.id.textView)
        view.findViewById<Button>(R.id.callAPIActionButton).setOnClickListener { callAPIAction(it) }
        view.findViewById<Button>(R.id.failRefreshActionButton).setOnClickListener { failRefreshAction(it) }
    }

    override fun onResume() {
        super.onResume()
        clear()
    }

    private fun callAPIAction(@Suppress("UNUSED_PARAMETER") sender: Any) {
        clear()
        Connection(ExampleAPISpec(tokenContainer = tokenContainer)).addErrorListener(tokenRefresher).addListener(logger).addResponseListener(logger).addErrorListener(logger).start()
    }

    private fun failRefreshAction(@Suppress("UNUSED_PARAMETER") sender: Any) {
        clear()
        Connection(ExampleAPISpec(tokenContainer = tokenContainer)).addErrorListener(badTokenRefresher).addListener(logger).addResponseListener(logger).addErrorListener(logger).start()
    }

    private fun clear() {
        textView.text = null
    }

    @SuppressLint("SetTextI18n")
    private fun pushLine(text: String) {
        this.textView.text = this.textView.text.toString() + text + "\n"
    }
}

/**
 * 401エラーが発生したらトークンを取得して自動リトライする
 */
class TokenRefresher(val tokenContainer: TokenContainer, val fail: Boolean = false, print: (String) -> Unit) : ConnectionErrorListener {
    // 無限ループ防止のためのカウンター
    private var errorCount = 0
    val printFunc: (String) -> Unit = print

    override fun onResponseError(connection: Connection<*>, response: Response) {
        if (response.statusCode != 401 || errorCount >= 3) {
            return
        }
        errorCount += 1
        printFunc("401エラー発生、トークン再取得。")
        connection.interrupt()
        Connection(GetTokenSpec(fail = fail)) { token ->
            this.printFunc("トークン取得完了、再通信。")
            this.tokenContainer.token = token
            connection.start()
        }.addOnError { _, _, _ ->
            this.printFunc("トークン取得失敗")
            connection.breakInterruption()
        }.start()
    }

    override fun onNetworkError(connection: Connection<*>, error: Exception?) {}

    override fun onParseError(connection: Connection<*>, response: Response, error: Exception) {}

    override fun onValidationError(connection: Connection<*>, response: Response, responseModel: Any) {}

    override fun afterError(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError) {}

    override fun onCanceled(connection: Connection<*>) {}
}

class TokenContainer {
    var token: String? = null
}