package com.crimetracker.app.data.local.dao

import androidx.room.*
import com.crimetracker.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>
}

