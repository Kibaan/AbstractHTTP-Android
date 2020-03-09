package kibaan.android.abstracthttp.examples.commonrequestspec

import abstracthttp.core.Connection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import kibaan.android.abstracthttp.ExampleItem
import kibaan.android.abstracthttp.examples.R

class CommonRequestSpecFragment : Fragment() {

    lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_common_request_spec, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textView = view.findViewById(R.id.textView)
        view.findViewById<Button>(R.id.button1).setOnClickListener { button1Action() }
        view.findViewById<Button>(R.id.button2).setOnClickListener { button2Action() }
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
