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
    onNavigateToReportCrime: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onReportClick: (String) -> Unit = {}, // reportId
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            if (selectedTab == 1) {
                // TopBar apenas para aba Comunidade com botão de voltar
                TopAppBar(
                    title = { Text("Comunidade") },
                    navigationIcon = {
                        IconButton(onClick = { selectedTab = 0 }) {
                            Icon(Icons.Default.ArrowBack, "Voltar")
                        }
                    }
                )
            }
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
                // Mostrar placeholder de Comunidade com botão de voltar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "👥",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Comunidade",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gerencie seus grupos de vizinhança",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { onNavigateToCommunity() },
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text("Ver Comunidade")
                        }
                    }
                }
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

