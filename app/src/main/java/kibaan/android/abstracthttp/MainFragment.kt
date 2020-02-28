package kibaan.android.abstracthttp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kibaan.android.abstracthttp.examples.R

class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button1).setOnClickListener { findNavController().navigate(R.id.action_to_simplest) }
        view.findViewById<Button>(R.id.button2).setOnClickListener { findNavController().navigate(R.id.action_to_get_json) }
        view.findViewById<Button>(R.id.button3).setOnClickListener { findNavController().navigate(R.id.action_to_common_request_spec) }
        view.findViewById<Button>(R.id.button4).setOnClickListener { findNavController().navigate(R.id.action_to_indicator) }
        view.findViewById<Button>(R.id.button5).setOnClickListener { findNavController().navigate(R.id.action_to_listener) }
//        view.findViewById<Button>(R.id.button6).setOnClickListener { findNavController().navigate(R.id.action_to_listener) }
        view.findViewById<Button>(R.id.button7).setOnClickListener { findNavController().navigate(R.id.action_to_mock) }
        view.findViewById<Button>(R.id.button8).setOnClickListener { findNavController().navigate(R.id.action_to_polling) }
    }
}