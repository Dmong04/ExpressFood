package com.project.expressfood.data.local.dao

import androidx.room.*
import com.project.expressfood.data.local.entity.OrderDetailEntity
import com.project.expressfood.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders WHERE clientId = :clientId ORDER BY date DESC")
    fun getOrdersByClient(clientId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun getById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY date DESC")
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orderDetail WHERE orderId = :orderId")
    suspend fun getDetailsByOrder(orderId: String): List<OrderDetailEntity>

    @Query("SELECT * FROM orders WHERE synced = 0")
    suspend fun getUnsynced(): List<OrderEntity>

    @Query("UPDATE orders SET synced = 1 WHERE orderId = :orderId")
    suspend fun markSynced(orderId: String)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateStatus(orderId: String, status: String)

    @Upsert
    suspend fun upsertOrder(order: OrderEntity)

    @Upsert
    suspend fun upsertDetails(details: List<OrderDetailEntity>)
}
