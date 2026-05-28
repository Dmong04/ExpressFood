package com.project.expressfood.data.repository

import com.project.expressfood.data.local.dao.OrderDao
import com.project.expressfood.data.local.entity.OrderEntity
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(private val orderDao: OrderDao) {

    fun getOrdersByUser(userId: String): Flow<List<Order>> =
        orderDao.getOrdersByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveOrder(order: Order) {
        orderDao.upsertOrder(order.toEntity())
    }

    private fun OrderEntity.toDomain() = Order(
        id = id,
        userId = userId,
        status = runCatching { OrderStatus.valueOf(status) }.getOrDefault(OrderStatus.PENDING),
        total = total,
        createdAt = createdAt
    )

    private fun Order.toEntity() = OrderEntity(
        id = id,
        userId = userId,
        status = status.name,
        total = total,
        createdAt = createdAt
    )
}
