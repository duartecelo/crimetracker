package com.crimetracker.app.util

import kotlinx.coroutines.flow.Flow

sealed class Resource<T>(
    val data: T? = null,
    val cachedData: Flow<T>? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, cachedData: Flow<T>? = null, data: T? = null) : Resource<T>(data, cachedData, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}

