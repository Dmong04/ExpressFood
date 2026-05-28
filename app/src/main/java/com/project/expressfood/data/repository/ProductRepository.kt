package com.project.expressfood.data.repository

import com.project.expressfood.data.local.dao.ProductDao
import com.project.expressfood.data.local.entity.ProductEntity
import com.project.expressfood.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val productDao: ProductDao) {

    val products: Flow<List<Product>> = productDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun upsertProducts(products: List<Product>) {
        productDao.upsertAll(products.map { it.toEntity() })
    }

    private fun ProductEntity.toDomain() = Product(
        id = id,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        estimatedTimeMinutes = estimatedTimeMinutes,
        rating = rating
    )

    private fun Product.toEntity() = ProductEntity(
        id = id,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        ingredients = ingredients.joinToString(","),
        estimatedTimeMinutes = estimatedTimeMinutes,
        rating = rating
    )
}
