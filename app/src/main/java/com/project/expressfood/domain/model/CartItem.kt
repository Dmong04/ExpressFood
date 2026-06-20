package com.project.expressfood.domain.model

data class CartItem(
    val cartItemId: String = "",
    val clientId: String = "",
    val itemId: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
)
