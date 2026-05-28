package com.project.expressfood.data.repository

import com.project.expressfood.data.local.dao.ItemDao
import com.project.expressfood.data.local.entity.ItemEntity
import com.project.expressfood.data.remote.firestore.ItemFirestoreService
import com.project.expressfood.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ItemRepository(
    private val itemDao: ItemDao,
    private val itemFirestoreService: ItemFirestoreService,
) {

    val activeItems: Flow<List<Item>> = itemDao.getActive().map { entities ->
        entities.map { it.toDomain() }
    }

    /** Descarga los ítems activos desde Firestore y los guarda en Room. */
    suspend fun syncFromFirestore() {
        val remoteItems = itemFirestoreService.getActiveItems()
        if (remoteItems.isNotEmpty()) {
            itemDao.upsertAll(remoteItems.map { it.toEntity() })
        }
    }

    private fun ItemEntity.toDomain() = Item(
        itemId      = itemId,
        title       = title,
        description = description,
        price       = price,
        prepTime    = prepTime,
        imgUrl      = imgUrl,
        active      = active,
        synced      = synced,
    )

    private fun Item.toEntity() = ItemEntity(
        itemId      = itemId,
        title       = title,
        description = description,
        price       = price,
        prepTime    = prepTime,
        imgUrl      = imgUrl,
        active      = active,
        synced      = synced,
    )
}
