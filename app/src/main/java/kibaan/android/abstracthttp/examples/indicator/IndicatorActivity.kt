package kibaan.android.abstracthttp.examples.indicator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import kibaan.android.abstracthttp.examples.R
import kibaan.android.abstracthttp.ExampleItem
import abstracthttp.core.Connection
import abstracthttp.defaultimpl.ConnectionIndicator
import abstracthttp.defaultimpl.DefaultHTTPConnector
import android.annotation.SuppressLint
import android.view.View
import android.widget.ProgressBar
import kibaan.android.abstracthttp.commonspec.WaitableAPISpec

class IndicatorActivity : FragmentActivity(), ExampleItem {

    override val displayTitle: String
        get() = "通信インジケーターの表示"

    private lateinit var indicatorView: ProgressBar
    private lateinit var textView: TextView

    private lateinit var indicator: ConnectionIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indicator)

        indicatorView = findViewById(R.id.indicatorView)
        textView = findViewById(R.id.textView)
        indicator = ConnectionIndicator(view = indicatorView)

        findViewById<Button>(R.id.singleButton).setOnClickListener { singleButtonAction() }
        findViewById<Button>(R.id.errorButton).setOnClickListener { errorButtonAction() }
        findViewById<Button>(R.id.sequencialButton).setOnClickListener { sequencialButtonAction() }
        findViewById<Button>(R.id.parallelButton).setOnClickListener { parallelButtonAction() }
    }

    override fun onResume() {
        super.onResume()
        textView.text = null
    }

    private fun singleButtonAction() {
        val spec = WaitableAPISpec()
        clear()
        pushLine("[START] ${spec.url}")

        Connection(spec).addOnEnd { response, model, error ->
            this.pushLine("[END  ] ${spec.url}")
        }.addListener(indicator).start()
    }

    private fun errorButtonAction() {
        // APIで5秒待機するが1秒でタイムアウトさせる
        val spec = WaitableAPISpec(waitSeconds = 5)
        clear()
        pushLine("[START] ${spec.url}")

        val connection = Connection(spec)
        (connection.httpConnector as? DefaultHTTPConnector)?.timeoutInterval = 1.0
        connection.addOnEnd { response, model, error ->
            this.pushLine("[END  ] ${spec.url}")
        }.addListener(indicator).start()
    }

    private fun sequencialButtonAction() {
        // 直列で複数の通信を実行。全ての通信が完了したらインジケーターが消える
        clear()
        sequencialRequest(max = 3)
    }

    private fun sequencialRequest(max: Int, count: Int = 1) {
        pushLine("[START] Request ${count}")
        Connection(WaitableAPISpec(waitSeconds = 1)).addOnEnd { response, model, error ->
            this.pushLine("[END  ] Request ${count}")
            if (count < max) {
                this.sequencialRequest(max = max, count = count + 1)
            }
        }.addListener(indicator).start()
    }

    private fun parallelButtonAction() {
        // 並列で複数の通信を実行。全ての通信が完了したらインジケーターが消える
        clear()

        pushLine("[START] Request 1")
        Connection(WaitableAPISpec(waitSeconds = 1)).addOnEnd { response, model, error ->
            this.pushLine("[END  ] Request 1")
        }.addListener(indicator).start()

        pushLine("[START] Request 2")
        Connection(WaitableAPISpec(waitSeconds = 2)).addOnEnd { response, model, error ->
            this.pushLine("[END  ] Request 2")
        }.addListener(indicator).start()

        pushLine("[START] Request 3")
        Connection(WaitableAPISpec(waitSeconds = 3)).addOnEnd { response, model, error ->
            this.pushLine("[END  ] Request 3")
        }.addListener(indicator).start()
    }

    private fun clear() {
        textView.text = null
    }

    @SuppressLint("SetTextI18n")
    private fun pushLine(text: String) {
        textView.text = textView.text.toString() + text + "\n"
    }

}
