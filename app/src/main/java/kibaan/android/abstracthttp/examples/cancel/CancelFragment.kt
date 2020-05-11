package kibaan.android.abstracthttp.examples.cancel

import abstracthttp.android.core.Connection
import abstracthttp.android.core.ConnectionHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import kibaan.android.abstracthttp.ExampleType
import kibaan.android.abstracthttp.commonspec.WaitableAPISpec
import kibaan.android.abstracthttp.examples.R

class CancelFragment : Fragment() {

    val exampleType: ExampleType = ExampleType.SIMPLEST

    private var latestConnection: Connection<*>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cancel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.connectButton).setOnClickListener { startConnection(it) }
        view.findViewById<Button>(R.id.allCancelButton).setOnClickListener { allCancel(it) }
        view.findViewById<Button>(R.id.singleCancelButton).setOnClickListener { singleCancel(it) }
    }

    private fun startConnection(@Suppress("UNUSED_PARAMETER") sender: Any) {
        val connection = Connection(WaitableAPISpec(waitSeconds = 3))
        connection.start()
        latestConnection = connection
    }

    private fun allCancel(@Suppress("UNUSED_PARAMETER") sender: Any) {
        // すべての通信をキャンセル
        ConnectionHolder.shared.cancelAll()
    }

    private fun singleCancel(@Suppress("UNUSED_PARAMETER") sender: Any) {
        // 直近の通信をキャンセル
        latestConnection?.cancel()
    }

}
