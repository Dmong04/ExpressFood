// data/remote/auth/AuthService.kt
package com.project.expressfood.data.remote.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthService(
    private val auth: FirebaseAuth,
    private val webClientId: String,          // lo sacas de google-services.json → client[0].oauth_client[0].client_id
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Lanza el selector de cuentas de Google y devuelve el FirebaseUser autenticado.
     * Lanza excepción si el usuario cancela o hay error.
     */
    suspend fun signInWithGoogle(context: Context): FirebaseUser {
        val credentialManager = CredentialManager.create(context)

        // Primero intenta con cuentas ya autorizadas
        // Si falla, intenta mostrando todas las cuentas
        val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = try {
            credentialManager.getCredential(context = context, request = request)
        } catch (e: GetCredentialException) {
            throw Exception("Google Sign-In cancelado o fallido: ${e.type} - ${e.message}")
        }

        val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential).await()
        return authResult.user ?: throw Exception("Firebase no devolvió usuario")
    }

    fun signOut() = auth.signOut()
}