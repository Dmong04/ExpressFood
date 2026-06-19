package com.project.expressfood.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderStatus
import com.project.expressfood.data.remote.firestore.UserFirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class OrderWithClient(
    val order: Order,
    val clientName: String,
    val itemsSummary: List<OrderDetailUiModel> = emptyList()
)

data class OrderDetailUiModel(
    val productName: String,
    val quantity: Int,
    val price: Double
)

class AdminOrdersViewModel(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userFirestoreService: UserFirestoreService
) : ViewModel() {

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _currentFilter = MutableStateFlow<OrderStatus?>(null)

    private val _ordersState = MutableStateFlow<List<OrderWithClient>>(emptyList())
    val ordersState: StateFlow<List<OrderWithClient>> = _ordersState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val clientNamesCache = mutableMapOf<String, String>()
    private val productNamesCache = mutableMapOf<String, String>()

    init {
        observeOrders()
        setupFiltering()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            orderRepository.watchAllOrders().collect { orders ->
                _allOrders.value = orders
                _isLoading.value = false
            }
        }
    }

    private fun setupFiltering() {
        viewModelScope.launch {
            combine(_allOrders, _currentFilter) { orders, filter ->
                val filtered = if (filter == null) orders else orders.filter { it.status == filter }
                filtered.sortedByDescending { it.date }
            }.collect { filteredOrders ->
                val ordersWithClient = filteredOrders.map { order ->
                    val name = getClientName(order.clientId)
                    
                    // Fetch full order with details if not present
                    val fullOrder = orderRepository.getOrderWithDetails(order.orderId) ?: order
                    
                    val itemsSummary = fullOrder.details.map { detail ->
                        OrderDetailUiModel(
                            productName = getProductName(detail.itemId),
                            quantity = detail.quantity,
                            price = detail.itemPrice
                        )
                    }
                    
                    OrderWithClient(fullOrder, name, itemsSummary)
                }
                _ordersState.value = ordersWithClient
            }
        }
    }

    private suspend fun getClientName(clientId: String): String {
        if (clientId.isEmpty()) return "Anónimo"
        return clientNamesCache[clientId] ?: run {
            val user = userFirestoreService.getUser(clientId)
            val name = if (user != null) "${user.firstName} ${user.lastName}".trim()
                      else "Usuario #$clientId"
            val finalName = name.ifEmpty { user?.displayName ?: "Sin Nombre" }
            clientNamesCache[clientId] = finalName
            finalName
        }
    }

    private suspend fun getProductName(productId: String): String {
        return productNamesCache[productId] ?: run {
            val product = productRepository.getProductById(productId)
            val name = product?.name ?: "Producto #$productId"
            productNamesCache[productId] = name
            name
        }
    }

    fun filterByStatus(status: OrderStatus?) {
        _currentFilter.value = status
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateStatus(orderId, newStatus)
        }
    }

    class Factory(
        private val orderRepository: OrderRepository,
        private val productRepository: ProductRepository,
        private val userFirestoreService: UserFirestoreService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AdminOrdersViewModel(orderRepository, productRepository, userFirestoreService) as T
    }
}
