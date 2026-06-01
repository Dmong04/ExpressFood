package com.tuapp.expressfood.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tuapp.expressfood.data.local.dao.*
import com.tuapp.expressfood.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        ItemEntity::class,
        OrderEntity::class,
        OrderDetailEntity::class,
        CartItemEntity::class,
        StorageEntity::class
    ],
    version = 1,
    exportSchema = true          // genera JSON del esquema → útil para migraciones futuras
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao
    abstract fun orderDao(): OrderDao
    abstract fun orderDetailDao(): OrderDetailDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun storageDao(): StorageDao

    companion object {
        private const val DB_NAME = "expressfood.db"

        // Singleton thread-safe con double-checked locking
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // En producción reemplaza por migraciones reales en lugar de fallbackToDestructiveMigration
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}