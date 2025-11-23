package com.crimetracker.app.data.remote

import com.crimetracker.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // AUTH
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("api/auth/profile")
    suspend fun getProfile(): Response<UserProfileResponse>
    
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>
    
    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>
    
    // REPORTS
    @POST("api/reports")
    suspend fun createReport(@Body request: CreateReportRequest): Response<ReportResponse>
    
    @GET("api/reports/nearby")
    suspend fun getNearbyReports(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius_km") radiusKm: Double = 5.0
    ): Response<ReportsListResponse>
    
    @GET("api/reports/{id}")
    suspend fun getReportById(@Path("id") id: String): Response<ReportResponse>
    
    @POST("api/reports/{id}/feedback")
    suspend fun submitReportFeedback(
        @Path("id") reportId: String,
        @Body request: ReportFeedbackRequest
    ): Response<ReportFeedbackResponse>
    
    @POST("api/reports/{id}/abuse")
    suspend fun reportAbuse(
        @Path("id") reportId: String,
        @Body request: ReportAbuseRequest
    ): Response<MessageResponse>
    
    // GROUPS
    @Multipart
    @POST("api/groups")
    suspend fun createGroup(
        @Part("nome") nome: okhttp3.RequestBody,
        @Part("descricao") descricao: okhttp3.RequestBody?,
        @Part imagem: okhttp3.MultipartBody.Part?
    ): Response<GroupResponse>
    
    @GET("api/groups")
    suspend fun getGroups(@Query("search") search: String? = null): Response<GroupsListResponse>
    
    @POST("api/groups/{id}/join")
    suspend fun joinGroup(@Path("id") groupId: String): Response<GroupResponse>
    
    @POST("api/groups/{id}/leave")
    suspend fun leaveGroup(@Path("id") groupId: String): Response<GroupResponse>
    
    @GET("api/groups/{id}/members")
    suspend fun getGroupMembers(@Path("id") groupId: String): Response<GroupMembersResponse>
    
    // FEED
    @POST("api/groups/{group_id}/posts")
    suspend fun createPost(
        @Path("group_id") groupId: String,
        @Body request: CreatePostRequest
    ): Response<PostResponse>
    
    @GET("api/groups/{group_id}/posts")
    suspend fun getGroupPosts(
        @Path("group_id") groupId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PostsListResponse>
    
    @GET("api/feed")
    suspend fun getUserFeed(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PostsListResponse>
    
    @GET("api/posts/user/me")
    suspend fun getUserPosts(): Response<PostsListResponse>
    
    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): Response<DeleteResponse>
}

