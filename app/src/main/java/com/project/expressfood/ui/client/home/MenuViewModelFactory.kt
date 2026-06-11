package com.project.expressfood.ui.client.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.data.repository.ProductRepository

class MenuViewModelFactory(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val clientId: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MenuViewModel(productRepository, cartRepository, clientId) as T
    }
}