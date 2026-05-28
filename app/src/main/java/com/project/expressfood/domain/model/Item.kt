package com.project.expressfood.domain.model

data class Item(
    val itemId: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val prepTime: Int = 0,
    val imgUrl: String = "",
    val active: Boolean = true,
    val synced: Boolean = false,
)
