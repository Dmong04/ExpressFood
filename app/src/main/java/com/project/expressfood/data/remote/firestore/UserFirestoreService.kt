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
                    uid          = uid,
                    firstName    = doc.getString("firstName") ?: "",
                    lastName     = doc.getString("lastName") ?: "",
                    phone        = doc.getString("phone") ?: "",
                    profilePhoto = doc.getString("profilePhoto") ?: "",
                    role         = UserRole.valueOf(doc.getString("role") ?: UserRole.CLIENT.name),
                    address      = doc.getString("address") ?: "",
                    createdAt    = doc.getLong("createdAt") ?: 0L,
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUser(user: User) {
        usersCollection.document(user.uid).set(
            mapOf(
                "firstName"    to user.firstName,
                "lastName"     to user.lastName,
                "phone"        to user.phone,
                "profilePhoto" to user.profilePhoto,
                "role"         to user.role.name,
                "address"      to user.address,
                "createdAt"    to user.createdAt,
            )
        ).await()
    }
}
