package com.project.expressfood.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.expressfood.data.repository.AuthRepository
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderStatus
import com.project.expressfood.data.remote.firestore.UserFirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class OrderWithClient(
    val order: Order,
    val clientName: String
)

class AdminOrdersViewModel(
    private val orderRepository: OrderRepository,
    private val userFirestoreService: UserFirestoreService
) : ViewModel() {

    private val _ordersState = MutableStateFlow<List<OrderWithClient>>(emptyList())
    val ordersState: StateFlow<List<OrderWithClient>> = _ordersState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val clientNamesCache = mutableMapOf<String, String>()

    init {
        observeOrders()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            orderRepository.watchAllOrders().collectLatest { orders ->
                val ordersWithClient = orders.map { order ->
                    val name = getClientName(order.clientId)
                    OrderWithClient(order, name)
                }
                _ordersState.value = ordersWithClient
                _isLoading.value = false
            }
        }
    }

    private suspend fun getClientName(clientId: String): String {
        if (clientId.isEmpty()) return "Anónimo"
        
        return clientNamesCache[clientId] ?: run {
            val user = userFirestoreService.getUser(clientId)
            val name = if (user != null) "${user.firstName} ${user.lastName}".trim()
                      else "Usuario #$clientId"
            val finalName = if (name.isEmpty()) user?.displayName ?: "Sin Nombre" else name
            clientNamesCache[clientId] = finalName
            finalName
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateStatus(orderId, newStatus)
        }
    }

    class Factory(
        private val orderRepository: OrderRepository,
        private val userFirestoreService: UserFirestoreService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AdminOrdersViewModel(orderRepository, userFirestoreService) as T
    }
}
