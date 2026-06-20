package com.project.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val estimatedTimeMinutes: Int,
    val imageUrl: String,
    val available: Boolean = true,
    val category: String = "",
    val ingredients: String = "",   // JSON string: ["Carne","Pan"]
    val rating: Double = 0.0,
)