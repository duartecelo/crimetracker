package com.crimetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crime_reports")
data class CrimeReportEntity(
    @PrimaryKey
    val id: String,
    val tipo: String,
    val descricao: String,
    val lat: Double,
    val lon: Double,
    val createdAt: String,
    val authorUsername: String?,
    val distanceMeters: Int?,
    val distanceKm: String?,
    val lastSync: Long = System.currentTimeMillis()
)

