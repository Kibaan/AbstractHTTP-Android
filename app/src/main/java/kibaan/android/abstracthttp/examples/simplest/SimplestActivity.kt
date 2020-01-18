package kibaan.android.abstracthttp.examples.simplest

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import kibaan.android.abstracthttp.examples.R
import kibaan.android.abstracthttp.ExampleItem
import abstracthttp.core.Connection

class SimplestViewController : FragmentActivity(), ExampleItem {
    override val displayTitle: String
        get() = "最小構成"

    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simplest)

        textView = findViewById(R.id.textView)
        findViewById<Button>(R.id.connectButton).setOnClickListener {
            buttonAction(it)
        }
    }

    override fun onResume() {
        super.onResume()
        textView.text = null
    }

    fun buttonAction(sender: Any) {
        Connection(SimplestSpec()) { response ->
            this.textView.text = response
        }.start()
    }
}
