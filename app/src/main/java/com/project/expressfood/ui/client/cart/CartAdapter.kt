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
import com.bumptech.glide.Glide
import com.project.expressfood.R

class CartAdapter(
    private val onIncrement: (CartItem) -> Unit,
    private val onDecrement: (CartItem) -> Unit,
) : ListAdapter<CartItemWithProduct, CartAdapter.CartViewHolder>(DiffCallback) {

    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CR"))

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: CartItemWithProduct) {
            val item    = entry.cartItem
            val product = entry.product

            binding.tvCartItemName.text     = product?.name ?: item.itemId
            binding.tvCartItemPrice.text    = currency.format(item.unitPrice)
            binding.tvQuantity.text         = item.quantity.toString()
            binding.tvCartItemSubtotal.text = currency.format(item.unitPrice * item.quantity)

            Glide.with(binding.root)
                .load(product?.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_food_placeholder)
                .into(binding.ivCartItemImage)

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

    companion object DiffCallback : DiffUtil.ItemCallback<CartItemWithProduct>() {
        override fun areItemsTheSame(old: CartItemWithProduct, new: CartItemWithProduct) =
            old.cartItem.cartItemId == new.cartItem.cartItemId
        override fun areContentsTheSame(old: CartItemWithProduct, new: CartItemWithProduct) =
            old == new
    }
}