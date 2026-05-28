package com.project.expressfood.domain.model

data class Order(
    val orderId: String = "",
    val clientId: String = "",
    val date: Long = 0L,
    val time: String = "",
    val status: OrderStatus = OrderStatus.PENDING,
    val totalPrice: Double = 0.0,
    val synced: Boolean = false,
    val details: List<OrderDetail> = emptyList(),
)

enum class OrderStatus {
    PENDING, PREPARING, READY, DELIVERED, CANCELLED
}
