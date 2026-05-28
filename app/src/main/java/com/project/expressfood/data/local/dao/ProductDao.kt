package com.project.expressfood.data.local.dao

import androidx.room.*
import com.project.expressfood.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: String): ProductEntity?

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Delete
    suspend fun delete(product: ProductEntity)
}
