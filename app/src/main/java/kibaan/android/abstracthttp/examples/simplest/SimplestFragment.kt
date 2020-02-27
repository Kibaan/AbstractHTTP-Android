package kibaan.android.abstracthttp.examples.simplest

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

class SimplestViewController : Fragment(), ExampleItem {

    override val displayTitle: String
        get() = "最小構成"

    lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_simplest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView = view.findViewById(R.id.textView)
        view.findViewById<Button>(R.id.connectButton).setOnClickListener {
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
