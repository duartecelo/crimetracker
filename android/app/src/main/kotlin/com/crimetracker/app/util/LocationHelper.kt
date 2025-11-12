package com.crimetracker.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

object LocationHelper {
    
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        return try {
            if (!hasLocationPermission(context)) return null
            
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fusedLocationClient.lastLocation.await()
            
            location?.let {
                Pair(it.latitude, it.longitude)
            } ?: run {
                // Localização padrão (São Paulo) se não conseguir obter
                Pair(-23.5505, -46.6333)
            }
        } catch (e: Exception) {
            // Localização padrão em caso de erro
            Pair(-23.5505, -46.6333)
        }
    }
}

