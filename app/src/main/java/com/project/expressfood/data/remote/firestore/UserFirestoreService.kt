package com.project.expressfood.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.project.expressfood.domain.model.User
import com.project.expressfood.domain.model.UserRole
import kotlinx.coroutines.tasks.await

class UserFirestoreService(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")

    suspend fun getUser(uid: String): User? {
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                User(
                    uid = uid,
                    email = doc.getString("email") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    role = UserRole.valueOf(doc.getString("role") ?: UserRole.CLIENT.name),
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUser(user: User) {
        usersCollection.document(user.uid).set(
            mapOf(
                "email" to user.email,
                "displayName" to user.displayName,
                "role" to user.role.name,
                "createdAt" to user.createdAt
            )
        ).await()
    }
}
