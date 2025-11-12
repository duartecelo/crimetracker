package com.crimetracker.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ReportCrime : Screen("report_crime")
    object CreateGroup : Screen("create_group")
    object CreatePost : Screen("create_post/{groupId}") {
        fun createRoute(groupId: String) = "create_post/$groupId"
    }
    object Profile : Screen("profile")
}

