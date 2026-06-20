package com.project.expressfood.orders

import com.project.expressfood.data.local.dao.OrderDao
import com.project.expressfood.data.local.entity.OrderDetailEntity
import com.project.expressfood.data.local.entity.OrderEntity
import com.project.expressfood.data.remote.firestore.OrderFirestoreService
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderDetail
import com.project.expressfood.domain.model.OrderStatus
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OrderRepositoryTest {

    @MockK lateinit var orderDao: OrderDao
    @MockK lateinit var orderFirestoreService: OrderFirestoreService

    private lateinit var repository: OrderRepository

    private val fakeOrderEntity = OrderEntity(
        orderId    = "order_001",
        clientId   = "client_001",
        date       = 1000L,
        time       = "12:30",
        status     = "PENDING",
        totalPrice = 9000.0,
        synced     = false,
    )

    private val fakeOrder = Order(
        orderId    = "order_001",
        clientId   = "client_001",
        date       = 1000L,
        time       = "12:30",
        status     = OrderStatus.PENDING,
        totalPrice = 9000.0,
        synced     = false,
        details    = emptyList(),
    )

    private val fakeDetailEntity = OrderDetailEntity(
        detailId  = "detail_001",
        orderId   = "order_001",
        itemId    = "hamburguesa_clasica",
        quantity  = 2,
        itemPrice = 4500.0,
        rating    = 0f,
    )

    private val fakeDetail = OrderDetail(
        detailId  = "detail_001",
        orderId   = "order_001",
        itemId    = "hamburguesa_clasica",
        quantity  = 2,
        itemPrice = 4500.0,
        rating    = 0f,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = OrderRepository(orderDao, orderFirestoreService)
    }

    // ─── getOrdersByClient ────────────────────────────────────────────────────

    @Test
    fun `getOrdersByClient retorna ordenes del cliente desde Room`() = runTest {
        every { orderDao.getOrdersByClient("client_001") } returns flowOf(listOf(fakeOrderEntity))

        val result = repository.getOrdersByClient("client_001").first()

        assertEquals(1, result.size)
        assertEquals("order_001", result[0].orderId)
        assertEquals("client_001", result[0].clientId)
        assertEquals(OrderStatus.PENDING, result[0].status)
    }

    @Test
    fun `getOrdersByClient retorna lista vacia si no hay ordenes`() = runTest {
        every { orderDao.getOrdersByClient("client_001") } returns flowOf(emptyList())

        val result = repository.getOrdersByClient("client_001").first()

        assertTrue(result.isEmpty())
    }

    // ─── getAllOrders ─────────────────────────────────────────────────────────

    @Test
    fun `getAllOrders retorna todas las ordenes desde Room`() = runTest {
        val segundaOrden = fakeOrderEntity.copy(orderId = "order_002", clientId = "client_002")
        every { orderDao.getAllOrders() } returns flowOf(listOf(fakeOrderEntity, segundaOrden))

        val result = repository.getAllOrders().first()

        assertEquals(2, result.size)
        assertEquals("order_001", result[0].orderId)
        assertEquals("order_002", result[1].orderId)
    }

    @Test
    fun `getAllOrders retorna lista vacia si no hay ordenes`() = runTest {
        every { orderDao.getAllOrders() } returns flowOf(emptyList())

        val result = repository.getAllOrders().first()

        assertTrue(result.isEmpty())
    }

    // ─── getOrdersByStatus ────────────────────────────────────────────────────

    @Test
    fun `getOrdersByStatus retorna solo ordenes con el status indicado`() = runTest {
        val preparingEntity = fakeOrderEntity.copy(orderId = "order_003", status = "PREPARING")
        every { orderDao.getOrdersByStatus("PREPARING") } returns flowOf(listOf(preparingEntity))

        val result = repository.getOrdersByStatus(OrderStatus.PREPARING).first()

        assertEquals(1, result.size)
        assertEquals(OrderStatus.PREPARING, result[0].status)
    }

    @Test
    fun `getOrdersByStatus mapea status invalido a PENDING por defecto`() = runTest {
        val corruptEntity = fakeOrderEntity.copy(status = "STATUS_INVALIDO")
        every { orderDao.getOrdersByStatus("PENDING") } returns flowOf(listOf(corruptEntity))

        val result = repository.getOrdersByStatus(OrderStatus.PENDING).first()

        assertEquals(OrderStatus.PENDING, result[0].status)
    }

    // ─── saveOrder ────────────────────────────────────────────────────────────

    @Test
    fun `saveOrder guarda orden y detalles en Room`() = runTest {
        val orderConDetalles = fakeOrder.copy(details = listOf(fakeDetail))
        coEvery { orderDao.upsertOrder(any()) } just Runs
        coEvery { orderDao.upsertDetails(any()) } just Runs

        repository.saveOrder(orderConDetalles)

        coVerify(exactly = 1) { orderDao.upsertOrder(match { it.orderId == "order_001" }) }
        coVerify(exactly = 1) { orderDao.upsertDetails(match { it.size == 1 && it[0].detailId == "detail_001" }) }
    }

    @Test
    fun `saveOrder con lista de detalles vacia llama upsertDetails con lista vacia`() = runTest {
        coEvery { orderDao.upsertOrder(any()) } just Runs
        coEvery { orderDao.upsertDetails(any()) } just Runs

        repository.saveOrder(fakeOrder)

        coVerify(exactly = 1) { orderDao.upsertOrder(any()) }
        coVerify(exactly = 1) { orderDao.upsertDetails(emptyList()) }
    }

    @Test
    fun `saveOrder mapea correctamente los campos de la orden`() = runTest {
        coEvery { orderDao.upsertOrder(any()) } just Runs
        coEvery { orderDao.upsertDetails(any()) } just Runs

        repository.saveOrder(fakeOrder)

        coVerify {
            orderDao.upsertOrder(match {
                it.orderId == "order_001" &&
                        it.clientId == "client_001" &&
                        it.status == "PENDING" &&
                        it.totalPrice == 9000.0
            })
        }
    }
}