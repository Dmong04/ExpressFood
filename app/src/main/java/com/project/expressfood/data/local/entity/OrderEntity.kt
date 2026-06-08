package com.project.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val clientId: String,
    val date: Long,
    val time: String,
    val status: String,
    val totalPrice: Double,
    val synced: Boolean = false,
)
