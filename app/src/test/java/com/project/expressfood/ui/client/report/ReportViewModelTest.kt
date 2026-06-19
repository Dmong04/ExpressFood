package com.project.expressfood.ui.client.report

import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderStatus
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ReportViewModelTest {

    @MockK lateinit var orderRepository: OrderRepository

    private lateinit var viewModel: ReportViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val clientId = "client_123"
    private val ordersFlow = MutableStateFlow<List<Order>>(emptyList())

    private val dateJan1 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { 
        set(2026, Calendar.JANUARY, 1, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    private val dateJan2 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { 
        set(2026, Calendar.JANUARY, 2, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val fakeOrder1 = Order(
        orderId = "order_1",
        clientId = clientId,
        date = dateJan1,
        totalPrice = 5000.0,
        status = OrderStatus.DELIVERED
    )

    private val fakeOrder2 = Order(
        orderId = "order_2",
        clientId = clientId,
        date = dateJan1,
        totalPrice = 3000.0,
        status = OrderStatus.DELIVERED
    )

    private val fakeOrder3 = Order(
        orderId = "order_3",
        clientId = clientId,
        date = dateJan2,
        totalPrice = 10000.0,
        status = OrderStatus.DELIVERED
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { orderRepository.watchOrdersByClient(clientId) } returns ordersFlow

        viewModel = ReportViewModel(orderRepository, clientId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `agrupamiento por dia funciona correctamente`() = runTest {

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.reportItems.collect {}
        }

        ordersFlow.value = listOf(fakeOrder1, fakeOrder2, fakeOrder3)
        advanceUntilIdle()

        val items = viewModel.reportItems.value
        

        assertEquals("Debería haber 5 items en total", 5, items.size)
        assertTrue(items[0] is ReportListItem.DayHeader)
        assertTrue(items[1] is ReportListItem.OrderRow)
        assertTrue(items[2] is ReportListItem.DayHeader)
        assertTrue(items[3] is ReportListItem.OrderRow)
        assertTrue(items[4] is ReportListItem.OrderRow)
        
        job.cancel()
    }

    @Test
    fun `todos los dias se expanden automaticamente al cargar`() = runTest {
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.reportItems.collect {}
        }

        ordersFlow.value = listOf(fakeOrder1, fakeOrder3)
        advanceUntilIdle()

        val items = viewModel.reportItems.value
        val headers = items.filterIsInstance<ReportListItem.DayHeader>()
        
        assertEquals(2, headers.size)
        assertTrue("Todos los headers deberían estar expandidos", headers.all { it.isExpanded })
        
        job.cancel()
    }

    @Test
    fun `toggleDay contrae y expande correctamente`() = runTest {
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.reportItems.collect {}
        }

        ordersFlow.value = listOf(fakeOrder1, fakeOrder3)
        advanceUntilIdle()

        val initialItems = viewModel.reportItems.value
        assertTrue("La lista no debería estar vacía", initialItems.isNotEmpty())
        
        val firstHeader = initialItems[0] as ReportListItem.DayHeader
        val dateLabel = firstHeader.dateLabel


        viewModel.toggleDay(dateLabel)
        advanceUntilIdle()

        val contractedItems = viewModel.reportItems.value

        assertEquals(3, contractedItems.size)
        
        val headerAfterToggle = contractedItems.find { 
            it is ReportListItem.DayHeader && it.dateLabel == dateLabel 
        } as ReportListItem.DayHeader
        assertEquals(false, headerAfterToggle.isExpanded)

        job.cancel()
    }

    @Test
    fun `monthlyTotal calcula la suma de todas las ordenes`() = runTest {
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.monthlyTotal.collect {}
        }

        ordersFlow.value = listOf(fakeOrder1, fakeOrder2, fakeOrder3)
        advanceUntilIdle()

        assertEquals(18000.0, viewModel.monthlyTotal.value, 0.01)
        
        job.cancel()
    }

    @Test
    fun `formatCurrency usa el formato de colones costarricenses`() {
        val result = viewModel.formatCurrency(12500.0)

        assertTrue(result.contains("12") && result.contains("500"))
    }

    @Test
    fun `lista vacia no genera items`() = runTest {
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.reportItems.collect {}
        }

        ordersFlow.value = emptyList()
        advanceUntilIdle()

        assertTrue(viewModel.reportItems.value.isEmpty())
        
        job.cancel()
    }
}
