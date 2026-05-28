package com.project.expressfood.data.local.dao

import androidx.room.*
import com.project.expressfood.data.local.entity.OrderEntity
import com.project.expressfood.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersByUser(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsByOrder(orderId: String): List<OrderItemEntity>

    @Upsert
    suspend fun upsertOrder(order: OrderEntity)

    @Upsert
    suspend fun upsertItems(items: List<OrderItemEntity>)
}
