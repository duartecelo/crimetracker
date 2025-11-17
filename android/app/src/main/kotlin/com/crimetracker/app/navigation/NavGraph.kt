package com.crimetracker.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.crimetracker.app.ui.screens.auth.ForgotPasswordScreen
import com.crimetracker.app.ui.screens.auth.LoginScreen
import com.crimetracker.app.ui.screens.auth.RegisterScreen
import com.crimetracker.app.ui.screens.auth.ResetPasswordScreen
import com.crimetracker.app.ui.screens.group.CreateGroupScreen
import com.crimetracker.app.ui.screens.home.HomeScreen
import com.crimetracker.app.ui.screens.home.MapHomeScreen
import com.crimetracker.app.ui.screens.map.MapScreen
import com.crimetracker.app.ui.screens.post.CreatePostScreen
import com.crimetracker.app.ui.screens.profile.ProfileScreen
import com.crimetracker.app.ui.screens.settings.SettingsScreen
import com.crimetracker.app.ui.screens.report.ReportCrimeScreen
import com.crimetracker.app.ui.screens.splash.SplashScreen
import com.crimetracker.app.ui.screens.community.CommunityScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReset = { email ->
                    navController.navigate(Screen.ResetPassword.createRoute(email))
                }
            )
        }
        
        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(
                email = email,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPasswordReset = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Home - Tela principal com mapa
        composable(Screen.Home.route) {
            MapHomeScreen(
                onNavigateToReportCrime = {
                    navController.navigate(Screen.ReportCrime.route)
                },
                onNavigateToCommunity = {
                    navController.navigate(Screen.Community.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onReportClick = { reportId ->
                    // TODO: Navegar para detalhes do report
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Map standalone
        composable(Screen.Map.route) {
            MapScreen(
                onNavigateToCreateReport = {
                    navController.navigate(Screen.ReportCrime.route)
                },
                onReportClick = { report ->
                    // TODO: Navegar para detalhes do report
                }
            )
        }

        // Report Crime
        composable(Screen.ReportCrime.route) {
            ReportCrimeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Create Group
        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Create Post
        composable(
            route = Screen.CreatePost.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            CreatePostScreen(
                groupId = groupId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Profile
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Community
        composable(Screen.Community.route) {
            CommunityScreen(
                onNavigateToCreateGroup = {
                    navController.navigate(Screen.CreateGroup.route)
                },
                onNavigateToGroup = { groupId ->
                    // TODO: Navegar para tela de detalhes do grupo
                }
            )
        }
    }
}

