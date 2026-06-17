package com.project.expressfood.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.expressfood.data.local.database.ExpressFoodDatabase
import com.project.expressfood.data.remote.auth.AuthService
import com.project.expressfood.data.remote.firestore.ProductFirestoreService
import com.project.expressfood.data.remote.firestore.OrderFirestoreService
import com.project.expressfood.data.remote.firestore.UserFirestoreService
import com.project.expressfood.data.remote.supabase.SupabaseStorageService
import com.project.expressfood.data.repository.AuthRepository
import com.project.expressfood.data.repository.CartRepository
import com.project.expressfood.data.repository.ProductRepository
import com.project.expressfood.data.repository.OrderRepository
import com.project.expressfood.data.util.NetworkMonitor

/**
 * Contenedor de dependencias (DI manual).
 * Reemplaza a Hilt mientras se resuelve la versión de KSP compatible con AGP 9.x.
 * Migrar a Hilt cuando esté disponible KSP para Kotlin 2.2.x.
 */
class AppContainer(context: Context) {

    // Firebase
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Supabase
    val supabaseStorageService: SupabaseStorageService by lazy {
        SupabaseStorageService(context)
    }

    // Room database
    val database: ExpressFoodDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            ExpressFoodDatabase::class.java,
            "expressfood_db"
        )
            .fallbackToDestructiveMigration(false)
        .build()
    }

    // Remote services
    val authService: AuthService by lazy {
        AuthService(
            auth        = firebaseAuth,
            webClientId = com.project.expressfood.BuildConfig.WEB_CLIENT_ID
        )
    }

    val userFirestoreService: UserFirestoreService by lazy { UserFirestoreService(firestore) }
    val productFirestoreService: ProductFirestoreService by lazy { ProductFirestoreService(firestore) }
    val orderFirestoreService: OrderFirestoreService by lazy { OrderFirestoreService(firestore) }

    // Repositories
    val authRepository: AuthRepository by lazy { AuthRepository(authService, userFirestoreService) }
    val productRepository: ProductRepository by lazy { ProductRepository(database.productDao(), productFirestoreService, supabaseStorageService) }
    val orderRepository: OrderRepository by lazy { OrderRepository(database.orderDao(), orderFirestoreService) }
    val cartRepository: CartRepository by lazy { CartRepository(database.cartDao()) }

    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(context) }
}
