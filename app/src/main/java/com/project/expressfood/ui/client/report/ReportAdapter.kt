package com.project.expressfood.ui.client.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.expressfood.databinding.ItemReportDayBinding
import com.project.expressfood.databinding.ItemReportOrderBinding
import com.project.expressfood.domain.model.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAdapter(
    private val onDayClick: (String) -> Unit,
    private val formatCurrency: (Double) -> String,
) : ListAdapter<ReportListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_DAY_HEADER = 0
        private const val TYPE_ORDER_ROW  = 1
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ReportListItem.DayHeader -> TYPE_DAY_HEADER
        is ReportListItem.OrderRow  -> TYPE_ORDER_ROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DAY_HEADER -> DayHeaderViewHolder(ItemReportDayBinding.inflate(inflater, parent, false))
            else            -> OrderRowViewHolder(ItemReportOrderBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ReportListItem.DayHeader -> (holder as DayHeaderViewHolder).bind(item)
            is ReportListItem.OrderRow  -> (holder as OrderRowViewHolder).bind(item)
        }
    }

    inner class DayHeaderViewHolder(private val binding: ItemReportDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReportListItem.DayHeader) {
            binding.tvDate.text      = item.dateLabel
            binding.tvDayTotal.text  = formatCurrency(item.dayTotal)
            binding.tvOrderCount.text = "${item.orderCount} pedido${if (item.orderCount != 1) "s" else ""}"
            binding.ivArrow.rotation = if (item.isExpanded) 180f else 0f
            binding.root.setOnClickListener { onDayClick(item.dateLabel) }
        }
    }

    inner class OrderRowViewHolder(private val binding: ItemReportOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(item: ReportListItem.OrderRow) {
            val order = item.order
            binding.tvOrderId.text    = "#${order.orderId.takeLast(8).uppercase()}"
            binding.tvOrderTime.text  = order.time.ifBlank { timeFormat.format(Date(order.date)) }
            binding.tvOrderTotal.text = formatCurrency(order.totalPrice)
            binding.tvOrderStatus.text = order.status.displayName()
            binding.tvOrderStatus.setBackgroundResource(order.status.colorRes())
        }

        private fun OrderStatus.displayName() = when (this) {
            OrderStatus.PENDING   -> "Pendiente"
            OrderStatus.PREPARING -> "En camino"
            OrderStatus.READY     -> "Listo"
            OrderStatus.DELIVERED -> "Entregada"
            OrderStatus.CANCELLED -> "Cancelada"
        }

        private fun OrderStatus.colorRes() = when (this) {
            OrderStatus.PENDING   -> com.project.expressfood.R.drawable.bg_status_pending
            OrderStatus.PREPARING -> com.project.expressfood.R.drawable.bg_status_preparing
            OrderStatus.READY     -> com.project.expressfood.R.drawable.bg_status_preparing
            OrderStatus.DELIVERED -> com.project.expressfood.R.drawable.bg_status_delivered
            OrderStatus.CANCELLED -> com.project.expressfood.R.drawable.bg_status_cancelled
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReportListItem>() {
        override fun areItemsTheSame(old: ReportListItem, new: ReportListItem) = when {
            old is ReportListItem.DayHeader && new is ReportListItem.DayHeader -> old.dateLabel == new.dateLabel
            old is ReportListItem.OrderRow  && new is ReportListItem.OrderRow  -> old.order.orderId == new.order.orderId
            else -> false
        }
        override fun areContentsTheSame(old: ReportListItem, new: ReportListItem) = old == new
    }
}
