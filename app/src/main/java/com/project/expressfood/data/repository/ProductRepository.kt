package com.project.expressfood.data.repository

import android.net.Uri
import com.project.expressfood.data.local.dao.ProductDao
import com.project.expressfood.data.local.entity.ProductEntity
import com.project.expressfood.data.remote.firestore.ProductFirestoreService
import com.project.expressfood.data.remote.supabase.SupabaseStorageService
import com.project.expressfood.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

class ProductRepository(
    private val productDao: ProductDao,
    private val productFirestoreService: ProductFirestoreService,
    private val supabaseStorageService: SupabaseStorageService,
) {

    val activeProducts: Flow<List<Product>> = productDao.getActive().map { entities ->
        entities.map { it.toDomain() }
    }

    // ─── GET ALL ───────────────────────────────────────────────────────────

    /** Retorna todos los productos desde Room como Flow (offline-first). */
    fun getAll(): Flow<List<Product>> =
        productDao.getAll().map { entities -> entities.map { it.toDomain() } }

    // ─── SEARCH ────────────────────────────────────────────────────────────

    /**
     * Busca productos por nombre o ingredientes en Room como Flow.
     * Combinar con [syncProducts] para tener datos actualizados de Firestore.
     */
    fun searchProducts(query: String): Flow<List<Product>> =
        productDao.search(query).map { entities -> entities.map { it.toDomain() } }

    // ─── SYNC ──────────────────────────────────────────────────────────────

    /** Descarga los productos activos desde Firestore y los guarda en Room. */
    suspend fun syncFromFirestore() {
        val remoteProducts = productFirestoreService.getActiveProducts()
        if (remoteProducts.isNotEmpty()) {
            productDao.upsertAll(remoteProducts.map { it.toEntity() })
        }
    }

    /**
     * Descarga TODOS los productos desde Firestore y los guarda en Room.
     * Usar para admin/panel donde se necesita ver productos no disponibles también.
     */
    suspend fun syncProducts() {
        val remoteProducts = productFirestoreService.getAllProducts()
        if (remoteProducts.isNotEmpty()) {
            productDao.upsertAll(remoteProducts.map { it.toEntity() })
        }
    }

    // ─── CREATE ────────────────────────────────────────────────────────────

    /**
     * Crea un producto nuevo.
     * Si se pasa [imageUri], primero sube la imagen a Supabase y guarda la URL en Firestore.
     */
    suspend fun createProduct(product: Product, imageUri: Uri? = null): Result<String> {
        return try {
            val imageUrl = if (imageUri != null) {
                supabaseStorageService.uploadProductImage(product.id, imageUri)
                    .getOrElse { return Result.failure(it) }
            } else {
                product.imageUrl
            }

            val productWithImage = product.copy(imageUrl = imageUrl)
            val docId = productFirestoreService.createProduct(productWithImage)
                .getOrElse { return Result.failure(it) }

            productDao.upsert(productWithImage.copy(id = docId).toEntity())
            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────

    /**
     * Actualiza un producto existente.
     * Si se pasa [imageUri], sube la nueva imagen a Supabase y reemplaza la URL.
     */
    suspend fun updateProduct(product: Product, imageUri: Uri? = null): Result<Unit> {
        return try {
            val imageUrl = if (imageUri != null) {
                supabaseStorageService.uploadProductImage(product.id, imageUri)
                    .getOrElse { return Result.failure(it) }
            } else {
                product.imageUrl
            }

            val productWithImage = product.copy(imageUrl = imageUrl)
            productFirestoreService.updateProduct(productWithImage)
                .getOrElse { return Result.failure(it) }

            productDao.upsert(productWithImage.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── DELETE ────────────────────────────────────────────────────────────

    /** Elimina un producto de Firestore, Room y su imagen en Supabase. */
    suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            supabaseStorageService.deleteProductImage(id)  // ignora si no hay imagen
            productFirestoreService.deleteProduct(id)
                .getOrElse { return Result.failure(it) }
            productDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── TOGGLE AVAILABLE ──────────────────────────────────────────────────

    suspend fun toggleAvailable(id: String, available: Boolean): Result<Unit> {
        return try {
            productFirestoreService.toggleAvailable(id, available)
                .getOrElse { return Result.failure(it) }
            val entity = productDao.getById(id) ?: return Result.success(Unit)
            productDao.upsert(entity.copy(available = available))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── MAPPERS ───────────────────────────────────────────────────────────

    private fun ProductEntity.toDomain() = Product(
        id                   = id,
        name                 = name,
        description          = description,
        price                = price,
        estimatedTimeMinutes = estimatedTimeMinutes,
        imageUrl             = imageUrl,
        available            = available,
        category             = category,
        ingredients          = ingredients.toIngredientList(),
        rating               = rating,
    )

    private fun Product.toEntity() = ProductEntity(
        id                   = id,
        name                 = name,
        description          = description,
        price                = price,
        estimatedTimeMinutes = estimatedTimeMinutes,
        imageUrl             = imageUrl,
        available            = available,
        category             = category,
        ingredients          = ingredients.toJsonString(),
        rating               = rating,
    )

    private fun String.toIngredientList(): List<String> {
        if (isBlank()) return emptyList()
        return try {
            val array = JSONArray(this)
            List(array.length()) { array.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun List<String>.toJsonString(): String {
        val array = JSONArray()
        forEach { array.put(it) }
        return array.toString()
    }
}