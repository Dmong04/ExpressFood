package com.project.expressfood.data.remote.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthService(private val firebaseAuth: FirebaseAuth) {

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    /** Emite el usuario actual cada vez que cambia el estado de autenticación. */
    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun signOut() = firebaseAuth.signOut()
}
