package com.project.expressfood.domain.model

data class OrderDetail(
    val detailId: String = "",
    val orderId: String = "",
    val itemId: String = "",
    val quantity: Int = 0,
    val itemPrice: Double = 0.0,
    val rating: Float = 0f,
)
