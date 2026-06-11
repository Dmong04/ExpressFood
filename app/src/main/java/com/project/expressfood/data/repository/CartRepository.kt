package com.project.expressfood.data.repository

import com.project.expressfood.data.local.dao.CartDao
import com.project.expressfood.data.local.entity.CartItemEntity
import com.project.expressfood.domain.model.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepository(private val cartDao: CartDao) {

    fun getCartItems(clientId: String): Flow<List<CartItem>> =
        cartDao.getByClient(clientId).map { it.map { e -> e.toDomain() } }

    suspend fun addOrUpdate(item: CartItem) =
        cartDao.upsert(item.toEntity())

    suspend fun remove(item: CartItem) =
        cartDao.delete(item.toEntity())

    suspend fun clearCart(clientId: String) =
        cartDao.clearByClient(clientId)

    suspend fun incrementQuantity(item: CartItem) =
        cartDao.incrementQuantity(item.cartItemId)

    suspend fun decrementOrRemove(item: CartItem) {
        if (item.quantity > 1) {
            cartDao.decrementQuantity(item.cartItemId)
        } else {
            cartDao.delete(item.toEntity())
        }
    }

    // ── Mappers ───────────────────────────────────────────────────

    private fun CartItemEntity.toDomain() = CartItem(
        cartItemId = cartItemId,
        clientId   = clientId,
        itemId     = itemId,
        quantity   = quantity,
        unitPrice  = unitPrice,
    )

    private fun CartItem.toEntity() = CartItemEntity(
        cartItemId = cartItemId,
        clientId   = clientId,
        itemId     = itemId,
        quantity   = quantity,
        unitPrice  = unitPrice,
    )
}