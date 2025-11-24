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

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String
)

data class ResetPasswordResponse(
    val success: Boolean,
    val message: String
)

// Report Models
data class CreateReportRequest(
    val tipo: String,
    val descricao: String,
    val latitude: Double,
    val longitude: Double,
    val isAnonymous: Boolean = false
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
    @SerializedName("distance_km") val distanceKm: String? = null,
    @SerializedName("useful_count") val usefulCount: Int = 0,
    @SerializedName("not_useful_count") val notUsefulCount: Int = 0,
    @SerializedName("user_feedback") val userFeedback: String? = null, // "useful", "not_useful", null
    @SerializedName("reliability_score") val reliabilityScore: Float? = null
)

data class ReportFeedbackRequest(
    @SerializedName("report_id") val reportId: String,
    val feedback: String // "useful" ou "not_useful"
)

data class ReportAbuseRequest(
    @SerializedName("report_id") val reportId: String,
    val reason: String, // "false_content", "offensive", "spam", "other"
    @SerializedName("description") val description: String? = null
)

data class ReportFeedbackResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("useful_count") val usefulCount: Int,
    @SerializedName("not_useful_count") val notUsefulCount: Int
)

// Group Models
data class CreateGroupRequest(
    val nome: String,
    val descricao: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null
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
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("imagem") val imagem: String? = null,
    @SerializedName("criador_username") val criadorUsername: String? = null,
    @SerializedName("member_count") val memberCount: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_member") val isMember: Boolean = false
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
    @SerializedName("group_name") val groupName: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    @SerializedName("is_important") val isImportant: Boolean = false,
    @SerializedName("media_url") val mediaUrl: String? = null,
    @SerializedName("media_type") val mediaType: String? = null, // "image", "video"
    @SerializedName("dislike_count") val dislikeCount: Int = 0,
    @SerializedName("is_disliked") val isDisliked: Boolean = false
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
