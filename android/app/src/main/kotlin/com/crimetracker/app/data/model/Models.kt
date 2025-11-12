package com.crimetracker.app.data.model

import com.google.gson.annotations.SerializedName

// Auth Models
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    val phone: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    val phone: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// Report Models
data class ReportRequest(
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    @SerializedName("image_path") val imagePath: String? = null
)

data class ReportResponse(
    val message: String,
    @SerializedName("reportId") val reportId: Int
)

data class Report(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val status: String,
    @SerializedName("image_path") val imagePath: String? = null,
    val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

// Group Models
data class GroupRequest(
    val name: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("radius_meters") val radiusMeters: Int? = null
)

data class GroupResponse(
    val message: String,
    @SerializedName("groupId") val groupId: Int
)

data class Group(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("created_by") val createdBy: Int,
    @SerializedName("creator_username") val creatorUsername: String,
    @SerializedName("member_count") val memberCount: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("radius_meters") val radiusMeters: Int,
    @SerializedName("created_at") val createdAt: String
)

data class GroupDetail(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("created_by") val createdBy: Int,
    @SerializedName("creator_username") val creatorUsername: String,
    @SerializedName("creator_name") val creatorName: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("radius_meters") val radiusMeters: Int,
    val members: List<GroupMember>,
    @SerializedName("created_at") val createdAt: String
)

data class GroupMember(
    val id: Int,
    val username: String,
    @SerializedName("full_name") val fullName: String,
    val role: String,
    @SerializedName("joined_at") val joinedAt: String
)

// Feed Models
data class PostRequest(
    val content: String,
    @SerializedName("group_id") val groupId: Int? = null,
    @SerializedName("image_path") val imagePath: String? = null
)

data class PostResponse(
    val message: String,
    @SerializedName("postId") val postId: Int
)

data class Post(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("group_id") val groupId: Int? = null,
    val content: String,
    @SerializedName("image_path") val imagePath: String? = null,
    val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("comment_count") val commentCount: Int,
    @SerializedName("created_at") val createdAt: String
)

data class PostDetail(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("group_id") val groupId: Int? = null,
    val content: String,
    @SerializedName("image_path") val imagePath: String? = null,
    val username: String,
    @SerializedName("full_name") val fullName: String,
    val comments: List<Comment>,
    @SerializedName("created_at") val createdAt: String
)

data class Comment(
    val id: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("user_id") val userId: Int,
    val content: String,
    val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("created_at") val createdAt: String
)

data class CommentRequest(
    val content: String
)

data class CommentResponse(
    val message: String,
    @SerializedName("commentId") val commentId: Int
)

data class MessageResponse(
    val message: String
)

