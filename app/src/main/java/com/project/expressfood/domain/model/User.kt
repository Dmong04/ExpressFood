package com.project.expressfood.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.CLIENT,
    val createdAt: Long = 0L
)

enum class UserRole {
    CLIENT, ADMIN
}
