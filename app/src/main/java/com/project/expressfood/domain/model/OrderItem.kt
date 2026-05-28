package com.project.expressfood.domain.model

data class OrderItem(
    val id: String = "",
    val orderId: String = "",
    val productId: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0
)
