package com.crimetracker.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CrimeTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

