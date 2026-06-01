package com.tuapp.expressfood.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(

    @PrimaryKey
    @ColumnInfo(name = "item_id")
    val itemId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "price")
    val price: Float,

    @ColumnInfo(name = "prep_time")
    val prepTime: Int,                 // minutos

    @ColumnInfo(name = "img_url")
    val imgUrl: String,

    @ColumnInfo(name = "active")
    val active: Boolean,

    @ColumnInfo(name = "synced")
    val synced: Boolean                // false = pendiente de subir a Firestore
)