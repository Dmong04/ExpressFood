package com.project.expressfood.data.repository

import com.google.firebase.auth.FirebaseUser
import com.project.expressfood.data.remote.auth.AuthService
import com.project.expressfood.data.remote.firestore.UserFirestoreService
import com.project.expressfood.domain.model.User
import com.project.expressfood.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val authService: AuthService,
    private val userFirestoreService: UserFirestoreService,
) {
    val currentUser: FirebaseUser? get() = authService.currentUser
    val authState: Flow<FirebaseUser?> = authService.authState

    /** Obtiene el perfil del usuario desde Firestore o lo crea si es la primera vez. */
    suspend fun getOrCreateUser(firebaseUser: FirebaseUser): User {
        return userFirestoreService.getUser(firebaseUser.uid)
            ?: User(
                uid         = firebaseUser.uid,
                email       = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                role        = UserRole.CLIENT,
                createdAt   = System.currentTimeMillis(),
            ).also { userFirestoreService.createUser(it) }
    }

    fun signOut() = authService.signOut()
}
