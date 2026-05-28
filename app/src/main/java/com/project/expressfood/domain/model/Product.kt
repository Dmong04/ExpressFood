package com.project.expressfood.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val ingredients: List<String> = emptyList(),
    val estimatedTimeMinutes: Int = 0,
    val rating: Float = 0f
)
