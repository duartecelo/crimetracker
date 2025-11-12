package com.crimetracker.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.ui.screens.auth.AuthViewModel
import com.crimetracker.app.ui.screens.home.tabs.FeedTab
import com.crimetracker.app.ui.screens.home.tabs.GroupsTab
import com.crimetracker.app.ui.screens.home.tabs.ReportsTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToReportCrime: () -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToCreatePost: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CrimeTracker") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Perfil") },
                            onClick = {
                                showMenu = false
                                onNavigateToProfile()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, "Perfil")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sair") },
                            onClick = {
                                showMenu = false
                                authViewModel.logout()
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ExitToApp, "Sair")
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, "Feed") },
                    label = { Text("Feed") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Warning, "Denúncias") },
                    label = { Text("Denúncias") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Group, "Grupos") },
                    label = { Text("Grupos") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> FeedTab(
                modifier = Modifier.padding(padding),
                onNavigateToCreatePost = onNavigateToCreatePost
            )
            1 -> ReportsTab(
                modifier = Modifier.padding(padding),
                onNavigateToReportCrime = onNavigateToReportCrime
            )
            2 -> GroupsTab(
                modifier = Modifier.padding(padding),
                onNavigateToCreateGroup = onNavigateToCreateGroup
            )
        }
    }
}
