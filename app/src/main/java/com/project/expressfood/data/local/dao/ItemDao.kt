package com.project.expressfood.data.local.dao

import androidx.room.*
import com.project.expressfood.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items WHERE active = 1")
    fun getActive(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE itemId = :itemId")
    suspend fun getById(itemId: String): ItemEntity?

    @Upsert
    suspend fun upsertAll(items: List<ItemEntity>)

    @Delete
    suspend fun delete(item: ItemEntity)
}
