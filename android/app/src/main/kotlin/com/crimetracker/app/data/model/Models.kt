package com.crimetracker.app.data.model

import com.google.gson.annotations.SerializedName

// Auth Models
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    @SerializedName("user_id") val userId: String,
    val username: String,
    val email: String,
    val token: String
)

data class UserProfileResponse(
    val success: Boolean,
    val user: User
)

data class User(
    val id: String,
    val username: String,
    val email: String,
    @SerializedName("created_at") val createdAt: String
)

// Report Models
data class CreateReportRequest(
    val tipo: String,
    val descricao: String,
    val latitude: Double,
    val longitude: Double
)

data class ReportResponse(
    val success: Boolean,
    val data: Report
)

data class ReportsListResponse(
    val success: Boolean,
    val data: List<Report>,
    val count: Int
)

data class Report(
    val id: String,
    val tipo: String,
    val descricao: String,
    val lat: Double,
    val lon: Double,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("author_username") val authorUsername: String? = null,
    @SerializedName("distance_meters") val distanceMeters: Int? = null,
    @SerializedName("distance_km") val distanceKm: String? = null
)

// Group Models
data class CreateGroupRequest(
    val nome: String,
    val descricao: String? = null
)

data class GroupResponse(
    val success: Boolean,
    val data: Group,
    val message: String? = null
)

data class GroupsListResponse(
    val success: Boolean,
    val data: List<Group>,
    val count: Int
)

data class Group(
    val id: String,
    val nome: String,
    val descricao: String? = null,
    @SerializedName("criador_username") val criadorUsername: String? = null,
    @SerializedName("member_count") val memberCount: Int,
    @SerializedName("created_at") val createdAt: String
)

data class GroupMembersResponse(
    val success: Boolean,
    val data: List<GroupMember>,
    val count: Int
)

data class GroupMember(
    val id: String,
    val username: String,
    val email: String,
    @SerializedName("joined_at") val joinedAt: String,
    @SerializedName("is_creator") val isCreator: Int
)

// Post Models
data class CreatePostRequest(
    val conteudo: String
)

data class PostResponse(
    val success: Boolean,
    val data: Post
)

data class PostsListResponse(
    val success: Boolean,
    val data: List<Post>,
    val pagination: Pagination
)

data class Post(
    val id: String,
    @SerializedName("group_id") val groupId: String,
    @SerializedName("author_id") val authorId: String,
    val conteudo: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("author_username") val authorUsername: String,
    @SerializedName("group_name") val groupName: String? = null
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("hasPrevPage") val hasPrevPage: Boolean
)

// Common Models
data class DeleteResponse(
    val success: Boolean,
    val message: String
)

data class ErrorResponse(
    val success: Boolean,
    val message: String
)
