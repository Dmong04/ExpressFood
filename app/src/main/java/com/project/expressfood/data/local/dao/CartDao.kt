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

    @Query("UPDATE cart_items SET quantity = quantity + 1 WHERE cartItemId = :cartItemId")
    suspend fun incrementQuantity(cartItemId: String)

    @Query("UPDATE cart_items SET quantity = quantity - 1 WHERE cartItemId = :cartItemId AND quantity > 0")
    suspend fun decrementQuantity(cartItemId: String)
}
