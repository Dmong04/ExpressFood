package com.project.expressfood.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.expressfood.data.local.dao.CartDao
import com.project.expressfood.data.local.dao.ItemDao
import com.project.expressfood.data.local.dao.OrderDao
import com.project.expressfood.data.local.entity.CartItemEntity
import com.project.expressfood.data.local.entity.ItemEntity
import com.project.expressfood.data.local.entity.OrderDetailEntity
import com.project.expressfood.data.local.entity.OrderEntity

@Database(
    entities = [
        ItemEntity::class,
        OrderEntity::class,
        OrderDetailEntity::class,
        CartItemEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class ExpressFoodDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao
}
