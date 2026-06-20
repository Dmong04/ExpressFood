package com.project.expressfood.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardState(
    val isLoading: Boolean = true,
    val todayRevenue: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val pendingCount: Int = 0,
    val preparingCount: Int = 0,
    val readyCount: Int = 0,
    val upcomingOrders: List<Order> = emptyList()
)

class AdminDashboardViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        observeOrders()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            orderRepository.watchAllOrders().collect { orders ->
                val todayStart = startOfToday()
                val delivered = orders.filter { it.status == OrderStatus.DELIVERED }
                val upcoming = orders
                    .filter {
                        it.status == OrderStatus.PENDING ||
                        it.status == OrderStatus.PREPARING ||
                        it.status == OrderStatus.READY
                    }
                    .sortedBy { it.date }

                _state.value = DashboardState(
                    isLoading = false,
                    todayRevenue = delivered
                        .filter { it.date >= todayStart }
                        .sumOf { it.totalPrice },
                    totalRevenue = delivered.sumOf { it.totalPrice },
                    pendingCount = upcoming.count { it.status == OrderStatus.PENDING },
                    preparingCount = upcoming.count { it.status == OrderStatus.PREPARING },
                    readyCount = upcoming.count { it.status == OrderStatus.READY },
                    upcomingOrders = upcoming
                )
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateStatus(orderId, newStatus)
        }
    }

    private fun startOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    class Factory(
        private val orderRepository: OrderRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AdminDashboardViewModel(orderRepository) as T
    }
}
