package com.crimetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String,
    val lastSync: Long = System.currentTimeMillis()
)

