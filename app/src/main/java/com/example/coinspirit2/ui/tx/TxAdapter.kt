package com.example.coinspirit2.ui.tx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.R
import com.example.coinspirit2.data.remote.TxDTO
import com.example.coinspirit2.databinding.ItemTxRowBinding
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

class TxAdapter(
    private val onEdit: (TxDTO) -> Unit,
    private val onDelete: (TxDTO) -> Unit
) : ListAdapter<TxDTO, TxAdapter.VH>(DIFF) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    inner class VH(val b: ItemTxRowBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemTxRowBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val t = getItem(position)
        val price = t.price.bd()
        val qty = t.quantity.bd()
        val total = price * qty

        val isBuy = t.type.equals("BUY", ignoreCase = true)
        h.b.tvType.text = if (isBuy) "Покупка ${t.symbol}" else "Продажа ${t.symbol}"
        h.b.tvNote.text = t.note ?: ""
        h.b.tvPriceQty.text = "${price.prettyUSD()} • ${qty.stripT()}"

        val signed = if (isBuy) "+${total.prettyUSD()}" else "-${total.prettyUSD()}"
        h.b.tvAmountSigned.text = signed
        val color = ContextCompat.getColor(
            h.itemView.context,
            if (isBuy) R.color.green_ok else R.color.red_bad
        )
        h.b.tvAmountSigned.setTextColor(color)

        h.b.btnMore.setOnClickListener { v -> showMenu(v, t) }
    }

    private fun showMenu(anchor: View, t: TxDTO) {
        PopupMenu(anchor.context, anchor).apply {
            menu.add(0, 1, 0, "Редактировать")
            menu.add(0, 2, 1, "Удалить")
            setOnMenuItemClickListener {
                when (it.itemId) {
                    1 -> onEdit(t)
                    2 -> onDelete(t)
                }
                true
            }
        }.show()
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<TxDTO>() {
            override fun areItemsTheSame(a: TxDTO, b: TxDTO) = a.id == b.id
            override fun areContentsTheSame(a: TxDTO, b: TxDTO) = a == b
        }
    }
}

/* helpers */
private fun String?.bd(): BigDecimal = try {
    if (this.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(this)
} catch (_: Throwable) { BigDecimal.ZERO }

private operator fun BigDecimal.times(o: BigDecimal) = this.multiply(o)

private fun BigDecimal.stripT(): String =
    this.setScale(8, RoundingMode.DOWN).stripTrailingZeros().toPlainString()

private fun BigDecimal.prettyUSD(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2; minimumFractionDigits = 2
    }
    return nf.format(this.setScale(2, RoundingMode.HALF_UP))
}