package com.project.expressfood.ui.client.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.Order
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class UiOrder(
    val order: Order,
    val itemsSummary: String
)

class OrdersViewModel(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val clientId       : String,
) : ViewModel() {

    private val _ordersState = MutableLiveData<List<UiOrder>>()
    val ordersState: LiveData<List<UiOrder>> = _ordersState

    init {
        observeOrders()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            orderRepository.getOrdersByClient(clientId).collectLatest { orderList ->
                val uiOrders = mutableListOf<UiOrder>()
                for (order in orderList) {
                    val orderWithDetails = orderRepository.getOrderWithDetails(order.orderId)
                    val details = orderWithDetails?.details ?: emptyList()
                    val summary = if (details.isEmpty()) {
                        "Sin detalles"
                    } else {
                        val itemStrings = mutableListOf<String>()
                        for (detail in details) {
                            val productName = productRepository.getProductById(detail.itemId)?.name ?: "Producto"
                            itemStrings.add("${detail.quantity}x $productName")
                        }
                        itemStrings.joinToString()
                    }
                    uiOrders.add(UiOrder(order, summary))
                }
                _ordersState.value = uiOrders
            }
        }
    }
}
