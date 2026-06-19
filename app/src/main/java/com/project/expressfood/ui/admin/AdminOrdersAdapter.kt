package com.project.expressfood.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.expressfood.R
import com.project.expressfood.databinding.ItemAdminOrderBinding
import com.project.expressfood.databinding.ItemOrderDetailSubitemBinding
import com.project.expressfood.domain.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminOrdersAdapter(
    private val onStatusUpdateClick: (OrderWithClient) -> Unit
) : ListAdapter<OrderWithClient, AdminOrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    private var expandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemAdminOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val isExpanded = position == expandedPosition
        holder.bind(getItem(position), isExpanded)
    }

    inner class OrderViewHolder(private val binding: ItemAdminOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: OrderWithClient, isExpanded: Boolean) {
            val order = item.order
            binding.tvOrderId.text = "#${order.orderId.takeLast(6).uppercase()}"
            binding.tvClientName.text = item.clientName
            binding.tvTotal.text = "₡${String.format(Locale.getDefault(), "%,.2f", order.totalPrice)}"
            

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date(order.date))
            binding.tvDateTime.text = "$dateStr - ${order.time}"


            binding.tvStatus.text = translateStatus(order.status)
            binding.tvStatus.setBackgroundResource(getStatusBackground(order.status))


            binding.layoutDetails.visibility = if (isExpanded) android.view.View.VISIBLE else android.view.View.GONE
            binding.ivArrow.rotation = if (isExpanded) 180f else 0f


            if (isExpanded) {
                binding.containerOrderItems.removeAllViews()
                item.itemsSummary.forEach { detailUi ->
                    val itemBinding = ItemOrderDetailSubitemBinding.inflate(
                        LayoutInflater.from(binding.root.context),
                        binding.containerOrderItems,
                        true
                    )
                    itemBinding.tvQuantity.text = "${detailUi.quantity}x "
                    itemBinding.tvProductName.text = detailUi.productName
                    itemBinding.tvPrice.text = "₡${String.format(Locale.getDefault(), "%,.2f", detailUi.price)}"
                }
                
                if (item.itemsSummary.isEmpty()) {

                    val emptyBinding = ItemOrderDetailSubitemBinding.inflate(
                        LayoutInflater.from(binding.root.context),
                        binding.containerOrderItems,
                        true
                    )
                    emptyBinding.tvProductName.text = "Sin detalles disponibles"
                    emptyBinding.tvQuantity.text = ""
                    emptyBinding.tvPrice.text = ""
                }
            }

            binding.layoutHeader.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val prevExpanded = expandedPosition
                    expandedPosition = if (isExpanded) -1 else currentPosition
                    
                    if (prevExpanded != -1) notifyItemChanged(prevExpanded)
                    if (expandedPosition != -1) notifyItemChanged(expandedPosition)
                }
            }

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
                OrderStatus.READY -> R.drawable.bg_status_delivered
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
