package com.example.coinspirit2.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.R
import com.example.coinspirit2.data.remote.PositionDTO
import com.example.coinspirit2.databinding.ItemPortfolioRvBinding
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

class PortfolioAdapter(
    private val onClick: (PositionDTO) -> Unit
) : ListAdapter<PositionDTO, PortfolioAdapter.VH>(DIFF) {

    inner class VH(val b: ItemPortfolioRvBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemPortfolioRvBinding.inflate(inf, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = getItem(position)
        val ctx = holder.itemView.context

        val qty = p.quantity.bd()
        val avg = p.avgPrice.bd()
        val invested = p.invested.bd()
        val mkt = p.marketPrice.bd()
        val valueNow = qty * mkt
        val pnl = p.pnl.bd()

        holder.b.tvSymbol.text = p.symbol.uppercase()
        holder.b.tvQty.text = "${qty.stripT()} ${p.symbol.uppercase()}"

        holder.b.tvInvested.text = invested.prettyUSD()
        holder.b.tvAvgPrice.text = avg.prettyUSD()

        holder.b.tvNowValue.text = valueNow.prettyUSD()
        holder.b.tvPnl.text = pnl.signedUSD()

        val color = if (pnl.signum() >= 0) R.color.green_ok else R.color.red_bad
        holder.b.tvPnl.setTextColor(ContextCompat.getColor(ctx, color))

        holder.itemView.setOnClickListener { onClick(p) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PositionDTO>() {
            override fun areItemsTheSame(a: PositionDTO, b: PositionDTO) = a.symbol == b.symbol
            override fun areContentsTheSame(a: PositionDTO, b: PositionDTO) = a == b
        }
    }
}

// helpers
private fun String?.bd(): BigDecimal = try {
    if (this.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(this)
} catch (_: Throwable) { BigDecimal.ZERO }

private fun BigDecimal.prettyUSD(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2; minimumFractionDigits = 2
    }
    return nf.format(this.setScale(2, RoundingMode.HALF_UP))
}
private fun BigDecimal.signedUSD(): String {
    val sign = if (this.signum() >= 0) "+" else ""
    val nf = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2; minimumFractionDigits = 2
    }
    return "$sign${nf.format(this.abs())}"
}
private fun BigDecimal.stripT(): String =
    this.setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString()

private operator fun BigDecimal.times(other: BigDecimal) = this.multiply(other)
