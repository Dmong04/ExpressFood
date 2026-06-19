package com.project.expressfood.ui.client.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.data.repository.ProductRepository

class OrdersViewModelFactory(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val clientId       : String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OrdersViewModel(orderRepository, productRepository, clientId) as T
    }
}
