package com.project.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "orderDetail",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["orderId"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class OrderDetailEntity(
    @PrimaryKey val detailId: String,
    val orderId: String,
    val itemId: String,
    val quantity: Int,
    val itemPrice: Double,
    val rating: Float = 0f,
)
