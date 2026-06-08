package com.project.expressfood.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val estimatedTimeMinutes: Int = 0,
    val imageUrl: String = "",
    val available: Boolean = true,
    val category: String = "",
    val ingredients: List<String> = emptyList(),
    val rating: Double = 0.0,
)
