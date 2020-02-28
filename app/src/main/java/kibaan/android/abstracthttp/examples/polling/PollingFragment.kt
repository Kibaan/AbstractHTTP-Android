package kibaan.android.abstracthttp.examples.polling

import abstracthttp.core.Connection
import abstracthttp.defaultimpl.Polling
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kibaan.android.abstracthttp.ExampleItem
import kibaan.android.abstracthttp.commonspec.FXRateList
import kibaan.android.abstracthttp.commonspec.FXRateListAPI
import kibaan.android.abstracthttp.examples.R
import java.text.SimpleDateFormat
import java.util.*

class PollingFragment : Fragment(), ExampleItem {

    override val displayTitle: String
        get() = "ポーリング（自動更新）"

    lateinit var timeLabel: TextView
    lateinit var recyclerView: RecyclerView

    private val polling by lazy {
        Polling(delaySeconds = 1, callback = ::request)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_polling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeLabel = view.findViewById(R.id.timeLabel)
        timeLabel.text = null

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = RecyclerAdapter(activity!!, rateList = null)
    }

    override fun onResume() {
        super.onResume()
        request()
    }

    override fun onPause() {
        super.onPause()
        polling.stop()
    }

    private fun request() {
        Connection(FXRateListAPI()) { response ->
            this.update(rateList = response)
        }.addListener(polling).start()
    }

    private fun update(rateList: FXRateList) {
        (recyclerView.adapter as? RecyclerAdapter)?.rateList = rateList
        recyclerView.adapter?.notifyDataSetChanged()
        val dateFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)
        timeLabel.text = dateFormatter.format(Date())
    }

    inner class RecyclerAdapter(context: Context, var rateList: FXRateList?) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.list_cell_getjson, parent, false))
        }

        override fun getItemCount(): Int {
            return rateList?.quotes?.size ?: 0
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val quote = rateList?.quotes?.get(position)
            holder.textView.text = quote?.stringValue
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var textView: TextView = itemView.findViewById(R.id.textView)
        }
    }
}
