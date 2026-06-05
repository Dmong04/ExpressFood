package com.project.expressfood.data.remote.supabase

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.project.expressfood.BuildConfig

class SupabaseStorageService(private val context: Context) {

    private val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Storage)
    }

    private val bucket = client.storage["ExpressFood_Media"]

    /**
     * Sube una imagen a Supabase Storage y retorna la URL pública.
     * @param productId  ID del producto (se usa como nombre de archivo)
     * @param imageUri   URI local de la imagen seleccionada
     * @return Result con la URL pública, o failure si algo falla
     */
    suspend fun uploadProductImage(productId: String, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver
                    .openInputStream(imageUri)
                    ?.use { it.readBytes() }
                    ?: return@withContext Result.failure(Exception("No se pudo leer la imagen"))

                val path = "products/$productId.jpg"

                bucket.upload(path, bytes) {
                    upsert = true   // sobreescribe si ya existe
                }

                val publicUrl = bucket.publicUrl(path)
                Result.success(publicUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Elimina la imagen de un producto en Supabase Storage.
     * @param productId  ID del producto
     */
    suspend fun deleteProductImage(productId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val path = "products/$productId.jpg"
                bucket.delete(listOf(path))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Retorna la URL pública de la imagen de un producto sin hacer ninguna llamada de red.
     * Útil para construir la URL si ya sabés que la imagen existe.
     */
    fun getPublicUrl(productId: String): String {
        return bucket.publicUrl("products/$productId.jpg")
    }
}