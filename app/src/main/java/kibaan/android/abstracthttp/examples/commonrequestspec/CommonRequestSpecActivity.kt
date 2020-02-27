package kibaan.android.abstracthttp.examples.commonrequestspec

import abstracthttp.core.Connection
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import kibaan.android.abstracthttp.ExampleItem
import kibaan.android.abstracthttp.examples.R
import kibaan.android.abstracthttp.examples.simplest.SimplestSpec

class CommonRequestSpecActivity : FragmentActivity(), ExampleItem {

    override val displayTitle: String
        get() = "リクエスト仕様の共通化"

    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_request_spec)

        textView = findViewById(R.id.textView)
        findViewById<Button>(R.id.button1).setOnClickListener { button1Action() }
        findViewById<Button>(R.id.button2).setOnClickListener { button2Action() }
    }

    override fun onResume() {
        super.onResume()
        textView.text = null
    }

    private fun button1Action() {
        Connection(Sub1RequestSpec(userId = 1)) { response ->
            this.textView.text = response.stringValue
        }.start()
    }


    private fun button2Action() {
        val spec = Sub2RequestSpec(postId = 1)
        Connection(spec) { response ->
            this.textView.text = response.stringValue
        }.start()
    }

}
