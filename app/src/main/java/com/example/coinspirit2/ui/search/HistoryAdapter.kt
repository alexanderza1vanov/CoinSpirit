// app/src/main/java/com/example/coinspirit2/ui/search/HistoryAdapter.kt
package com.example.coinspirit2.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.databinding.ItemHistorySimpleBinding

class HistoryAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    private val data = mutableListOf<String>()
    fun submit(list: List<String>) { data.clear(); data.addAll(list); notifyDataSetChanged() }
    fun getAt(pos: Int) = data[pos]
    fun removeAt(pos: Int) { data.removeAt(pos); notifyItemRemoved(pos) }

    inner class VH(val b: ItemHistorySimpleBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemHistorySimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }
    override fun getItemCount() = data.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        val term = data[position]
        holder.b.root.findViewById<TextView>(android.R.id.text1).text = term
        holder.itemView.setOnClickListener { onClick(term) }
    }
}
