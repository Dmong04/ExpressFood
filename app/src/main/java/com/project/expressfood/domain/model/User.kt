
package com.project.expressfood.domain.model

data class User(
    val uid          : String   = "",
    val email        : String   = "",   // ← nuevo, requerido por Firestore
    val displayName  : String   = "",   // ← nuevo, requerido por Firestore
    val firstName    : String   = "",
    val lastName     : String   = "",
    val phone        : String   = "",
    val profilePhoto : String   = "",
    val role         : UserRole = UserRole.CLIENT,
    val address      : String   = "",
    val createdAt    : Long     = 0L,
)