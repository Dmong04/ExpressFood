package com.project.expressfood.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.expressfood.R
import com.project.expressfood.databinding.ItemAdminOrderBinding
import com.project.expressfood.domain.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminOrdersAdapter(
    private val onStatusUpdateClick: (OrderWithClient) -> Unit
) : ListAdapter<OrderWithClient, AdminOrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemAdminOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemAdminOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: OrderWithClient) {
            val order = item.order
            binding.tvOrderId.text = "#${order.orderId.takeLast(6).uppercase()}"
            binding.tvClientName.text = item.clientName
            binding.tvTotal.text = "$${String.format(Locale.getDefault(), "%.2f", order.totalPrice)}"
            
            // Format Date
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date(order.date))
            binding.tvDateTime.text = "$dateStr - ${order.time}"

            // Status UI
            binding.tvStatus.text = translateStatus(order.status)
            binding.tvStatus.setBackgroundResource(getStatusBackground(order.status))

            binding.btnUpdateStatus.setOnClickListener { onStatusUpdateClick(item) }
        }

        private fun translateStatus(status: OrderStatus): String {
            return when (status) {
                OrderStatus.PENDING -> "Pendiente"
                OrderStatus.PREPARING -> "Preparando"
                OrderStatus.READY -> "Listo"
                OrderStatus.DELIVERED -> "Entregado"
                OrderStatus.CANCELLED -> "Cancelado"
            }
        }

        private fun getStatusBackground(status: OrderStatus): Int {
            return when (status) {
                OrderStatus.PENDING -> R.drawable.bg_status_pending
                OrderStatus.PREPARING -> R.drawable.bg_status_preparing
                OrderStatus.READY -> R.drawable.bg_status_delivered // Usando delivered para ready por ahora
                OrderStatus.DELIVERED -> R.drawable.bg_status_delivered
                OrderStatus.CANCELLED -> R.drawable.bg_status_cancelled
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderWithClient>() {
        override fun areItemsTheSame(oldItem: OrderWithClient, newItem: OrderWithClient): Boolean {
            return oldItem.order.orderId == newItem.order.orderId
        }

        override fun areContentsTheSame(oldItem: OrderWithClient, newItem: OrderWithClient): Boolean {
            return oldItem == newItem
        }
    }
}
