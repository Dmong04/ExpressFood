package com.project.expressfood.domain.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val status: OrderStatus = OrderStatus.PENDING,
    val total: Double = 0.0,
    val createdAt: Long = 0L,
    val items: List<OrderItem> = emptyList()
)

enum class OrderStatus {
    PENDING, PREPARING, READY, DELIVERED, CANCELLED
}
