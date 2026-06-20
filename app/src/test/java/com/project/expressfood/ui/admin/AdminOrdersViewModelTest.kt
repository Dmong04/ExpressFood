package com.project.expressfood.ui.admin

import com.project.expressfood.data.remote.firestore.UserFirestoreService
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AdminOrdersViewModelTest {

    @MockK lateinit var orderRepository: OrderRepository
    @MockK lateinit var productRepository: ProductRepository
    @MockK lateinit var userFirestoreService: UserFirestoreService

    private lateinit var viewModel: AdminOrdersViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val fakeOrdersFlow = MutableStateFlow<List<Order>>(emptyList())

    private val fakeOrder = Order(
        orderId = "order_001",
        clientId = "client_001",
        date = 1000L,
        time = "12:00",
        status = OrderStatus.PENDING,
        totalPrice = 5000.0,
        details = listOf(OrderDetail(itemId = "prod_001", quantity = 2, itemPrice = 2500.0))
    )

    private val fakeUser = User(
        uid = "client_001",
        firstName = "Juan",
        lastName = "Pérez"
    )

    private val fakeProduct = Product(
        id = "prod_001",
        name = "Hamburguesa"
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { orderRepository.watchAllOrders() } returns fakeOrdersFlow

        coEvery { orderRepository.getOrderWithDetails(any()) } answers {
            val id = it.invocation.args[0] as String
            if (id == "order_001") fakeOrder
            else if (id == "order_002") fakeOrder.copy(orderId = "order_002", status = OrderStatus.READY)
            else null
        }
        
        coEvery { userFirestoreService.getUser(any()) } returns fakeUser
        coEvery { productRepository.getProductById(any()) } returns fakeProduct

        viewModel = AdminOrdersViewModel(orderRepository, productRepository, userFirestoreService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `inicializacion carga ordenes y resuelve nombres correctamente`() = runTest {
        fakeOrdersFlow.value = listOf(fakeOrder)
        advanceUntilIdle()
        
        val state = viewModel.ordersState.value
        
        assertEquals(1, state.size)
        assertEquals("Juan Pérez", state[0].clientName)
        assertEquals(1, state[0].itemsSummary.size)
        assertEquals("Hamburguesa", state[0].itemsSummary[0].productName)
    }

    @Test
    fun `filtrado por estado funciona correctamente`() = runTest {
        val readyOrder = fakeOrder.copy(orderId = "order_002", status = OrderStatus.READY)
        fakeOrdersFlow.value = listOf(fakeOrder, readyOrder)
        advanceUntilIdle()


        viewModel.filterByStatus(OrderStatus.READY)
        advanceUntilIdle()

        val state = viewModel.ordersState.value
        assertEquals("Debería filtrar a 1 orden", 1, state.size)
        assertEquals(OrderStatus.READY, state[0].order.status)
        assertEquals("order_002", state[0].order.orderId)
    }

    @Test
    fun `filtro null muestra todas las ordenes`() = runTest {
        val readyOrder = fakeOrder.copy(orderId = "order_002", status = OrderStatus.READY)
        fakeOrdersFlow.value = listOf(fakeOrder, readyOrder)
        advanceUntilIdle()
        
        viewModel.filterByStatus(null)
        advanceUntilIdle()

        val state = viewModel.ordersState.value
        assertEquals(2, state.size)
    }

    @Test
    fun `resolucion de nombres usa cache`() = runTest {
        fakeOrdersFlow.value = listOf(fakeOrder, fakeOrder.copy(orderId = "order_002"))
        advanceUntilIdle()

        coVerify(exactly = 1) { userFirestoreService.getUser("client_001") }
    }

    @Test
    fun `updateOrderStatus llama al repositorio correctamente`() = runTest {
        coEvery { orderRepository.updateStatus(any(), any()) } just Runs

        viewModel.updateOrderStatus("order_123", OrderStatus.DELIVERED)
        advanceUntilIdle()
        
        coVerify { orderRepository.updateStatus("order_123", OrderStatus.DELIVERED) }
    }

    @Test
    fun `estado de carga se actualiza al recibir datos`() = runTest {
        fakeOrdersFlow.value = listOf(fakeOrder)
        advanceUntilIdle()
        
        val loading = viewModel.isLoading.value
        assertEquals(false, loading)
    }
}
