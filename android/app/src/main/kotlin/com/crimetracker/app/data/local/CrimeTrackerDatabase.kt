package com.crimetracker.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.crimetracker.app.data.local.dao.*
import com.crimetracker.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        CrimeReportEntity::class,
        GroupEntity::class,
        PostEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CrimeTrackerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun crimeReportDao(): CrimeReportDao
    abstract fun groupDao(): GroupDao
    abstract fun postDao(): PostDao
}

