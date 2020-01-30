package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_core_examples.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.eclipse.keyple.core.seproxy.SeProxyService
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.example.calypso.android.omapi.adapter.EventAdapter
import org.eclipse.keyple.example.calypso.android.omapi.model.EventModel
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import java.util.*

class CoreExamplesActivity : BasicActivity() {

    lateinit var readers : SortedSet<SeReader>

    private val events= arrayListOf<EventModel>()
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var layoutManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core_examples)
        initActionBar(toolbar,"keyple-core", "Shows usage of Keyple Core")

        layoutManager = LinearLayoutManager(this)
        adapter = EventAdapter(events)

        eventRecyclerView.layoutManager = layoutManager
        eventRecyclerView.adapter = adapter


        readers = SeProxyService.getInstance().getPlugin(AndroidOmapiPlugin.PLUGIN_NAME).readers

        //What to do with core?
    }

    override fun onResume() {
        super.onResume()
        events.add(EventModel(0, "Readers Found: " + 2))
    }
}
