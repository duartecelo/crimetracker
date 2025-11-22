package com.crimetracker.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode") // "light", "dark", "system"
        private val ANONYMOUS_MODE_DEFAULT_KEY = booleanPreferencesKey("anonymous_mode_default")
        private val NOTIFICATION_RADIUS_KEY = stringPreferencesKey("notification_radius") // em km
        private val MAP_TYPE_KEY = stringPreferencesKey("map_type") // "standard", "satellite"
        private val AUTO_DAY_NIGHT_MODE_KEY = booleanPreferencesKey("auto_day_night_mode")
        private val USER_NICKNAME_KEY = stringPreferencesKey("user_nickname")
        private val USER_COLOR_KEY = stringPreferencesKey("user_color") // cor de destaque
    }

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val username: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    val email: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    suspend fun saveAuthData(token: String, userId: String, username: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[EMAIL_KEY] = email
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        var token: String? = null
        context.dataStore.data.map { preferences ->
            token = preferences[TOKEN_KEY]
        }.collect { }
        return !token.isNullOrEmpty()
    }

    // Theme preferences
    val themeMode: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    // Anonymous mode default
    val anonymousModeDefault: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ANONYMOUS_MODE_DEFAULT_KEY] ?: false
    }

    suspend fun setAnonymousModeDefault(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ANONYMOUS_MODE_DEFAULT_KEY] = enabled
        }
    }

    // Notification radius (km)
    val notificationRadius: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_RADIUS_KEY] ?: "5"
    }

    suspend fun setNotificationRadius(radius: String) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_RADIUS_KEY] = radius
        }
    }

    // Map type
    val mapType: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[MAP_TYPE_KEY] ?: "standard"
    }

    suspend fun setMapType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[MAP_TYPE_KEY] = type
        }
    }
    
    // Auto day/night mode
    val autoDayNightMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_DAY_NIGHT_MODE_KEY] ?: true
    }

    suspend fun setAutoDayNightMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_DAY_NIGHT_MODE_KEY] = enabled
        }
    }

    // User nickname
    val userNickname: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NICKNAME_KEY]
    }

    suspend fun setUserNickname(nickname: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NICKNAME_KEY] = nickname
        }
    }

    // User color
    val userColor: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_COLOR_KEY] ?: "#1E3A8A" // azul marinho padrão
    }

    suspend fun setUserColor(color: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_COLOR_KEY] = color
        }
    }

    // User description
    private val USER_DESCRIPTION_KEY = stringPreferencesKey("user_description")

    val userDescription: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_DESCRIPTION_KEY]
    }

    suspend fun setUserDescription(description: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_DESCRIPTION_KEY] = description
        }
    }
}

