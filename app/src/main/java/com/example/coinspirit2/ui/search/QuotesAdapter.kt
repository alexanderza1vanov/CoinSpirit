package com.example.coinspirit2.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.databinding.CurrencyRvItemBinding

class QuotesAdapter(
    private val onClick: (QuoteUi) -> Unit
) : RecyclerView.Adapter<QuotesAdapter.VH>() {

    private val data = mutableListOf<QuoteUi>()

    fun submit(list: List<QuoteUi>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: CurrencyRvItemBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = CurrencyRvItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val q = data[position]
        holder.b.TVSymbol.text = q.symbol
        holder.b.TVName.text = q.name ?: q.symbol
        holder.b.TVCurrencyRate.text = q.price ?: "—"
        holder.itemView.setOnClickListener { onClick(q) }
    }

    override fun getItemCount() = data.size
}
