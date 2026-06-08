package com.project.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val cartItemId: String,
    val clientId: String,
    val itemId: String,
    val quantity: Int,
    val unitPrice: Double = 0.0,   // nuevo
)
