package com.crimetracker.app.data.local.dao

import androidx.room.*
import com.crimetracker.app.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>
    
    @Query("SELECT * FROM posts WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getPostsByGroup(groupId: String): Flow<List<PostEntity>>
    
    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostById(postId: String): Flow<PostEntity?>
    
    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getPostsByAuthor(authorId: String): Flow<List<PostEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)
    
    @Delete
    suspend fun deletePost(post: PostEntity)
    
    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)
    
    @Query("DELETE FROM posts")
    suspend fun deleteAll()
    
    @Query("DELETE FROM posts WHERE lastSync < :timestamp")
    suspend fun deleteOldPosts(timestamp: Long)
}

