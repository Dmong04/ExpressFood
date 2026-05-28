package com.project.expressfood.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.expressfood.data.local.database.ExpressFoodDatabase
import com.project.expressfood.data.remote.auth.AuthService
import com.project.expressfood.data.remote.firestore.ItemFirestoreService
import com.project.expressfood.data.remote.firestore.UserFirestoreService
import com.project.expressfood.data.repository.AuthRepository
import com.project.expressfood.data.repository.ItemRepository
import com.project.expressfood.data.repository.OrderRepository

/**
 * Contenedor de dependencias (DI manual).
 * Reemplaza a Hilt mientras se resuelve la versión de KSP compatible con AGP 9.x.
 * Migrar a Hilt cuando esté disponible KSP para Kotlin 2.2.x.
 */
class AppContainer(context: Context) {

    // Firebase
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Room database
    val database: ExpressFoodDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            ExpressFoodDatabase::class.java,
            "expressfood_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    // Remote services
    val authService: AuthService by lazy { AuthService(firebaseAuth) }
    val userFirestoreService: UserFirestoreService by lazy { UserFirestoreService(firestore) }
    val itemFirestoreService: ItemFirestoreService by lazy { ItemFirestoreService(firestore) }

    // Repositories
    val authRepository: AuthRepository by lazy { AuthRepository(authService, userFirestoreService) }
    val itemRepository: ItemRepository by lazy { ItemRepository(database.itemDao(), itemFirestoreService) }
    val orderRepository: OrderRepository by lazy { OrderRepository(database.orderDao()) }
}
