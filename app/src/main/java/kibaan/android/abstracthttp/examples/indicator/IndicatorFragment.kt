package kibaan.android.abstracthttp.examples.indicator

import abstracthttp.core.Connection
import abstracthttp.defaultimpl.ConnectionIndicator
import abstracthttp.defaultimpl.DefaultHTTPConnector
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import kibaan.android.abstracthttp.commonspec.WaitableAPISpec
import kibaan.android.abstracthttp.examples.R

class IndicatorFragment : Fragment() {

    private lateinit var indicatorView: View
    private lateinit var textView: TextView

    private lateinit var indicator: ConnectionIndicator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_indicator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        indicatorView = view.findViewById(R.id.indicatorView)
        textView = view.findViewById(R.id.textView)
        indicator = ConnectionIndicator(view = indicatorView)

        view.findViewById<Button>(R.id.singleButton).setOnClickListener { singleButtonAction() }
        view.findViewById<Button>(R.id.errorButton).setOnClickListener { errorButtonAction() }
        view.findViewById<Button>(R.id.sequencialButton).setOnClickListener { sequencialButtonAction() }
        view.findViewById<Button>(R.id.parallelButton).setOnClickListener { parallelButtonAction() }
    }

    override fun onResume() {
        super.onResume()
        textView.text = null
    }

    private fun singleButtonAction() {
        val spec = WaitableAPISpec()
        clear()
        pushLine("[START] ${spec.url}")

        Connection(spec).addOnEnd { _, _, _ ->
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
        connection.addOnEnd { _, _, _ ->
            this.pushLine("[END  ] ${spec.url}")
        }.addListener(indicator).start()
    }

    private fun sequencialButtonAction() {
        // 直列で複数の通信を実行。全ての通信が完了したらインジケーターが消える
        clear()
        sequencialRequest(max = 3)
    }

    private fun sequencialRequest(max: Int, count: Int = 1) {
        pushLine("[START] Request $count")
        Connection(WaitableAPISpec(waitSeconds = 1)).addOnEnd { _, _, _ ->
            this.pushLine("[END  ] Request $count")
            if (count < max) {
                this.sequencialRequest(max = max, count = count + 1)
            }
        }.addListener(indicator).start()
    }

    private fun parallelButtonAction() {
        // 並列で複数の通信を実行。全ての通信が完了したらインジケーターが消える
        clear()

        pushLine("[START] Request 1")
        Connection(WaitableAPISpec(waitSeconds = 1)).addOnEnd { _, _, _ ->
            this.pushLine("[END  ] Request 1")
        }.addListener(indicator).start()

        pushLine("[START] Request 2")
        Connection(WaitableAPISpec(waitSeconds = 2)).addOnEnd { _, _, _ ->
            this.pushLine("[END  ] Request 2")
        }.addListener(indicator).start()

        pushLine("[START] Request 3")
        Connection(WaitableAPISpec(waitSeconds = 3)).addOnEnd { _, _, _ ->
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
