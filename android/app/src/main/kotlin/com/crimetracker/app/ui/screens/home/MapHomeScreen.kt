package com.crimetracker.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.ui.screens.map.MapScreen
import com.crimetracker.app.ui.screens.profile.ProfileInlineView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapHomeScreen(
    onNavigateToReportCrime: (Double, Double) -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onReportClick: (String) -> Unit = {}, // reportId
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            // TopBar removida daqui pois cada tela gerencia a sua (ou não precisa)
            // CommunityScreen já tem sua própria TopAppBar
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Map, "Mapa") },
                    label = { Text("Mapa") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                    },
                    icon = { Icon(Icons.Default.Group, "Comunidade") },
                    label = { Text("Comunidade") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                    },
                    icon = { Icon(Icons.Default.Person, "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> {
                MapScreen(
                    modifier = Modifier.padding(padding),
                    onNavigateToCreateReport = onNavigateToReportCrime,
                    onReportClick = { report ->
                    onReportClick(report.id)
                }
            )
            }
            1 -> {
                // Mostrar tela de Comunidade diretamente
                com.crimetracker.app.ui.screens.community.CommunityScreen(
                    onNavigateToCreateGroup = onNavigateToCreateGroup,
                    onNavigateToGroup = onNavigateToGroup
                )
            }
            2 -> {
                // Mostrar visualização direta do Perfil
                ProfileInlineView(
                    modifier = Modifier.padding(padding),
                    onNavigateToFullProfile = onNavigateToProfile,
                    onLogout = onLogout
                )
            }
        }
    }
}

