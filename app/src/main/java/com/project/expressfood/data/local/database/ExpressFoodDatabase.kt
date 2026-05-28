package com.project.expressfood.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.expressfood.data.local.dao.CartDao
import com.project.expressfood.data.local.dao.OrderDao
import com.project.expressfood.data.local.dao.ProductDao
import com.project.expressfood.data.local.entity.CartItemEntity
import com.project.expressfood.data.local.entity.OrderEntity
import com.project.expressfood.data.local.entity.OrderItemEntity
import com.project.expressfood.data.local.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CartItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ExpressFoodDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao
}
