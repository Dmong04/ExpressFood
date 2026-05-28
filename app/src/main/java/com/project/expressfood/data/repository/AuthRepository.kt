package com.project.expressfood.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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

    /**
     * Autentica al usuario con un ID token de Google obtenido via Credential Manager.
     * Si tiene éxito el AuthStateListener en AuthService emite el usuario actualizado.
     */
    suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        authService.signInWithCredential(credential)
    }

    fun signOut() = authService.signOut()
}
