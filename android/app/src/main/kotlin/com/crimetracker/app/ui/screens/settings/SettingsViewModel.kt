package com.crimetracker.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.local.MapTheme
import com.crimetracker.app.data.local.UserPreferences
import com.crimetracker.app.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _themeMode = MutableStateFlow<ThemeMode>(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _anonymousModeDefault = MutableStateFlow(true)
    val anonymousModeDefault: StateFlow<Boolean> = _anonymousModeDefault.asStateFlow()

    private val _notificationRadius = MutableStateFlow("5")
    val notificationRadius: StateFlow<String> = _notificationRadius.asStateFlow()

    private val _mapType = MutableStateFlow("standard")
    val mapType: StateFlow<String> = _mapType.asStateFlow()
    
    private val _autoDayNightMode = MutableStateFlow(true)
    val autoDayNightMode: StateFlow<Boolean> = _autoDayNightMode.asStateFlow()

    private val _mapTheme = MutableStateFlow(MapTheme.SYSTEM)
    val mapTheme: StateFlow<MapTheme> = _mapTheme.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferences.themeMode.first()?.let { mode ->
                _themeMode.value = when (mode) {
                    "light" -> ThemeMode.LIGHT
                    "dark" -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
            }
            
            userPreferences.anonymousModeDefault.first()?.let {
                _anonymousModeDefault.value = it
            }
            
            userPreferences.notificationRadius.first()?.let {
                _notificationRadius.value = it
            }
            
            userPreferences.mapType.first()?.let {
                _mapType.value = it
            }
            
            userPreferences.autoDayNightMode.first().let {
                _autoDayNightMode.value = it
            }
            
            userPreferences.mapTheme.first().let {
                _mapTheme.value = it
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            userPreferences.setThemeMode(
                when (mode) {
                    ThemeMode.LIGHT -> "light"
                    ThemeMode.DARK -> "dark"
                    ThemeMode.SYSTEM -> "system"
                }
            )
        }
    }

    fun setAnonymousModeDefault(enabled: Boolean) {
        _anonymousModeDefault.value = enabled
        viewModelScope.launch {
            userPreferences.setAnonymousModeDefault(enabled)
        }
    }

    fun setNotificationRadius(radius: String) {
        _notificationRadius.value = radius
        viewModelScope.launch {
            userPreferences.setNotificationRadius(radius)
        }
    }

    fun setMapType(type: String) {
        _mapType.value = type
        viewModelScope.launch {
            userPreferences.setMapType(type)
        }
    }
    
    fun setAutoDayNightMode(enabled: Boolean) {
        _autoDayNightMode.value = enabled
        viewModelScope.launch {
            userPreferences.setAutoDayNightMode(enabled)
        }
    }
    
    fun setMapTheme(theme: MapTheme) {
        _mapTheme.value = theme
        viewModelScope.launch {
            userPreferences.setMapTheme(theme)
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        viewModelScope.launch {
            userPreferences.clearAuthData()
            onSignOut()
        }
    }
}

