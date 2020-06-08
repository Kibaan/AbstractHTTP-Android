package kibaan.android.abstracthttp

import abstracthttp.android.core.Connection
import abstracthttp.android.core.ConnectionHolder
import abstracthttp.android.core.ConnectionHolderListener
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kibaan.android.abstracthttp.examples.R

class MainActivity : AppCompatActivity(), ConnectionHolderListener {

    lateinit var countIcon: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolBar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolBar)
        countIcon = toolBar.findViewById(R.id.countIcon)

        ConnectionHolder.shared.addListener(this)
    }

    override fun onAdded(connection: Connection<*>, count: Int) {
        this.countIcon.text = count.toString()
    }

    override fun onRemoved(connection: Connection<*>, count: Int) {
        this.countIcon.text = count.toString()
    }
}