package com.crimetracker.app.data.model

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    val success: Boolean,
    val data: Comment
)

data class Comment(
    val id: String,
    @SerializedName("post_id") val postId: String,
    @SerializedName("author_id") val authorId: String,
    val conteudo: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("author_username") val authorUsername: String
)
