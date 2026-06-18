package com.project.expressfood.ui.client.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.expressfood.databinding.ItemOrderBinding
import com.project.expressfood.domain.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersAdapter : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(DiffCallback) {

    private val currency   = NumberFormat.getCurrencyInstance(Locale("es", "CR"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "CR"))

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvOrderDate.text  = dateFormat.format(Date(order.date)) + " · ${order.time}"
            binding.tvOrderStatus.text = statusLabel(order.status.name)
            binding.tvOrderTotal.text  = currency.format(order.totalPrice)

            if (order.synced) {
                binding.chipSyncStatus.text = "Sincronizada"
                binding.chipSyncStatus.setChipBackgroundColorResource(
                    com.project.expressfood.R.color.green_light
                )
            } else {
                binding.chipSyncStatus.text = "Pendiente de sincronizar"
                binding.chipSyncStatus.setChipBackgroundColorResource(
                    com.project.expressfood.R.color.amber_light
                )
            }
        }

        private fun statusLabel(status: String) = when (status) {
            "PENDING"   -> "Pendiente"
            "PREPARING" -> "En preparación"
            "READY"     -> "Lista"
            "DELIVERED" -> "Entregada"
            "CANCELLED" -> "Cancelada"
            else          -> status
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        OrderViewHolder(
            ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(old: Order, new: Order) = old.orderId == new.orderId
        override fun areContentsTheSame(old: Order, new: Order) = old == new
    }
}