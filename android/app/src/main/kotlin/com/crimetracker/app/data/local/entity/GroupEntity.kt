package com.crimetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val descricao: String?,
    val criadorUsername: String?,
    val memberCount: Int,
    val createdAt: String,
    val isMember: Boolean = false,
    val lastSync: Long = System.currentTimeMillis()
)

