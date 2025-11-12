package com.crimetracker.app.data.local.dao

import androidx.room.*
import com.crimetracker.app.data.local.entity.CrimeReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrimeReportDao {
    
    @Query("SELECT * FROM crime_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<CrimeReportEntity>>
    
    @Query("SELECT * FROM crime_reports WHERE id = :reportId")
    fun getReportById(reportId: String): Flow<CrimeReportEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: CrimeReportEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<CrimeReportEntity>)
    
    @Delete
    suspend fun deleteReport(report: CrimeReportEntity)
    
    @Query("DELETE FROM crime_reports")
    suspend fun deleteAll()
    
    @Query("DELETE FROM crime_reports WHERE lastSync < :timestamp")
    suspend fun deleteOldReports(timestamp: Long)
}

