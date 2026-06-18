package com.project.expressfood.data.local.dao

import androidx.room.*
import com.project.expressfood.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE available = 1")
    fun getActive(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: String): ProductEntity?

    @Query("""
        SELECT * FROM products 
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(ingredients) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(description) LIKE '%' || LOWER(:query) || '%'
    """)
    fun search(query: String): Flow<List<ProductEntity>>

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Upsert
    suspend fun upsert(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)
}