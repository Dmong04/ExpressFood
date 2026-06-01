package com.tuapp.expressfood.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["uid"],
            childColumns = ["client_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["client_id"])]
)
data class OrderEntity(

    @PrimaryKey
    @ColumnInfo(name = "order_id")
    val orderId: String,

    @ColumnInfo(name = "client_id")
    val clientId: String,              // FK -> users.uid

    @ColumnInfo(name = "date")
    val date: Long,                    // epoch millis

    @ColumnInfo(name = "time")
    val time: String,                  // "HH:mm"

    @ColumnInfo(name = "status")
    val status: String,                // "PENDING" | "IN_PROGRESS" | "DELIVERED" | "CANCELLED"

    @ColumnInfo(name = "total_price")
    val totalPrice: Float,

    @ColumnInfo(name = "synced")
    val synced: Boolean
)