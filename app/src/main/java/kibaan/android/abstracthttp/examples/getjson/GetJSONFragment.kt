package kibaan.android.abstracthttp.examples.getjson

import abstracthttp.core.Connection
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kibaan.android.abstracthttp.entity.User
import kibaan.android.abstracthttp.examples.R

class GetJSONFragment : Fragment() {

    lateinit var recyclerView: RecyclerView

    var response: List<User>
        get() {
            return (recyclerView.adapter as? RecyclerAdapter)?.data ?: listOf()
        }
        set(newValue) {
            (recyclerView.adapter as? RecyclerAdapter)?.data = newValue
            recyclerView.adapter?.notifyDataSetChanged()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_get_json, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.connectButton).setOnClickListener {
            buttonAction()
        }
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = RecyclerAdapter(activity!!, response)
    }

    private fun buttonAction() {
        Connection(GetJSONSpec()) { response ->
            this.response = response
        }.start()
    }

    inner class RecyclerAdapter(context: Context, var data: List<User>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.list_cell_getjson, parent, false))
        }

        override fun getItemCount(): Int {
            return data.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = data[position]
            holder.textView.text = "[${user.id}] (${user.username ?: ""})"
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var textView: TextView = itemView.findViewById(R.id.textView)
        }
    }
}
