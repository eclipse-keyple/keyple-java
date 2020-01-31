package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.eclipse.keyple.core.seproxy.SeProxyService
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.example.calypso.android.omapi.adapter.EventAdapter
import org.eclipse.keyple.example.calypso.android.omapi.model.EventModel
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import timber.log.Timber
import java.util.*

abstract class ExamplesActivity: BasicActivity() {

    /**
     * Variables for event window
     */
    protected lateinit var readers : SortedSet<SeReader>
    protected lateinit var adapter: RecyclerView.Adapter<*>
    protected lateinit var layoutManager: RecyclerView.LayoutManager
    protected val events= arrayListOf<EventModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = EventAdapter(events)
        layoutManager = LinearLayoutManager(this)

        /**
         * Get OMAPI Readers
         */
        readers = SeProxyService.getInstance().getPlugin(AndroidOmapiPlugin.PLUGIN_NAME).readers
    }

    protected fun clearEvents(){
        events.clear()
        adapter.notifyDataSetChanged()
    }

    protected fun addHeaderEvent(message: String){
        events.add(EventModel(EventModel.TYPE_HEADER, message))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Header: %s", message)
    }

    protected fun addActionEvent(message: String){
        events.add(EventModel(EventModel.TYPE_ACTION, message))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Action: %s", message)
    }

    protected fun addResultEvent(message: String){
        events.add(EventModel(EventModel.TYPE_RESULT, message))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Result: %s", message)
    }
}