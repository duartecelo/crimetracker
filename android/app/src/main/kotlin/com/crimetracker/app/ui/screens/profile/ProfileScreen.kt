package com.crimetracker.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var editingNickname by remember { mutableStateOf(uiState.nickname) }

    // Cores pré-definidas compatíveis com a paleta
    val availableColors = listOf(
        "#1E3A8A", // Azul marinho
        "#3B82F6", // Azul claro
        "#60A5FA", // Azul destaque
        "#10B981", // Verde
        "#F59E0B"  // Laranja
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto de perfil (placeholder)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder - em produção seria Image composable com foto do usuário
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color(android.graphics.Color.parseColor(uiState.userColor))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.nickname.take(1).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White
                        )
                    }
                }
            }

            // Nome/Apelido
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = uiState.nickname.ifEmpty { uiState.username },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { 
                    editingNickname = uiState.nickname.ifEmpty { uiState.username }
                    showNicknameDialog = true 
                }) {
                    Icon(Icons.Default.Edit, "Editar apelido")
                }
            }

            // Email
            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Personalização
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Personalização",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Cor de destaque
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cor de destaque",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Escolha sua cor favorita",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableColors.forEach { color ->
                                Surface(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { viewModel.updateUserColor(color) },
                                    shape = CircleShape,
                                    color = Color(android.graphics.Color.parseColor(color)),
                                    border = if (uiState.userColor == color) {
                                        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    } else null
                                ) {}
                            }
                        }
                    }
                }
            }

            // Configurações
            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configurações")
            }
        }

        // Dialog para editar nickname
        if (showNicknameDialog) {
            AlertDialog(
                onDismissRequest = { showNicknameDialog = false },
                title = { Text("Editar Apelido") },
                text = {
                    OutlinedTextField(
                        value = editingNickname,
                        onValueChange = { 
                            if (it.length <= 30) editingNickname = it 
                        },
                        label = { Text("Apelido") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("${editingNickname.length}/30") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateNickname(editingNickname)
                            showNicknameDialog = false
                        }
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNicknameDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

