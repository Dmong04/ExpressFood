package com.project.expressfood.products

import android.net.Uri
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.firestore.FirebaseFirestore
import com.project.expressfood.data.local.entity.ProductEntity
import com.project.expressfood.data.remote.firestore.ProductFirestoreService
import com.project.expressfood.data.remote.supabase.SupabaseStorageService
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.domain.model.Product
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.net.URL

/**
 * Seeder de integración real — sube imágenes a Supabase y guarda en Firestore.
 * El DAO se implementa como fake inline — sin MockK, sin Room.
 * Ejecutar con dispositivo/emulador conectado.
 */
@RunWith(AndroidJUnit4::class)
class ProductSeederTest {

    private lateinit var repository: ProductRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // DAO fake inline — ignora escrituras, retorna Flows vacíos
        val productDao = object : com.project.expressfood.data.local.dao.ProductDao {
            override fun getActive() = flowOf(emptyList<ProductEntity>())
            override fun getAll() = flowOf(emptyList<ProductEntity>())
            override fun search(query: String) = flowOf(emptyList<ProductEntity>())
            override suspend fun getById(id: String): ProductEntity? = null
            override suspend fun upsertAll(products: List<ProductEntity>) = Unit
            override suspend fun upsert(product: ProductEntity) = Unit
            override suspend fun delete(product: ProductEntity) = Unit
            override suspend fun deleteById(id: String) = Unit
        }

        // Servicios reales
        val firestoreService = ProductFirestoreService(FirebaseFirestore.getInstance())
        val supabaseService = SupabaseStorageService(context)

        repository = ProductRepository(productDao, firestoreService, supabaseService)
    }

    // ─── Hamburguesa Clásica ───────────────────────────────────────────────────

    @Test
    fun seedHamburguesaClasica() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext

            val product = Product(
                id                   = "hamburguesa_clasica",
                name                 = "Hamburguesa Clásica",
                description          = "Carne 100% de res, lechuga fresca, tomate, queso cheddar y salsa especial.",
                price                = 4500.0,
                estimatedTimeMinutes = 15,
                imageUrl             = "",
                available            = true,
                category             = "hamburguesas",
                ingredients          = listOf(
                    "Carne de res",
                    "Pan brioche",
                    "Lechuga",
                    "Tomate",
                    "Queso cheddar",
                    "Salsa especial",
                ),
                rating = 4.5,
            )

            val imageUrl = "https://www.saborusa.com/wp-content/uploads/2019/10/67.-Hamburguesa-de-carne.png"
            val imageUri = downloadImage(context.cacheDir, imageUrl, "hamburguesa_clasica.jpg")

            println("Subiendo Hamburguesa Clásica...")
            val result = repository.createProduct(product = product, imageUri = imageUri)

            println("Resultado: $result")
            result.exceptionOrNull()?.printStackTrace()
            assertTrue("Hamburguesa Clásica debe crearse exitosamente", result.isSuccess)
            println("Producto creado con ID: ${result.getOrNull()}")

            File(context.cacheDir, "hamburguesa_clasica.jpg").delete()
        }
    }

    // ─── Hamburguesa BBQ ──────────────────────────────────────────────────────

    @Test
    fun seedHamburguesaBBQ() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext

            val product = Product(
                id                   = "hamburguesa_bbq",
                name                 = "Hamburguesa BBQ",
                description          = "Carne de res con salsa BBQ ahumada, cebolla caramelizada y tocino crujiente.",
                price                = 5200.0,
                estimatedTimeMinutes = 18,
                imageUrl             = "",
                available            = true,
                category             = "hamburguesas",
                ingredients          = listOf(
                    "Carne de res",
                    "Salsa BBQ",
                    "Cebolla caramelizada",
                    "Tocino",
                    "Pan brioche",
                ),
                rating = 4.7,
            )

            val imageUrl = "https://bsstatic2.mrjack.es/wp-content/uploads/2020/09/hamburguesa-bbq-cab.jpg"
            val imageUri = downloadImage(context.cacheDir, imageUrl, "hamburguesa_bbq.jpg")

            println("Subiendo Hamburguesa BBQ...")
            val result = repository.createProduct(product = product, imageUri = imageUri)

            println("Resultado: $result")
            result.exceptionOrNull()?.printStackTrace()
            assertTrue("Hamburguesa BBQ debe crearse exitosamente", result.isSuccess)
            println("Producto creado con ID: ${result.getOrNull()}")

            File(context.cacheDir, "hamburguesa_bbq.jpg").delete()
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun downloadImage(cacheDir: File, imageUrl: String, fileName: String): Uri {
        val tempFile = File(cacheDir, fileName)
        println("Descargando imagen desde: $imageUrl")
        URL(imageUrl).openStream().use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        assertTrue("La imagen descargada debe tener contenido", tempFile.length() > 0)
        println("Imagen descargada: ${tempFile.length()} bytes")
        return tempFile.toUri()
    }
}