package com.project.expressfood.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.project.expressfood.domain.model.Item
import kotlinx.coroutines.tasks.await

class ItemFirestoreService(private val firestore: FirebaseFirestore) {

    private val itemsCollection = firestore.collection("items")

    suspend fun getActiveItems(): List<Item> {
        return try {
            itemsCollection
                .whereEqualTo("active", true)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    Item(
                        itemId      = doc.id,
                        title       = doc.getString("title") ?: return@mapNotNull null,
                        description = doc.getString("description") ?: "",
                        price       = doc.getDouble("price") ?: 0.0,
                        prepTime    = (doc.getLong("prepTime") ?: 0L).toInt(),
                        imgUrl      = doc.getString("imgUrl") ?: "",
                        active      = doc.getBoolean("active") ?: true,
                        synced      = true,
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
