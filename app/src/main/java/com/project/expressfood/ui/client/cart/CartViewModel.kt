package com.project.expressfood.ui.client.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.CartItem

data class CartSummary(
    val subtotal : Double,
    val tax      : Double,
    val total    : Double,
)

class CartViewModel(
    private val cartRepository   : CartRepository,
    private val productRepository : ProductRepository,
    private val clientId          : String,
) : ViewModel() {

    companion object {
        const val TAX_RATE = 0.13  // IVA Costa Rica 13%
    }

    // ── Carrito como LiveData ─────────────────────────────────────
    val cartItems = cartRepository
        .getCartItems(clientId)
        .asLiveData()

    // ── Resumen reactivo derivado del carrito ─────────────────────
    val summary = cartItems.map { items ->
        buildSummary(items)
    }

    private fun buildSummary(items: List<CartItem>): CartSummary {
        val subtotal = items.sumOf { it.unitPrice * it.quantity }
        val tax      = subtotal * TAX_RATE
        val total    = subtotal + tax
        return CartSummary(subtotal, tax, total)
    }

    suspend fun removeItem(item: CartItem) =
        cartRepository.remove(item)

    suspend fun incrementItem(item: CartItem) =
        cartRepository.incrementQuantity(item)

    suspend fun decrementItem(item: CartItem) =
        cartRepository.decrementOrRemove(item)

    suspend fun clearCart() =
        cartRepository.clearCart(clientId)
}