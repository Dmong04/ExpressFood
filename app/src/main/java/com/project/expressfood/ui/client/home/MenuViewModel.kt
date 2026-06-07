package com.project.expressfood.ui.client.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.CartItem
import com.project.expressfood.domain.model.Product
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

class MenuViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val clientId: String,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val products: StateFlow<List<Product>> = run {
        val result = MutableStateFlow<List<Product>>(emptyList())
        viewModelScope.launch {
            // Usa searchProducts de Room cuando hay query, activeProducts cuando está vacío
            _searchQuery.collect { query ->
                if (query.isBlank()) {
                    productRepository.activeProducts.collect { result.value = it }
                } else {
                    productRepository.searchProducts(query).collect { result.value = it }
                }
            }
        }
        result
    }

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
                )
            )
            _cartEvent.emit("${product.name} agregado al carrito")
        }
    }

    fun syncMenu() {
        viewModelScope.launch { productRepository.syncFromFirestore() }
    }
}