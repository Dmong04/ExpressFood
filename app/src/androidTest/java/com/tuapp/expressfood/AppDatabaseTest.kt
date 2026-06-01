package com.tuapp.expressfood.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tuapp.expressfood.data.local.dao.*
import com.tuapp.expressfood.data.local.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba instrumentada de Room.
 * Corre en un dispositivo/emulador real pero con base de datos en memoria
 * (se destruye al terminar, no afecta datos reales).
 *
 * Para correrla: clic derecho sobre el archivo → Run
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var itemDao: ItemDao
    private lateinit var orderDao: OrderDao
    private lateinit var orderDetailDao: OrderDetailDao
    private lateinit var cartItemDao: CartItemDao

    // -------------------------------------------------------------------------
    // Datos de prueba
    // -------------------------------------------------------------------------
    private val testUser = UserEntity(
        uid           = "user_001",
        firstName     = "Bryan",
        lastName      = "Pérez",
        phone         = "+506 8888-8888",
        profilePhoto  = "https://foto.com/bryan.jpg",
        role          = "CLIENT",
        address       = "San José, Costa Rica",
        createdAt     = System.currentTimeMillis()
    )

    private val testItem = ItemEntity(
        itemId      = "item_001",
        title       = "Pizza Margherita",
        description = "Tomate, mozzarella y albahaca",
        price       = 8500f,
        prepTime    = 20,
        imgUrl      = "https://img.com/pizza.jpg",
        active      = true,
        synced      = false
    )

    private val testOrder = OrderEntity(
        orderId    = "order_001",
        clientId   = "user_001",       // FK -> testUser
        date       = System.currentTimeMillis(),
        time       = "12:30",
        status     = "PENDING",
        totalPrice = 8500f,
        synced     = false
    )

    private val testDetail = OrderDetailEntity(
        detailId  = "detail_001",
        orderId   = "order_001",       // FK -> testOrder
        itemId    = "item_001",        // FK -> testItem
        quantity  = 1,
        itemPrice = 8500f,
        rating    = 0f
    )

    private val testCartItem = CartItemEntity(
        cartItemId = "cart_001",
        clientId   = "user_001",       // FK -> testUser
        itemId     = "item_001",       // FK -> testItem
        quantity   = 2
    )

    // -------------------------------------------------------------------------
    // Setup y teardown
    // -------------------------------------------------------------------------
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()  // solo para tests
            .build()

        userDao       = db.userDao()
        itemDao       = db.itemDao()
        orderDao      = db.orderDao()
        orderDetailDao = db.orderDetailDao()
        cartItemDao   = db.cartItemDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // ✅ Test 1: Insertar y leer un usuario
    // -------------------------------------------------------------------------
    @Test
    fun insertAndReadUser() = runTest {
        userDao.insert(testUser)

        val result = userDao.getById("user_001")

        assertNotNull("El usuario debe existir", result)
        assertEquals("Bryan", result?.firstName)
        assertEquals("CLIENT", result?.role)
        println("✅ Test 1 OK — Usuario insertado y leído correctamente")
    }

    // -------------------------------------------------------------------------
    // ✅ Test 2: Insertar producto y verificar campos
    // -------------------------------------------------------------------------
    @Test
    fun insertAndReadItem() = runTest {
        itemDao.insert(testItem)

        val activos = itemDao.getActiveItems().first()

        assertEquals(1, activos.size)
        assertEquals("Pizza Margherita", activos[0].title)
        assertEquals(8500f, activos[0].price)
        assertFalse("No debe estar sincronizado aún", activos[0].synced)
        println("✅ Test 2 OK — Producto insertado correctamente")
    }

    // -------------------------------------------------------------------------
    // ✅ Test 3: Relación usuario → órdenes
    // -------------------------------------------------------------------------
    @Test
    fun userWithOrdersRelation() = runTest {
        userDao.insert(testUser)
        itemDao.insert(testItem)
        orderDao.insert(testOrder)

        val userWithOrders = userDao.getUserWithOrders("user_001").first()

        assertNotNull(userWithOrders)
        assertEquals(1, userWithOrders?.orders?.size)
        assertEquals("PENDING", userWithOrders?.orders?.get(0)?.status)
        println("✅ Test 3 OK — Relación usuario→órdenes funciona")
    }

    // -------------------------------------------------------------------------
    // ✅ Test 4: Relación orden → detalles
    // -------------------------------------------------------------------------
    @Test
    fun orderWithDetailsRelation() = runTest {
        userDao.insert(testUser)
        itemDao.insert(testItem)
        orderDao.insert(testOrder)
        orderDetailDao.insert(testDetail)

        val orderWithDetails = orderDao.getOrderWithDetails("order_001").first()

        assertNotNull(orderWithDetails)
        assertEquals(1, orderWithDetails?.details?.size)
        assertEquals(1, orderWithDetails?.details?.get(0)?.quantity)
        println("✅ Test 4 OK — Relación orden→detalles funciona")
    }

    // -------------------------------------------------------------------------
    // ✅ Test 5: Carrito — agregar ítem y contar
    // -------------------------------------------------------------------------
    @Test
    fun cartItemCountAndRelation() = runTest {
        userDao.insert(testUser)
        itemDao.insert(testItem)
        cartItemDao.insert(testCartItem)

        val count = cartItemDao.getCartItemCount("user_001").first()
        val cartWithItems = cartItemDao.getCartWithItems("user_001").first()

        assertEquals(1, count)
        assertEquals("Pizza Margherita", cartWithItems[0].item.title)
        assertEquals(2, cartWithItems[0].cartItem.quantity)
        println("✅ Test 5 OK — Carrito funciona correctamente")
    }

    // -------------------------------------------------------------------------
    // ✅ Test 6: CASCADE — borrar usuario elimina sus órdenes
    // -------------------------------------------------------------------------
    @Test
    fun deletingUserCascadesToOrders() = runTest {
        userDao.insert(testUser)
        itemDao.insert(testItem)
        orderDao.insert(testOrder)

        // Verificar que la orden existe
        assertNotNull(orderDao.getById("order_001"))

        // Borrar el usuario
        userDao.delete(testUser)

        // La orden debe haberse eliminado automáticamente por CASCADE
        val deletedOrder = orderDao.getById("order_001")
        assertNull("La orden debe eliminarse con el usuario (CASCADE)", deletedOrder)
        println("✅ Test 6 OK — CASCADE funciona correctamente")
    }

    // -------------------------------------------------------------------------
    // ✅ Test 7: Marcar ítem como sincronizado
    // -------------------------------------------------------------------------
    @Test
    fun markItemAsSynced() = runTest {
        itemDao.insert(testItem)

        // Antes: no sincronizado
        val unsyncedBefore = itemDao.getUnsynced()
        assertEquals(1, unsyncedBefore.size)

        // Marcar como sincronizado
        itemDao.markAsSynced("item_001")

        // Después: lista vacía
        val unsyncedAfter = itemDao.getUnsynced()
        assertEquals(0, unsyncedAfter.size)
        println("✅ Test 7 OK — Sincronización offline→online funciona")
    }
}