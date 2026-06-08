package com.project.expressfood.ui.client.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.data.repository.ProductRepository

class CartViewModelFactory(
    private val cartRepository   : CartRepository,
    private val productRepository : ProductRepository,
    private val clientId          : String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CartViewModel(cartRepository, productRepository, clientId) as T
    }
}