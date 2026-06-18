package com.project.expressfood.ui.client.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.project.expressfood.data.repository.OrderRepository

class OrdersViewModel(
    orderRepository: OrderRepository,
    clientId       : String,
) : ViewModel() {

    // Ya viene ordenado por fecha desc desde el DAO
    val orders = orderRepository
        .getOrdersByClient(clientId)
        .asLiveData()
}