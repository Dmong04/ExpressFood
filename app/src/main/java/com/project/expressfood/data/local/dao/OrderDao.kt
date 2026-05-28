package com.project.expressfood.data.local.dao

import androidx.room.*
import com.project.expressfood.data.local.entity.OrderDetailEntity
import com.project.expressfood.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders WHERE clientId = :clientId ORDER BY date DESC")
    fun getOrdersByClient(clientId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_detail WHERE orderId = :orderId")
    suspend fun getDetailsByOrder(orderId: String): List<OrderDetailEntity>

    @Upsert
    suspend fun upsertOrder(order: OrderEntity)

    @Upsert
    suspend fun upsertDetails(details: List<OrderDetailEntity>)
}
