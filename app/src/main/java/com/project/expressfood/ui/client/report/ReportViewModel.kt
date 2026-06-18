package com.project.expressfood.ui.client.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.domain.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DayReport(
    val dateLabel: String,
    val orders: List<Order>,
    val dayTotal: Double,
)

sealed class ReportListItem {
    data class DayHeader(
        val dateLabel: String,
        val dayTotal: Double,
        val orderCount: Int,
        val isExpanded: Boolean,
    ) : ReportListItem()

    data class OrderRow(val order: Order) : ReportListItem()
}

class ReportViewModel(
    orderRepository: OrderRepository,
    clientId: String,
) : ViewModel() {

    private val dayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CR"))

    private val dayReports: StateFlow<List<DayReport>> = orderRepository
        .watchOrdersByClient(clientId)
        .map { orders -> groupByDay(orders) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _expandedDays = MutableStateFlow<Set<String>>(emptySet())

    val reportItems: StateFlow<List<ReportListItem>> =
        combine(dayReports, _expandedDays) { reports, expanded ->
            buildList {
                reports.forEach { day ->
                    val isExpanded = day.dateLabel in expanded
                    add(ReportListItem.DayHeader(day.dateLabel, day.dayTotal, day.orders.size, isExpanded))
                    if (isExpanded) day.orders.forEach { add(ReportListItem.OrderRow(it)) }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyTotal: StateFlow<Double> = dayReports
        .map { reports -> reports.sumOf { it.dayTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun toggleDay(dateLabel: String) {
        _expandedDays.update { current ->
            if (dateLabel in current) current - dateLabel else current + dateLabel
        }
    }

    fun formatCurrency(amount: Double): String = currencyFormat.format(amount)

    private fun groupByDay(orders: List<Order>): List<DayReport> {
        val grouped = orders.groupBy { dayFormat.format(Date(it.date)) }
        return grouped.map { (label, dayOrders) ->
            DayReport(
                dateLabel = label,
                orders    = dayOrders.sortedByDescending { it.date },
                dayTotal  = dayOrders.sumOf { it.totalPrice },
            )
        }.sortedByDescending { it.orders.firstOrNull()?.date ?: 0L }
            .also { reports ->
                // Auto-expand all days on first load
                _expandedDays.update { current ->
                    current + reports.map { it.dateLabel }
                }
            }
    }
}
