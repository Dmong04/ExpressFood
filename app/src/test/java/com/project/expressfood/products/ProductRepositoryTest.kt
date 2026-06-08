package com.project.expressfood.products

import android.net.Uri
import com.project.expressfood.data.local.dao.ProductDao
import com.project.expressfood.data.local.entity.ProductEntity
import com.project.expressfood.data.remote.firestore.ProductFirestoreService
import com.project.expressfood.data.remote.supabase.SupabaseStorageService
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.Product
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
class ProductRepositoryTest {

    @MockK lateinit var productDao: ProductDao
    @MockK lateinit var productFirestoreService: ProductFirestoreService
    @MockK lateinit var supabaseStorageService: SupabaseStorageService

    private lateinit var repository: ProductRepository

    private val fakeProduct = Product(
        id                   = "item_test_001",
        name                 = "Hamburguesa Test",
        description          = "Producto de prueba",
        price                = 4500.0,
        estimatedTimeMinutes = 15,
        imageUrl             = "",
        available            = true,
        category             = "hamburguesas",
        ingredients          = listOf("Carne", "Pan", "Lechuga"),
        rating               = 0.0,
    )

    private val fakeEntity = ProductEntity(
        id                   = "item_test_001",
        name                 = "Hamburguesa Test",
        description          = "Producto de prueba",
        price                = 4500.0,
        estimatedTimeMinutes = 15,
        imageUrl             = "",
        available            = true,
        category             = "hamburguesas",
        ingredients          = "[\"Carne\",\"Pan\",\"Lechuga\"]",
        rating               = 0.0,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { productDao.getActive() } returns flowOf(emptyList())
        repository = ProductRepository(productDao, productFirestoreService, supabaseStorageService)
    }

    // ─── CREATE sin imagen ─────────────────────────────────────────────────

    @Test
    fun `createProduct sin imagen guarda en Firestore y Room`() = runTest {
        coEvery { productFirestoreService.createProduct(fakeProduct) } returns Result.success("item_test_001")
        coEvery { productDao.upsert(any()) } just Runs

        val result = repository.createProduct(fakeProduct, imageUri = null)

        assertTrue(result.isSuccess)
        assertEquals("item_test_001", result.getOrNull())
        coVerify(exactly = 1) { productFirestoreService.createProduct(fakeProduct) }
        coVerify(exactly = 1) { productDao.upsert(any()) }
        coVerify(exactly = 0) { supabaseStorageService.uploadProductImage(any(), any()) }
    }

    // ─── CREATE con imagen ─────────────────────────────────────────────────

    @Test
    fun `createProduct con imagen sube a Supabase y guarda URL en Firestore`() = runTest {
        val fakeUri = mockk<Uri>()
        val fakeImageUrl = "https://fgswazvmjbxgguvhlpyz.supabase.co/storage/v1/object/public/ExpressFood_Media/products/item_test_001.jpg"

        coEvery { supabaseStorageService.uploadProductImage("item_test_001", fakeUri) } returns Result.success(fakeImageUrl)
        coEvery { productFirestoreService.createProduct(any()) } returns Result.success("item_test_001")
        coEvery { productDao.upsert(any()) } just Runs

        val result = repository.createProduct(fakeProduct, imageUri = fakeUri)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { supabaseStorageService.uploadProductImage("item_test_001", fakeUri) }
        coVerify(exactly = 1) { productFirestoreService.createProduct(match { it.imageUrl == fakeImageUrl }) }
        coVerify(exactly = 1) { productDao.upsert(any()) }
    }

    // ─── CREATE falla en Supabase ──────────────────────────────────────────

    @Test
    fun `createProduct falla si Supabase lanza error`() = runTest {
        val fakeUri = mockk<Uri>()

        coEvery {
            supabaseStorageService.uploadProductImage("item_test_001", fakeUri)
        } returns Result.failure(Exception("Error de red"))

        val result = repository.createProduct(fakeProduct, imageUri = fakeUri)

        assertTrue(result.isFailure)
        assertEquals("Error de red", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { productFirestoreService.createProduct(any()) }
        coVerify(exactly = 0) { productDao.upsert(any()) }
    }

    // ─── DELETE ────────────────────────────────────────────────────────────

    @Test
    fun `deleteProduct elimina imagen, Firestore y Room`() = runTest {
        coEvery { supabaseStorageService.deleteProductImage("item_test_001") } returns Result.success(Unit)
        coEvery { productFirestoreService.deleteProduct("item_test_001") } returns Result.success(Unit)
        coEvery { productDao.deleteById("item_test_001") } just Runs

        val result = repository.deleteProduct("item_test_001")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { supabaseStorageService.deleteProductImage("item_test_001") }
        coVerify(exactly = 1) { productFirestoreService.deleteProduct("item_test_001") }
        coVerify(exactly = 1) { productDao.deleteById("item_test_001") }
    }

    // ─── TOGGLE AVAILABLE ──────────────────────────────────────────────────

    @Test
    fun `toggleAvailable actualiza Firestore y Room`() = runTest {
        coEvery { productFirestoreService.toggleAvailable("item_test_001", false) } returns Result.success(Unit)
        coEvery { productDao.getById("item_test_001") } returns fakeEntity
        coEvery { productDao.upsert(any()) } just Runs

        val result = repository.toggleAvailable("item_test_001", false)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { productFirestoreService.toggleAvailable("item_test_001", false) }
        coVerify(exactly = 1) { productDao.upsert(fakeEntity.copy(available = false)) }
    }

    // ─── GET ALL ───────────────────────────────────────────────────────────

    @Test
    fun `getAll retorna lista de productos desde Room`() = runTest {
        every { productDao.getAll() } returns flowOf(listOf(fakeEntity))

        val result = repository.getAll().first()

        assertEquals(1, result.size)
        assertEquals("item_test_001", result[0].id)
        assertEquals("Hamburguesa Test", result[0].name)
        assertEquals(listOf("Carne", "Pan", "Lechuga"), result[0].ingredients)
    }

    @Test
    fun `getAll retorna lista vacía si no hay productos en Room`() = runTest {
        every { productDao.getAll() } returns flowOf(emptyList())

        val result = repository.getAll().first()

        assertTrue(result.isEmpty())
    }

    // ─── SEARCH ────────────────────────────────────────────────────────────

    @Test
    fun `searchProducts retorna productos que coinciden por nombre`() = runTest {
        every { productDao.search("hambur") } returns flowOf(listOf(fakeEntity))

        val result = repository.searchProducts("hambur").first()

        assertEquals(1, result.size)
        assertTrue(result[0].name.contains("Hambur", ignoreCase = true))
    }

    @Test
    fun `searchProducts retorna productos que coinciden por ingrediente`() = runTest {
        val wrapEntity = ProductEntity(
            id                   = "item_test_002",
            name                 = "Wrap Pollo",
            description          = "",
            price                = 3800.0,
            estimatedTimeMinutes = 10,
            imageUrl             = "",
            available            = true,
            category             = "wraps",
            ingredients          = "[\"Pollo\",\"Lechuga\",\"Tomate\"]",
            rating               = 0.0,
        )
        every { productDao.search("pollo") } returns flowOf(listOf(wrapEntity))

        val result = repository.searchProducts("pollo").first()

        assertEquals(1, result.size)
        assertTrue(result[0].ingredients.contains("Pollo"))
    }

    @Test
    fun `searchProducts retorna múltiples productos si varios coinciden`() = runTest {
        val segundoEntity = fakeEntity.copy(
            id   = "item_test_003",
            name = "Hamburguesa Doble",
        )
        every { productDao.search("hambur") } returns flowOf(listOf(fakeEntity, segundoEntity))

        val result = repository.searchProducts("hambur").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.name.contains("Hambur", ignoreCase = true) })
    }

    @Test
    fun `searchProducts retorna vacío si no hay coincidencias`() = runTest {
        every { productDao.search("xyz_inexistente") } returns flowOf(emptyList())

        val result = repository.searchProducts("xyz_inexistente").first()

        assertTrue(result.isEmpty())
    }

    // ─── SYNC ──────────────────────────────────────────────────────────────

    @Test
    fun `syncProducts descarga de Firestore y guarda en Room`() = runTest {
        coEvery { productFirestoreService.getAllProducts() } returns listOf(fakeProduct)
        coEvery { productDao.upsertAll(any()) } just Runs

        repository.syncProducts()

        coVerify(exactly = 1) { productFirestoreService.getAllProducts() }
        coVerify(exactly = 1) { productDao.upsertAll(match { it.size == 1 && it[0].id == "item_test_001" }) }
    }

    @Test
    fun `syncProducts no llama upsertAll si Firestore devuelve lista vacía`() = runTest {
        coEvery { productFirestoreService.getAllProducts() } returns emptyList()

        repository.syncProducts()

        coVerify(exactly = 0) { productDao.upsertAll(any()) }
    }
}