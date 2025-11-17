package com.crimetracker.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val anonymousModeDefault by viewModel.anonymousModeDefault.collectAsState()
    val notificationRadius by viewModel.notificationRadius.collectAsState()
    val mapType by viewModel.mapType.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção de Tema
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Aparência",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Tema",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                            label = { Text("Claro") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                            label = { Text("Escuro") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                            label = { Text("Sistema") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Seção de Denúncias
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Denúncias",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo anônimo padrão",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Enviar denúncias como anônimo por padrão",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = anonymousModeDefault,
                            onCheckedChange = { viewModel.setAnonymousModeDefault(it) }
                        )
                    }
                }
            }

            // Seção de Notificações
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Notificações",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Raio de alerta de crimes (km)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1", "3", "5", "10", "20").forEach { radius ->
                            FilterChip(
                                selected = notificationRadius == radius,
                                onClick = { viewModel.setNotificationRadius(radius) },
                                label = { Text("$radius km") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Seção de Mapa
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Mapa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Tipo de visualização",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = mapType == "standard",
                            onClick = { viewModel.setMapType("standard") },
                            label = { Text("Padrão") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = mapType == "satellite",
                            onClick = { viewModel.setMapType("satellite") },
                            label = { Text("Satélite") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

