package com.project.expressfood.ui.client.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.expressfood.R
import com.project.expressfood.databinding.ItemOrderBinding
import com.project.expressfood.domain.model.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersAdapter : ListAdapter<UiOrder, OrdersAdapter.OrderViewHolder>(DiffCallback) {

    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CR"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "CR"))

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uiOrder: UiOrder) {
            val order = uiOrder.order
            binding.tvOrderId.text = "#${order.orderId.takeLast(6).uppercase()}"
            binding.tvDateTime.text = "${dateFormat.format(Date(order.date))} - ${order.time}"
            binding.tvStatus.text = translateStatus(order.status)
            binding.tvStatus.setBackgroundResource(getStatusBackground(order.status))
            binding.tvDetails.text = uiOrder.itemsSummary
            binding.tvTotal.text = currency.format(order.totalPrice)

            if (order.synced) {
                binding.chipSyncStatus.text = "Sincronizada"
                binding.chipSyncStatus.setChipBackgroundColorResource(R.color.green_light)
                binding.chipSyncStatus.setChipIconResource(R.drawable.ic_offline) // Usar un icono de check si hay
                binding.chipSyncStatus.isChipIconVisible = false
            } else {
                binding.chipSyncStatus.text = "Pendiente de sincronizar"
                binding.chipSyncStatus.setChipBackgroundColorResource(R.color.amber_light)
                binding.chipSyncStatus.isChipIconVisible = true
            }
        }

        private fun translateStatus(status: OrderStatus): String {
            return when (status) {
                OrderStatus.PENDING -> "Pendiente"
                OrderStatus.PREPARING -> "En preparación"
                OrderStatus.READY -> "Lista"
                OrderStatus.DELIVERED -> "Entregada"
                OrderStatus.CANCELLED -> "Cancelada"
            }
        }

        private fun getStatusBackground(status: OrderStatus): Int {
            return when (status) {
                OrderStatus.PENDING -> R.drawable.bg_status_pending
                OrderStatus.PREPARING -> R.drawable.bg_status_preparing
                OrderStatus.READY -> R.drawable.bg_status_delivered
                OrderStatus.DELIVERED -> R.drawable.bg_status_delivered
                OrderStatus.CANCELLED -> R.drawable.bg_status_cancelled
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        OrderViewHolder(
            ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<UiOrder>() {
        override fun areItemsTheSame(old: UiOrder, new: UiOrder) = old.order.orderId == new.order.orderId
        override fun areContentsTheSame(old: UiOrder, new: UiOrder) = old == new
    }
}
