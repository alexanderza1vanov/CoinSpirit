package com.example.coinspirit2.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.databinding.ItemSearchRowBinding

class SearchAdapter(
    private val onClick: (QuoteUi) -> Unit
) : RecyclerView.Adapter<SearchAdapter.VH>() {

    private val data = mutableListOf<QuoteUi>()

    fun submit(list: List<QuoteUi>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemSearchRowBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemSearchRowBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = data[position]
        h.b.tvSymbol.text = item.symbol
        h.b.tvName.text   = item.name ?: ""
        h.b.tvPrice.text  = item.price ?: ""
        h.itemView.setOnClickListener { onClick(item) } // <-- фикс: передаём элемент, а не View
    }

    override fun getItemCount(): Int = data.size
}