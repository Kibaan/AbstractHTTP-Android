package kibaan.android.abstracthttp.examples.mock

import abstracthttp.core.Connection
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import kibaan.android.abstracthttp.ExampleItem
import kibaan.android.abstracthttp.commonspec.SimpleGetSpec
import kibaan.android.abstracthttp.examples.R

class MockActivity : FragmentActivity(), ExampleItem {

    override val displayTitle: String
        get() = "通信処理のカスタマイズ・モック化"

    lateinit var textView: TextView

    private val mockHTTPConnector = MockHTTPConnector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mock)

        textView = findViewById(R.id.textView)
        findViewById<Button>(R.id.executeButton).setOnClickListener { executeButtonAction() }
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
