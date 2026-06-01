package com.tuapp.expressfood.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(

    @PrimaryKey
    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    val lastName: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "profile_photo")
    val profilePhoto: String,

    @ColumnInfo(name = "role")
    val role: String,                  // "CLIENT" | "ADMIN"

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long                // epoch millis
)