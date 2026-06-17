package com.project.expressfood.ui.client.cart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.data.work.SyncOrdersWorker
import com.project.expressfood.domain.model.CartItem
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderDetail
import com.project.expressfood.domain.model.OrderStatus
import com.project.expressfood.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CartSummary(
    val subtotal: Double,
    val tax     : Double,
    val total   : Double,
)

data class CartItemWithProduct(
    val cartItem: CartItem,
    val product : Product?,
)

sealed class CheckoutState {
    object Idle    : CheckoutState()
    object Loading : CheckoutState()
    object Success : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

class CartViewModel(
    private val cartRepository   : CartRepository,
    private val productRepository: ProductRepository,
    private val orderRepository  : OrderRepository,
    private val clientId         : String,
    private val appContext       : Context,
) : ViewModel() {

    companion object {
        const val TAX_RATE = 0.13
    }

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState

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

    // ── Checkout ──────────────────────────────────────────────────
    fun checkout() {
        val items = cartItems.value
        if (items.isNullOrEmpty()) {
            _checkoutState.value = CheckoutState.Error("El carrito está vacío")
            return
        }

        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading
            try {
                val now      = Date()
                val orderId  = UUID.randomUUID().toString()
                val subtotal = items.sumOf { it.unitPrice * it.quantity }
                val total    = subtotal + (subtotal * TAX_RATE)
                val time     = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

                val order = Order(
                    orderId    = orderId,
                    clientId   = clientId,
                    date       = now.time,
                    time       = time,
                    status     = OrderStatus.PENDING,
                    totalPrice = total,
                    synced     = false,
                    details    = items.map { item ->
                        OrderDetail(
                            detailId  = UUID.randomUUID().toString(),
                            orderId   = orderId,
                            itemId    = item.itemId,
                            quantity  = item.quantity,
                            itemPrice = item.unitPrice,
                        )
                    }
                )

                orderRepository.saveOrder(order)

                val syncRequest = OneTimeWorkRequestBuilder<SyncOrdersWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
                WorkManager.getInstance(appContext).enqueue(syncRequest)

                cartRepository.clearCart(clientId)
                _checkoutState.value = CheckoutState.Success
            } catch (e: Exception) {
                _checkoutState.value = CheckoutState.Error(e.message ?: "Error al procesar la orden")
            }
        }
    }

    suspend fun removeItem(item: CartItem)    = cartRepository.remove(item)
    suspend fun incrementItem(item: CartItem) = cartRepository.incrementQuantity(item)
    suspend fun decrementItem(item: CartItem) = cartRepository.decrementOrRemove(item)
    suspend fun clearCart()                   = cartRepository.clearCart(clientId)
}