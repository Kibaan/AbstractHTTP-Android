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
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kibaan.android.abstracthttp.ExampleItem
import kibaan.android.abstracthttp.entity.User
import kibaan.android.abstracthttp.examples.R


class GetJSONActivity : FragmentActivity(), ExampleItem {
    override val displayTitle: String
        get() = "JSON取得"

    lateinit var recyclerView: RecyclerView

    var response: List<User>
        get() {
            return (recyclerView.adapter as? RecyclerAdapter)?.data ?: listOf()
        }
        set(newValue) {
            (recyclerView.adapter as? RecyclerAdapter)?.data = newValue
            recyclerView.adapter?.notifyDataSetChanged()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getjson)

        findViewById<Button>(R.id.connectButton).setOnClickListener {
            buttonAction(it)
        }
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecyclerAdapter(this, response)
    }

    private fun buttonAction(sender: Any) {
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
