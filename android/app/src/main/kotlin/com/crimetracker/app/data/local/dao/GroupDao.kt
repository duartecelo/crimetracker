package com.crimetracker.app.data.local.dao

import androidx.room.*
import com.crimetracker.app.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>
    
    @Query("SELECT * FROM groups WHERE isMember = 1 ORDER BY createdAt DESC")
    fun getMyGroups(): Flow<List<GroupEntity>>
    
    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun getGroupById(groupId: String): Flow<GroupEntity?>
    
    @Query("SELECT * FROM groups WHERE nome LIKE '%' || :query || '%'")
    fun searchGroups(query: String): Flow<List<GroupEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)
    
    @Query("UPDATE groups SET isMember = :isMember WHERE id = :groupId")
    suspend fun updateMemberStatus(groupId: String, isMember: Boolean)
    
    @Delete
    suspend fun deleteGroup(group: GroupEntity)
    
    @Query("DELETE FROM groups")
    suspend fun deleteAll()
}

