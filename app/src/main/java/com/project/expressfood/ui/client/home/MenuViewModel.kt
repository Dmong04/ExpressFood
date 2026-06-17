package com.project.expressfood.ui.client.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.CartItem
import com.project.expressfood.domain.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import com.project.expressfood.data.util.NetworkMonitor

class MenuViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val clientId: String,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) productRepository.activeProducts
            else productRepository.searchProducts(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _cartEvent = MutableSharedFlow<String>()
    val cartEvent: SharedFlow<String> = _cartEvent

    fun onSearch(query: String) { _searchQuery.value = query }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            cartRepository.addOrUpdate(
                CartItem(
                    cartItemId = UUID.randomUUID().toString(),
                    clientId   = clientId,
                    itemId     = product.id,
                    quantity   = 1,
                    unitPrice  = product.price,
                )
            )
            _cartEvent.emit("${product.name} agregado al carrito")
        }
    }

    fun syncMenu() {
        viewModelScope.launch { productRepository.syncFromFirestore() }
    }
}