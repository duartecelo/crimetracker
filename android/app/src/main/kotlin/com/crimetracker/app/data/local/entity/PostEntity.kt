package com.crimetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val authorId: String,
    val conteudo: String,
    val createdAt: String,
    val authorUsername: String,
    val groupName: String?,
    val lastSync: Long = System.currentTimeMillis()
)

