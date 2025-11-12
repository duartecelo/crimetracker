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
    
    // GROUPS
    @POST("api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<GroupResponse>
    
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
    
    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): Response<DeleteResponse>
}

