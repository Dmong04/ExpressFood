package com.project.expressfood.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.project.expressfood.data.local.entity.OrderDetailEntity
import com.project.expressfood.data.local.entity.OrderEntity
import kotlinx.coroutines.tasks.await

class OrderFirestoreService(private val firestore: FirebaseFirestore) {

    private val ordersCollection    = firestore.collection("orders")
    private val detailsCollection   = firestore.collection("orderDetail")

    // ── Subir orden pendiente (WorkManager) ───────────────────────

    suspend fun pushOrder(order: OrderEntity, details: List<OrderDetailEntity>) {
        try {
            val orderData = mapOf(
                "clientId"   to order.clientId,
                "date"       to order.date,
                "time"       to order.time,
                "status"     to order.status,
                "totalPrice" to order.totalPrice,
                "synced"     to true,
            )
            ordersCollection.document(order.orderId).set(orderData).await()
            android.util.Log.d("OrderSync", "Order pushed: ${order.orderId}, details count: ${details.size}")

            details.forEach { detail ->
                val detailData = mapOf(
                    "orderId"   to detail.orderId,
                    "itemId"    to detail.itemId,
                    "quantity"  to detail.quantity,
                    "itemPrice" to detail.itemPrice,
                    "rating"    to detail.rating,
                )
                detailsCollection.document(detail.detailId).set(detailData).await()
                android.util.Log.d("OrderSync", "Detail pushed: ${detail.detailId}")
            }
        } catch (e: Exception) {
            android.util.Log.e("OrderSync", "pushOrder failed for order ${order.orderId}", e)
        }
    }

    // ── Cambiar estado (admin) ────────────────────────────────────

    suspend fun updateStatus(orderId: String, status: String) {
        try {
            ordersCollection.document(orderId)
                .update("status", status)
                .await()
        } catch (e: Exception) {
            // Sin conexión — Room ya tiene el estado actualizado
        }
    }

    // ── Obtener órdenes del cliente desde Firestore ───────────────
    // Usado en sincronización inversa (Firestore → Room)

    suspend fun getOrdersByClient(clientId: String): List<OrderEntity> {
        return try {
            ordersCollection
                .whereEqualTo("clientId", clientId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    OrderEntity(
                        orderId    = doc.id,
                        clientId   = doc.getString("clientId") ?: return@mapNotNull null,
                        date       = doc.getLong("date") ?: 0L,
                        time       = doc.getString("time") ?: "",
                        status     = doc.getString("status") ?: "PENDING",
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                        synced     = true,
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Obtener todas las órdenes (admin) ─────────────────────────
    suspend fun getAllOrders(): List<OrderEntity> {
        return try {
            ordersCollection
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    OrderEntity(
                        orderId    = doc.id,
                        clientId   = doc.getString("clientId") ?: return@mapNotNull null,
                        date       = doc.getLong("date") ?: 0L,
                        time       = doc.getString("time") ?: "",
                        status     = doc.getString("status") ?: "PENDING",
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                        synced     = true,
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}