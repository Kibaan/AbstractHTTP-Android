package kibaan.android.abstracthttp.examples.retry

import abstracthttp.android.core.Connection
import abstracthttp.android.defaultimpl.DefaultHTTPConnector
import android.annotation.SuppressLint
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
import kibaan.android.abstracthttp.examples.listener.ConnectionLogger

class RetryFragment : Fragment() {

    val exampleType: ExampleType = ExampleType.SIMPLEST

    lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_retry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView = view.findViewById(R.id.textView)
        view.findViewById<Button>(R.id.executeButton).setOnClickListener {
            executeButtonAction(it)
        }
    }

    override fun onResume() {
        super.onResume()
        clear()
    }

    private fun executeButtonAction(sender: Any) {
        clear()

        // タイムアウトさせる
        val spec = WaitableAPISpec(waitSeconds = 3)
        val connection = Connection(spec)
        (connection.httpConnector as? DefaultHTTPConnector)?.timeoutInterval = 1.0

        val listener = ConnectionLogger(print = ::pushLine)
        connection
            .addListener(listener)
            .addResponseListener(listener)
            .addErrorListener(listener)
            .addErrorListener(RetryRunner(context!!))
            .start()
    }


    private fun clear() {
        textView.text = null
    }

    @SuppressLint("SetTextI18n")
    private fun pushLine(text: String) {
        textView.text = textView.text.toString() + text + "\n"
    }
}
