package com.project.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val ingredients: String, // Lista serializada como JSON
    val estimatedTimeMinutes: Int,
    val rating: Float,
    val synced: Boolean = false
)
