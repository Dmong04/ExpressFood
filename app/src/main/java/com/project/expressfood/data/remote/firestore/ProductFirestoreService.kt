package com.project.expressfood.data.remote.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.project.expressfood.domain.model.Product
import kotlinx.coroutines.tasks.await

class ProductFirestoreService(private val firestore: FirebaseFirestore) {

    private val productsCollection = firestore.collection("products")

    // ─── READ ──────────────────────────────────────────────────────────────

    suspend fun getActiveProducts(): List<Product> {
        return try {
            productsCollection
                .whereEqualTo("available", true)
                .get()
                .await()
                .documents
                .mapNotNull { it.toProduct() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllProducts(): List<Product> {
        return try {
            productsCollection
                .get()
                .await()
                .documents
                .mapNotNull { it.toProduct() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProductById(id: String): Product? {
        return try {
            productsCollection.document(id).get().await().toProduct()
        } catch (e: Exception) {
            null
        }
    }

    // ─── CREATE ────────────────────────────────────────────────────────────

    suspend fun createProduct(product: Product): Result<String> {
        return try {
            val docRef = productsCollection.document(
                product.id.ifBlank { productsCollection.document().id }
            )
            docRef.set(product.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────

    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            val data = product.toMap() + mapOf("updatedAt" to Timestamp.now())
            productsCollection.document(product.id).update(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── DELETE ────────────────────────────────────────────────────────────

    suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            productsCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── TOGGLE AVAILABLE ──────────────────────────────────────────────────

    suspend fun toggleAvailable(id: String, available: Boolean): Result<Unit> {
        return try {
            productsCollection.document(id)
                .update("available", available)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── MAPPERS ───────────────────────────────────────────────────────────

    private fun DocumentSnapshot.toProduct(): Product? {
        return try {
            Product(
                id                   = id,
                name                 = getString("name") ?: return null,
                description          = getString("description") ?: "",
                price                = getDouble("price") ?: 0.0,
                estimatedTimeMinutes = (getLong("estimatedTimeMinutes") ?: 0L).toInt(),
                imageUrl             = getString("imageUrl") ?: "",
                available            = getBoolean("available") ?: true,
                category             = getString("category") ?: "",
                ingredients          = (get("ingredients") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                rating               = getDouble("rating") ?: 0.0,
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Product.toMap(): Map<String, Any> = mapOf(
        "name"                 to name,
        "description"          to description,
        "price"                to price,
        "estimatedTimeMinutes" to estimatedTimeMinutes,
        "imageUrl"             to imageUrl,
        "available"            to available,
        "category"             to category,
        "ingredients"          to ingredients,
        "rating"               to rating,
        "createdAt"            to Timestamp.now(),
    )
}