package com.project.expressfood.ui.client.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.expressfood.data.repository.OrderRepository

class OrdersViewModelFactory(
    private val orderRepository: OrderRepository,
    private val clientId       : String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OrdersViewModel(orderRepository, clientId) as T
    }
}