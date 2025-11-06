package com.example.coinspirit2.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.R

class HistoryRVAdapter(
    private val historyList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<HistoryRVAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val historyItem: TextView = itemView.findViewById(R.id.tvHistoryItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.historyItem.text = historyItem
        holder.itemView.setOnClickListener { onItemClick(historyItem) }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}