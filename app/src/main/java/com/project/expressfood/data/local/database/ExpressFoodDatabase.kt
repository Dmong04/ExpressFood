package com.project.expressfood.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.expressfood.data.local.dao.CartDao
import com.project.expressfood.data.local.dao.ProductDao
import com.project.expressfood.data.local.dao.OrderDao
import com.project.expressfood.data.local.entity.CartItemEntity
import com.project.expressfood.data.local.entity.ProductEntity
import com.project.expressfood.data.local.entity.OrderDetailEntity
import com.project.expressfood.data.local.entity.OrderEntity

@Database(
    entities = [
        ProductEntity::class,
        OrderEntity::class,
        OrderDetailEntity::class,
        CartItemEntity::class,
    ],
    version = 3,
    exportSchema = false
)
abstract class ExpressFoodDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao
}
