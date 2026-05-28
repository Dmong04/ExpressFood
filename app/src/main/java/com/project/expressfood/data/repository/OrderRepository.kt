package com.project.expressfood.data.repository

import com.project.expressfood.data.local.dao.OrderDao
import com.project.expressfood.data.local.entity.OrderDetailEntity
import com.project.expressfood.data.local.entity.OrderEntity
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderDetail
import com.project.expressfood.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(private val orderDao: OrderDao) {

    fun getOrdersByClient(clientId: String): Flow<List<Order>> =
        orderDao.getOrdersByClient(clientId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveOrder(order: Order) {
        orderDao.upsertOrder(order.toEntity())
        orderDao.upsertDetails(order.details.map { it.toEntity() })
    }

    private fun OrderEntity.toDomain() = Order(
        orderId    = orderId,
        clientId   = clientId,
        date       = date,
        time       = time,
        status     = runCatching { OrderStatus.valueOf(status) }.getOrDefault(OrderStatus.PENDING),
        totalPrice = totalPrice,
        synced     = synced,
    )

    private fun Order.toEntity() = OrderEntity(
        orderId    = orderId,
        clientId   = clientId,
        date       = date,
        time       = time,
        status     = status.name,
        totalPrice = totalPrice,
        synced     = synced,
    )

    private fun OrderDetail.toEntity() = OrderDetailEntity(
        detailId  = detailId,
        orderId   = orderId,
        itemId    = itemId,
        quantity  = quantity,
        itemPrice = itemPrice,
        rating    = rating,
    )
}
