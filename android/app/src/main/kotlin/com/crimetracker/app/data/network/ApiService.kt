package com.crimetracker.app.data.network

import com.crimetracker.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<User>

    // Reports
    @POST("api/reports")
    suspend fun createReport(@Body request: ReportRequest): Response<ReportResponse>

    @GET("api/reports")
    suspend fun getReports(
        @Query("category") category: String? = null,
        @Query("status") status: String? = null
    ): Response<List<Report>>

    @GET("api/reports/{id}")
    suspend fun getReport(@Path("id") id: Int): Response<Report>

    @PATCH("api/reports/{id}/status")
    suspend fun updateReportStatus(
        @Path("id") id: Int,
        @Body status: Map<String, String>
    ): Response<MessageResponse>

    // Groups
    @POST("api/groups")
    suspend fun createGroup(@Body request: GroupRequest): Response<GroupResponse>

    @GET("api/groups")
    suspend fun getGroups(): Response<List<Group>>

    @GET("api/groups/{id}")
    suspend fun getGroup(@Path("id") id: Int): Response<GroupDetail>

    @POST("api/groups/{id}/join")
    suspend fun joinGroup(@Path("id") id: Int): Response<MessageResponse>

    @POST("api/groups/{id}/leave")
    suspend fun leaveGroup(@Path("id") id: Int): Response<MessageResponse>

    // Feed
    @POST("api/feed")
    suspend fun createPost(@Body request: PostRequest): Response<PostResponse>

    @GET("api/feed")
    suspend fun getFeed(@Query("group_id") groupId: Int? = null): Response<List<Post>>

    @GET("api/feed/{id}")
    suspend fun getPost(@Path("id") id: Int): Response<PostDetail>

    @POST("api/feed/{id}/comments")
    suspend fun addComment(
        @Path("id") id: Int,
        @Body request: CommentRequest
    ): Response<CommentResponse>
}

