package com.tuapp.expressfood.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_detail",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["order_id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["item_id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["order_id"]),
        Index(value = ["item_id"])
    ]
)
data class OrderDetailEntity(

    @PrimaryKey
    @ColumnInfo(name = "detail_id")
    val detailId: String,

    @ColumnInfo(name = "order_id")
    val orderId: String,               // FK -> orders.order_id

    @ColumnInfo(name = "item_id")
    val itemId: String,                // FK -> items.item_id

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "item_price")
    val itemPrice: Float,              // precio al momento de la orden

    @ColumnInfo(name = "rating")
    val rating: Float                  // 0.0 si aún no calificado
)