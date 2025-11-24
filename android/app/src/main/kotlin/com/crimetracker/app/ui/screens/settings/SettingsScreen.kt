package com.crimetracker.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.data.local.MapTheme
import com.crimetracker.app.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val anonymousModeDefault by viewModel.anonymousModeDefault.collectAsState()
    val notificationRadius by viewModel.notificationRadius.collectAsState()
    val mapType by viewModel.mapType.collectAsState()
    val autoDayNightMode by viewModel.autoDayNightMode.collectAsState()
    val mapTheme by viewModel.mapTheme.collectAsState()

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção de Tema Unificado (App + Mapa)
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
                        text = "Tema (App e Mapa)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeMode == ThemeMode.LIGHT && mapTheme == MapTheme.LIGHT,
                            onClick = { 
                                viewModel.setThemeMode(ThemeMode.LIGHT)
                                viewModel.setMapTheme(MapTheme.LIGHT)
                            },
                            label = { Text("Claro") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.DARK && mapTheme == MapTheme.DARK,
                            onClick = { 
                                viewModel.setThemeMode(ThemeMode.DARK)
                                viewModel.setMapTheme(MapTheme.DARK)
                            },
                            label = { Text("Escuro") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = mapTheme == MapTheme.AUTO,
                            onClick = { 
                                viewModel.setMapTheme(MapTheme.AUTO)
                                // Auto mode will handle theme switching based on time
                            },
                            label = { Text("Auto") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.SYSTEM && mapTheme == MapTheme.SYSTEM,
                            onClick = { 
                                viewModel.setThemeMode(ThemeMode.SYSTEM)
                                viewModel.setMapTheme(MapTheme.SYSTEM)
                            },
                            label = { Text("Sistema") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Helper text
                    Text(
                        text = when {
                            mapTheme == MapTheme.AUTO -> "Modo automático: Claro durante o dia (6h-18h), Escuro à noite (18h-6h)"
                            themeMode == ThemeMode.LIGHT && mapTheme == MapTheme.LIGHT -> "App e mapa sempre claros"
                            themeMode == ThemeMode.DARK && mapTheme == MapTheme.DARK -> "App e mapa sempre escuros"
                            themeMode == ThemeMode.SYSTEM && mapTheme == MapTheme.SYSTEM -> "Segue configuração do sistema"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        var showAnonymousDialog by remember { mutableStateOf(false) }

                        if (showAnonymousDialog) {
                            AlertDialog(
                                onDismissRequest = { showAnonymousDialog = false },
                                title = { Text("Desativar modo anônimo?") },
                                text = { Text("Você tem certeza que deseja desativar o modo anônimo padrão? Suas denúncias poderão ser identificadas.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.setAnonymousModeDefault(false)
                                            showAnonymousDialog = false
                                        }
                                    ) {
                                        Text("Sim, desativar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showAnonymousDialog = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }

                        Switch(
                            checked = anonymousModeDefault,
                            onCheckedChange = { 
                                if (!it) {
                                    showAnonymousDialog = true
                                } else {
                                    viewModel.setAnonymousModeDefault(true)
                                }
                            }
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

            // Botão de Sair
            Button(
                onClick = { viewModel.signOut(onSignOut) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sair da conta")
            }
        }
    }
}

