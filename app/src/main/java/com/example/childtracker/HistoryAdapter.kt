package com.example.childtracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val items: List<String>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.dateText)
        val coordsText: TextView = view.findViewById(R.id.coordsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val raw = items[position]

        val parts = raw.split("|")
        if (parts.size == 2) {
            holder.dateText.text = parts[0]
            holder.coordsText.text = parts[1]
        }
    }

    override fun getItemCount(): Int = items.size
}
