package com.project.expressfood.ui.client.orders

import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderDetail
import com.project.expressfood.domain.model.OrderStatus
import com.project.expressfood.domain.model.Product
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class OrdersViewModelTest {

    @MockK lateinit var orderRepository: OrderRepository
    @MockK lateinit var productRepository: ProductRepository

    private lateinit var viewModel: OrdersViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val clientId = "client_123"

    private val fakeOrder = Order(
        orderId = "order_001",
        clientId = clientId,
        date = 1000L,
        time = "14:00",
        totalPrice = 5000.0,
        status = OrderStatus.DELIVERED
    )

    private val fakeOrderDetail = OrderDetail(
        itemId = "prod_001",
        quantity = 2,
        itemPrice = 2500.0
    )

    private val fakeProduct = Product(
        id = "prod_001",
        name = "Pizza Familiar"
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)


        every { orderRepository.getOrdersByClient(clientId) } returns flowOf(listOf(fakeOrder))
        coEvery { orderRepository.getOrderWithDetails("order_001") } returns fakeOrder.copy(details = listOf(fakeOrderDetail))
        coEvery { productRepository.getProductById("prod_001") } returns fakeProduct
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeOrders mapea ordenes a UiOrder con nombres de productos correctamente`() = runTest {
        viewModel = OrdersViewModel(orderRepository, productRepository, clientId)

        advanceUntilIdle()

        val uiOrders = viewModel.ordersState.value
        
        assertEquals(1, uiOrders?.size)
        assertEquals("2x Pizza Familiar", uiOrders?.get(0)?.itemsSummary)
        assertEquals("order_001", uiOrders?.get(0)?.order?.orderId)
    }

    @Test
    fun `cuando no hay detalles muestra texto Sin detalles`() = runTest {
        coEvery { orderRepository.getOrderWithDetails("order_001") } returns fakeOrder.copy(details = emptyList())
        
        viewModel = OrdersViewModel(orderRepository, productRepository, clientId)
        advanceUntilIdle()

        val uiOrders = viewModel.ordersState.value
        assertEquals("Sin detalles", uiOrders?.get(0)?.itemsSummary)
    }

    @Test
    fun `cuando el producto no existe en el repo usa nombre generico`() = runTest {
        coEvery { productRepository.getProductById("prod_001") } returns null
        
        viewModel = OrdersViewModel(orderRepository, productRepository, clientId)
        advanceUntilIdle()

        val uiOrders = viewModel.ordersState.value
        assertEquals("2x Producto", uiOrders?.get(0)?.itemsSummary)
    }

    @Test
    fun `lista vacia del repositorio resulta en lista vacia en el estado`() = runTest {
        every { orderRepository.getOrdersByClient(clientId) } returns flowOf(emptyList())
        
        viewModel = OrdersViewModel(orderRepository, productRepository, clientId)
        advanceUntilIdle()

        val uiOrders = viewModel.ordersState.value
        assertEquals(0, uiOrders?.size)
    }
}
