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
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    val isImportant: Boolean = false,
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val dislikeCount: Int = 0,
    val isDisliked: Boolean = false,
    val lastSync: Long = System.currentTimeMillis()
)

