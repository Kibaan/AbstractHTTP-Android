package kibaan.android.abstracthttp.examples.mock

import abstracthttp.core.Connection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import kibaan.android.abstracthttp.ExampleItem
import kibaan.android.abstracthttp.commonspec.SimpleGetSpec
import kibaan.android.abstracthttp.examples.R

class MockFragment : Fragment(), ExampleItem {

    override val displayTitle: String
        get() = "通信処理のカスタマイズ・モック化"

    lateinit var textView: TextView

    private val mockHTTPConnector = MockHTTPConnector()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textView = view.findViewById(R.id.textView)
        view.findViewById<Button>(R.id.executeButton).setOnClickListener { executeButtonAction() }
    }

    override fun onResume() {
        super.onResume()
        clear()
    }

    fun executeButtonAction() {
        clear()

        val spec = SimpleGetSpec(url = "https://www.google.com/")
        val connection = Connection(spec) { response ->
            this.textView.text = response
        }
        connection.httpConnector = mockHTTPConnector
        connection.start()
    }

    private fun clear() {
        textView.text = null
    }
}
