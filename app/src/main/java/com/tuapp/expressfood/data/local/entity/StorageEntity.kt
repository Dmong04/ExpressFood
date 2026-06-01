package com.tuapp.expressfood.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "storage",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["item_id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["item_id"])]
)
data class StorageEntity(

    @PrimaryKey
    @ColumnInfo(name = "path")
    val path: String,                  // ruta local o URL de Firebase Storage

    @ColumnInfo(name = "item_id")
    val itemId: String                 // FK -> items.item_id
)