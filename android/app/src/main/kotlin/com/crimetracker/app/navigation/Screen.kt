package com.crimetracker.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{email}") {
        fun createRoute(email: String) = "reset_password/$email"
    }
    object Home : Screen("home")
    object Map : Screen("map")
    object ReportCrime : Screen("report_crime")
    object CreateGroup : Screen("create_group")
    object CreatePost : Screen("create_post/{groupId}") {
        fun createRoute(groupId: String) = "create_post/$groupId"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Community : Screen("community")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
}

