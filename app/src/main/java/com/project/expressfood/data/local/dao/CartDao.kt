package com.project.expressfood.data.local.dao

import androidx.room.*
import com.project.expressfood.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items WHERE clientId = :clientId")
    fun getByClient(clientId: String): Flow<List<CartItemEntity>>

    @Upsert
    suspend fun upsert(item: CartItemEntity)

    @Delete
    suspend fun delete(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE clientId = :clientId")
    suspend fun clearByClient(clientId: String)
}
