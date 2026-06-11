package com.project.expressfood.ui.client.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.expressfood.databinding.ItemCartBinding
import com.project.expressfood.domain.model.CartItem
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val onIncrement : (CartItem) -> Unit,
    private val onDecrement : (CartItem) -> Unit,
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback) {

    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CR"))

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvCartItemName.text  = item.itemId  // se reemplaza con nombre real en siguiente tarea
            binding.tvCartItemPrice.text = currency.format(item.unitPrice)
            binding.tvQuantity.text      = item.quantity.toString()

            binding.btnIncrement.setOnClickListener { onIncrement(item) }
            binding.btnDecrement.setOnClickListener { onDecrement(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CartViewHolder(
            ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(old: CartItem, new: CartItem) =
            old.cartItemId == new.cartItemId
        override fun areContentsTheSame(old: CartItem, new: CartItem) =
            old == new
    }
}