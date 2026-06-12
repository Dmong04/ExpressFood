package com.project.expressfood.ui.client.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.CartItem
import com.project.expressfood.domain.model.Product

data class CartSummary(
    val subtotal: Double,
    val tax     : Double,
    val total   : Double,
)

data class CartItemWithProduct(
    val cartItem: CartItem,
    val product : Product?,
)

class CartViewModel(
    private val cartRepository   : CartRepository,
    private val productRepository: ProductRepository,
    private val clientId         : String,
) : ViewModel() {

    companion object {
        const val TAX_RATE = 0.13
    }

    // ── Carrito base ──────────────────────────────────────────────
    val cartItems = cartRepository
        .getCartItems(clientId)
        .asLiveData()

    // ── Carrito enriquecido con nombre e imagen ───────────────────
    val cartItemsWithProducts = cartItems.switchMap { items ->
        liveData {
            emit(items.map { item ->
                CartItemWithProduct(
                    cartItem = item,
                    product  = productRepository.getProductById(item.itemId),
                )
            })
        }
    }

    // ── Resumen reactivo ──────────────────────────────────────────
    val summary = cartItems.map { items ->
        val subtotal = items.sumOf { it.unitPrice * it.quantity }
        val tax      = subtotal * TAX_RATE
        val total    = subtotal + tax
        CartSummary(subtotal, tax, total)
    }

    suspend fun removeItem(item: CartItem)    = cartRepository.remove(item)
    suspend fun incrementItem(item: CartItem) = cartRepository.incrementQuantity(item)
    suspend fun decrementItem(item: CartItem) = cartRepository.decrementOrRemove(item)
    suspend fun clearCart()                   = cartRepository.clearCart(clientId)
}