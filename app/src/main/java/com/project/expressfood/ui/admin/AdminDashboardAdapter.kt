package com.project.expressfood.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.expressfood.R
import com.project.expressfood.databinding.ItemDashboardOrderBinding
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminDashboardAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, AdminDashboardAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemDashboardOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvOrderRef.text = "Pedido #${order.orderId.takeLast(6).uppercase()}"
            binding.tvOrderInfo.text = buildOrderInfo(order)
            applyStatusBadge(order.status)
            binding.root.setOnClickListener { onOrderClick(order) }
        }

        private fun buildOrderInfo(order: Order): String {
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = timeFmt.format(Date(order.date))
            val moneyFmt = NumberFormat.getNumberInstance(Locale("es", "CR")).apply {
                minimumFractionDigits = 0
                maximumFractionDigits = 0
            }
            return "$time · ₡${moneyFmt.format(order.totalPrice)}"
        }

        private fun applyStatusBadge(status: OrderStatus) {
            val ctx = binding.root.context
            val (bgRes, textColorRes, label) = when (status) {
                OrderStatus.PENDING -> Triple(
                    R.drawable.bg_status_pending,
                    R.color.status_pending_text,
                    "PENDIENTE"
                )
                OrderStatus.PREPARING -> Triple(
                    R.drawable.bg_status_preparing,
                    R.color.status_preparing_text,
                    "PREPARANDO"
                )
                OrderStatus.READY -> Triple(
                    R.drawable.bg_status_ready,
                    R.color.status_ready_text,
                    "LISTO"
                )
                OrderStatus.DELIVERED -> Triple(
                    R.drawable.bg_status_delivered,
                    R.color.status_delivered_text,
                    "ENTREGADO"
                )
                OrderStatus.CANCELLED -> Triple(
                    R.drawable.bg_status_cancelled,
                    R.color.status_cancelled_text,
                    "CANCELADO"
                )
            }
            binding.tvOrderStatus.background = ContextCompat.getDrawable(ctx, bgRes)
            binding.tvOrderStatus.setTextColor(ContextCompat.getColor(ctx, textColorRes))
            binding.tvOrderStatus.text = label
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order) =
            oldItem.orderId == newItem.orderId
        override fun areContentsTheSame(oldItem: Order, newItem: Order) =
            oldItem == newItem
    }
}
