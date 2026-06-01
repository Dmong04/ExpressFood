package com.tuapp.expressfood.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_item",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["uid"],
            childColumns = ["client_id"],
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
        Index(value = ["client_id"]),
        Index(value = ["item_id"]),
        // Evita duplicados: un usuario no puede tener el mismo item dos veces en el carrito
        Index(value = ["client_id", "item_id"], unique = true)
    ]
)
data class CartItemEntity(

    @PrimaryKey
    @ColumnInfo(name = "cart_item_id")
    val cartItemId: String,

    @ColumnInfo(name = "client_id")
    val clientId: String,              // FK -> users.uid

    @ColumnInfo(name = "item_id")
    val itemId: String,                // FK -> items.item_id

    @ColumnInfo(name = "quantity")
    val quantity: Int
)