package kibaan.android.abstracthttp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kibaan.android.abstracthttp.examples.R

class MainFragment : Fragment() {

    lateinit var recyclerView: RecyclerView

    private val exampleList: List<ExampleType> = listOf(
        ExampleType.SIMPLEST,
        ExampleType.GET_JSON,
        ExampleType.COMMON_REQUEST_SPEC,
        ExampleType.INDICATOR,
        ExampleType.LISTENER,
        ExampleType.RETRY,
        ExampleType.MOCK,
        ExampleType.POLLING,
        ExampleType.CANCEL,
        ExampleType.TOKEN_REFRESH
    )
//    ConvenientViewController()
//    ]

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = RecyclerAdapter(activity!!, exampleList) {
            findNavController().navigate(it.actionId)
        }
    }

    inner class RecyclerAdapter(context: Context, var data: List<ExampleType>, val onItemClick: ((ExampleType) -> Unit)) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val viewHolder = ViewHolder(inflater.inflate(R.layout.list_cell_main, parent, false))
            viewHolder.itemView.setOnClickListener {
                val position = viewHolder.adapterPosition
                val example = data[position]
                onItemClick(example)
            }
            return viewHolder
        }

        override fun getItemCount(): Int {
            return data.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val exampleItem = data[position]
            holder.textView.text = exampleItem.displayTitle
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var textView: TextView = itemView.findViewById(R.id.textView)
        }
    }
}