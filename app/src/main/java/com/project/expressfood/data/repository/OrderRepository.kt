package com.project.expressfood.data.repository

import com.project.expressfood.data.local.dao.OrderDao
import com.project.expressfood.data.local.entity.OrderDetailEntity
import com.project.expressfood.data.local.entity.OrderEntity
import com.project.expressfood.data.remote.firestore.OrderFirestoreService
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderDetail
import com.project.expressfood.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(
    private val orderDao: OrderDao,
    private val orderFirestoreService: OrderFirestoreService,
) {

    fun getOrdersByClient(clientId: String): Flow<List<Order>> =
        orderDao.getOrdersByClient(clientId).map { it.map { e -> e.toDomain() } }

    fun getAllOrders(): Flow<List<Order>> =
        orderDao.getAllOrders().map { it.map { e -> e.toDomain() } }

    // ── Tiempo real desde Firestore (admin cambia → cliente ve) ───

    fun watchOrdersByClient(clientId: String): Flow<List<Order>> =
        orderFirestoreService.watchOrdersByClient(clientId).map { it.map { e -> e.toDomain() } }

    fun watchAllOrders(): Flow<List<Order>> =
        orderFirestoreService.watchAllOrders().map { it.map { e -> e.toDomain() } }

    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>> =
        orderDao.getOrdersByStatus(status.name).map { it.map { e -> e.toDomain() } }

    suspend fun getOrderWithDetails(orderId: String): Order? {
        val entity  = orderDao.getOrdersByClient("") // ver nota abajo *
        val details = orderDao.getDetailsByOrder(orderId).map { it.toDomain() }
        return null // placeholder — se completa cuando exista getById en OrderDao
    }

    // ── Guardar orden (offline-first) ─────────────────────────────

    suspend fun saveOrder(order: Order) {
        orderDao.upsertOrder(order.toEntity())
        orderDao.upsertDetails(order.details.map { it.toEntity() })
    }

    suspend fun updateStatus(orderId: String, newStatus: OrderStatus) {
        orderDao.updateStatus(orderId, newStatus.name)
        orderFirestoreService.updateStatus(orderId, newStatus.name)
    }

    // ── Sincronización (WorkManager) ──────────────────────────────

    suspend fun syncPendingOrders() {
        orderDao.getUnsynced().forEach { entity ->
            val details = orderDao.getDetailsByOrder(entity.orderId)
            orderFirestoreService.pushOrder(entity, details)
            orderDao.markSynced(entity.orderId)
        }
    }

    // ── Mappers ───────────────────────────────────────────────────

    private fun OrderEntity.toDomain() = Order(
        orderId    = orderId,
        clientId   = clientId,
        date       = date,
        time       = time,
        status     = runCatching { OrderStatus.valueOf(status) }.getOrDefault(OrderStatus.PENDING),
        totalPrice = totalPrice,
        synced     = synced,
        details    = emptyList(), // se carga explícitamente con getOrderWithDetails()
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

    private fun OrderDetailEntity.toDomain() = OrderDetail(
        detailId  = detailId,
        orderId   = orderId,
        itemId    = itemId,
        quantity  = quantity,
        itemPrice = itemPrice,
        rating    = rating,
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
