package com.tuapp.expressfood.di

import android.content.Context
import androidx.room.Room
import com.tuapp.expressfood.data.local.AppDatabase
import com.tuapp.expressfood.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "expressfood.db"
    ).fallbackToDestructiveMigration().build()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideItemDao(db: AppDatabase): ItemDao = db.itemDao()
    @Provides fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()
    @Provides fun provideOrderDetailDao(db: AppDatabase): OrderDetailDao = db.orderDetailDao()
    @Provides fun provideCartItemDao(db: AppDatabase): CartItemDao = db.cartItemDao()
    @Provides fun provideStorageDao(db: AppDatabase): StorageDao = db.storageDao()
}