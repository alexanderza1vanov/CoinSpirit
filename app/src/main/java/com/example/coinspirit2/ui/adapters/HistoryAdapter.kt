package com.example.coinspirit2.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.databinding.ItemHistoryBinding
import com.example.coinspirit2.domain.model.TxDTO

class HistoryAdapter(
    private val onDelete: (TxDTO) -> Unit
) : ListAdapter<TxDTO, HistoryAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<TxDTO>() {
        override fun areItemsTheSame(o: TxDTO, n: TxDTO) = o.id == n.id
        override fun areContentsTheSame(o: TxDTO, n: TxDTO) = o == n
    }

    inner class VH(val b: ItemHistoryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = getItem(position)
        holder.b.tvHistoryItem.text =
            "${t.type}  |  ${t.symbol}  |  ${t.quantity} @ ${t.price}" + (t.note?.let { "  —  $it" } ?: "")
        holder.b.btnDeleteHistory.setOnClickListener { onDelete(t) }
    }
}
