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
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ProductSeederTest {

    private lateinit var repository: ProductRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

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

        val firestoreService = ProductFirestoreService(FirebaseFirestore.getInstance())
        val supabaseService  = SupabaseStorageService(context)

        repository = ProductRepository(productDao, firestoreService, supabaseService)
    }

    // ─── Refresco Natural ─────────────────────────────────────────────────────

    @Test
    fun seedRefrescoNatural() {
        runBlocking {
            val context   = InstrumentationRegistry.getInstrumentation().targetContext
            val productId = UUID.randomUUID().toString()

            val product = Product(
                id                   = productId,
                name                 = "Refresco Natural",
                description          = "Bebida natural del día: tamarindo, cas o guanábana.",
                price                = 1500.0,
                estimatedTimeMinutes = 5,
                imageUrl             = "",
                available            = true,
                category             = "bebidas",
                ingredients          = listOf(
                    "Fruta natural",
                    "Agua",
                    "Azúcar",
                ),
                rating = 4.4,
            )

            val imageUri = downloadImage(
                context.cacheDir,
                "https://www.muydelish.com/wp-content/uploads/2022/05/agua-de-tamarindo-drink.jpg",
                "$productId.jpg",
            )

            println("Subiendo Refresco Natural con ID: $productId")
            val result = repository.createProduct(product = product, imageUri = imageUri)

            result.exceptionOrNull()?.printStackTrace()
            assertTrue("Refresco Natural debe crearse exitosamente", result.isSuccess)
            println("Producto creado con ID: ${result.getOrNull()}")

            File(context.cacheDir, "$productId.jpg").delete()
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