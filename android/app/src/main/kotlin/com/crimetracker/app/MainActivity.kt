package com.crimetracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.crimetracker.app.data.local.UserPreferences
import com.crimetracker.app.navigation.NavGraph
import com.crimetracker.app.ui.screens.settings.SettingsViewModel
import com.crimetracker.app.ui.theme.CrimeTrackerTheme
import com.crimetracker.app.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeModeString by userPreferences.themeMode.collectAsState(initial = "system")
            val themeMode = when (themeModeString) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }

            CrimeTrackerTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}

