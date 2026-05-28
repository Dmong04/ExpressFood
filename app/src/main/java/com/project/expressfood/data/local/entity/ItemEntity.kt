package com.project.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val itemId: String,
    val title: String,
    val description: String,
    val price: Double,
    val prepTime: Int,
    val imgUrl: String,
    val active: Boolean = true,
    val synced: Boolean = false,
)
