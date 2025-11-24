package com.crimetracker.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    fun checkAuthStatus(onLoggedIn: () -> Unit, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            // Aguardar um pouco para mostrar a splash
            delay(1500)
            
            val token = userPreferences.authToken.first()
            if (!token.isNullOrEmpty()) {
                onLoggedIn()
            } else {
                onLoggedOut()
            }
        }
    }
}
