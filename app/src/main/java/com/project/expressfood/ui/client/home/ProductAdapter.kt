package com.project.expressfood.ui.client.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.expressfood.R
import com.project.expressfood.databinding.ItemMenuBinding
import com.project.expressfood.domain.model.Product
import java.text.NumberFormat
import java.util.Locale


class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {

    inner class ProductViewHolder(private val binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvItemTitle.text       = product.name
            binding.tvItemDescription.text = product.description
            binding.tvItemPrice.text = NumberFormat
                .getCurrencyInstance(Locale("es", "CR"))
                .format(product.price)
            binding.tvItemRating.text = product.rating.toString()

            Glide.with(binding.root)
                .load(product.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_food_placeholder)
                .into(binding.ivItemImage)

            binding.cardItem.setOnClickListener { onProductClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProductViewHolder(
            ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
        override fun areContentsTheSame(old: Product, new: Product) = old == new
    }
}