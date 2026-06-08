package com.project.expressfood.cart

import com.project.expressfood.data.local.dao.CartDao
import com.project.expressfood.data.local.entity.CartItemEntity
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.domain.model.CartItem
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
class CartRepositoryTest {

    @MockK lateinit var cartDao: CartDao

    private lateinit var repository: CartRepository

    private val fakeCartItemEntity = CartItemEntity(
        cartItemId = "cart_001",
        clientId   = "client_001",
        itemId     = "hamburguesa_clasica",
        quantity   = 2,
    )

    private val fakeCartItem = CartItem(
        cartItemId = "cart_001",
        clientId   = "client_001",
        itemId     = "hamburguesa_clasica",
        quantity   = 2,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = CartRepository(cartDao)
    }

    // ─── getCartItems ─────────────────────────────────────────────────────────

    @Test
    fun `getCartItems retorna items del carrito del cliente`() = runTest {
        every { cartDao.getByClient("client_001") } returns flowOf(listOf(fakeCartItemEntity))

        val result = repository.getCartItems("client_001").first()

        assertEquals(1, result.size)
        assertEquals("cart_001", result[0].cartItemId)
        assertEquals("hamburguesa_clasica", result[0].itemId)
        assertEquals(2, result[0].quantity)
    }

    @Test
    fun `getCartItems retorna lista vacia si el carrito esta vacio`() = runTest {
        every { cartDao.getByClient("client_001") } returns flowOf(emptyList())

        val result = repository.getCartItems("client_001").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getCartItems retorna multiples items correctamente`() = runTest {
        val segundoItem = fakeCartItemEntity.copy(
            cartItemId = "cart_002",
            itemId     = "wrap_pollo",
            quantity   = 1,
        )
        every { cartDao.getByClient("client_001") } returns flowOf(listOf(fakeCartItemEntity, segundoItem))

        val result = repository.getCartItems("client_001").first()

        assertEquals(2, result.size)
        assertEquals("cart_001", result[0].cartItemId)
        assertEquals("cart_002", result[1].cartItemId)
    }

    // ─── addOrUpdate ──────────────────────────────────────────────────────────

    @Test
    fun `addOrUpdate llama upsert en el DAO con el item correcto`() = runTest {
        coEvery { cartDao.upsert(any()) } just Runs

        repository.addOrUpdate(fakeCartItem)

        coVerify(exactly = 1) {
            cartDao.upsert(match {
                it.cartItemId == "cart_001" &&
                        it.clientId   == "client_001" &&
                        it.itemId     == "hamburguesa_clasica" &&
                        it.quantity   == 2
            })
        }
    }

    @Test
    fun `addOrUpdate puede actualizar la cantidad de un item existente`() = runTest {
        val itemActualizado = fakeCartItem.copy(quantity = 5)
        coEvery { cartDao.upsert(any()) } just Runs

        repository.addOrUpdate(itemActualizado)

        coVerify(exactly = 1) { cartDao.upsert(match { it.quantity == 5 }) }
    }

    // ─── remove ───────────────────────────────────────────────────────────────

    @Test
    fun `remove llama delete en el DAO con el item correcto`() = runTest {
        coEvery { cartDao.delete(any()) } just Runs

        repository.remove(fakeCartItem)

        coVerify(exactly = 1) {
            cartDao.delete(match { it.cartItemId == "cart_001" })
        }
    }

    // ─── clearCart ────────────────────────────────────────────────────────────

    @Test
    fun `clearCart llama clearByClient con el clientId correcto`() = runTest {
        coEvery { cartDao.clearByClient("client_001") } just Runs

        repository.clearCart("client_001")

        coVerify(exactly = 1) { cartDao.clearByClient("client_001") }
    }

    @Test
    fun `clearCart no afecta items de otros clientes`() = runTest {
        coEvery { cartDao.clearByClient(any()) } just Runs

        repository.clearCart("client_001")

        coVerify(exactly = 0) { cartDao.clearByClient("client_002") }
        coVerify(exactly = 1) { cartDao.clearByClient("client_001") }
    }
}