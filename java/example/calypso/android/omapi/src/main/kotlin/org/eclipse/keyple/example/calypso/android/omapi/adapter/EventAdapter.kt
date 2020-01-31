package org.eclipse.keyple.example.calypso.android.omapi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card_action_event.view.*
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.example.calypso.android.omapi.model.EventModel

class EventAdapter(private val events: ArrayList<EventModel>): RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = when (viewType) {
            EventModel.TYPE_ACTION -> LayoutInflater.from(parent.context).inflate(R.layout.card_action_event, parent, false)
            EventModel.TYPE_RESULT -> LayoutInflater.from(parent.context).inflate(R.layout.card_result_event, parent, false)
            else -> LayoutInflater.from(parent.context).inflate(R.layout.card_header_event, parent, false)
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(events[position])
    }

    override fun getItemViewType(position: Int): Int {
        return events[position].type
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(event: EventModel){
            with(itemView){
                cardActionTextView.text = event.text
            }
        }

    }
}