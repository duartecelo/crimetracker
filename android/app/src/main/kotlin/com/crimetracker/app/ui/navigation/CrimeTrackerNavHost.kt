package com.crimetracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crimetracker.app.ui.screens.auth.LoginScreen
import com.crimetracker.app.ui.screens.home.HomeScreen

@Composable
fun CrimeTrackerNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(onNavigateToHome = {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        composable("home") {
            HomeScreen()
        }
    }
}

