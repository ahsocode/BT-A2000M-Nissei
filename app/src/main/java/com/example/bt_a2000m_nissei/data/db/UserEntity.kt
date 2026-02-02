package com.example.bt_a2000m_nissei.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val username: String,
    val passwordHash: String,
    val role: String,       // "ADMIN" | "USER"
    val isActive: Boolean,
    val createdAt: Long
)
