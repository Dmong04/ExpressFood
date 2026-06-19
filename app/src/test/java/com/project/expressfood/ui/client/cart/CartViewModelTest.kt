package com.project.expressfood.ui.client.cart

import android.content.Context
import androidx.lifecycle.Observer
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.CartItem
import com.project.expressfood.domain.model.Product
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CartViewModelTest {

    @MockK lateinit var cartRepository: CartRepository
    @MockK lateinit var productRepository: ProductRepository
    @MockK lateinit var orderRepository: OrderRepository
    @MockK lateinit var appContext: Context

    private lateinit var viewModel: CartViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private val clientId = "client_001"
    private val cartItemsFlow = MutableStateFlow<List<CartItem>>(emptyList())

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { cartRepository.getCartItems(clientId) } returns cartItemsFlow
        
        viewModel = CartViewModel(
            cartRepository,
            productRepository,
            orderRepository,
            clientId,
            appContext
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `resumen se calcula correctamente al cambiar items del carrito`() = runTest {
        val item1 = CartItem("1", clientId, "prod_1", 2, 1000.0) // 2000
        val item2 = CartItem("2", clientId, "prod_2", 1, 3000.0) // 3000

        cartItemsFlow.value = listOf(item1, item2)

        viewModel.summary.observeForever { summary ->
            assertEquals(5000.0, summary.subtotal, 0.1)
            assertEquals(650.0, summary.tax, 0.1)
            assertEquals(5650.0, summary.total, 0.1)
        }
    }

    @Test
    fun `cartItemsWithProducts enriquece los items con info del producto`() = runTest {
        val item = CartItem("1", clientId, "prod_1", 1, 1000.0)
        val product = Product(id = "prod_1", name = "Pizza")
        
        cartItemsFlow.value = listOf(item)
        coEvery { productRepository.getProductById("prod_1") } returns product

        viewModel.cartItemsWithProducts.observeForever { enriched ->
            if (enriched.isNotEmpty()) {
                assertEquals(1, enriched.size)
                assertEquals("Pizza", enriched[0].product?.name)
            }
        }
    }

    @Test
    fun `removeItem llama al repositorio`() = runTest {
        val item = CartItem("1", clientId, "prod_1", 1, 1000.0)
        coEvery { cartRepository.remove(item) } just Runs

        viewModel.removeItem(item)

        coVerify { cartRepository.remove(item) }
    }

    @Test
    fun `incrementItem llama al repositorio`() = runTest {
        val item = CartItem("1", clientId, "prod_1", 1, 1000.0)
        coEvery { cartRepository.incrementQuantity(item) } just Runs

        viewModel.incrementItem(item)

        coVerify { cartRepository.incrementQuantity(item) }
    }

    @Test
    fun `checkout con carrito vacio emite error`() = runTest {
        cartItemsFlow.value = emptyList()

        viewModel.checkout()

        val state = viewModel.checkoutState.value
        assertTrue(state is CheckoutState.Error)
        assertEquals("El carrito está vacío", (state as CheckoutState.Error).message)
    }

}
