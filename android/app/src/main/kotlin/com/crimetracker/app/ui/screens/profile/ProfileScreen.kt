package com.crimetracker.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
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
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                actions = {
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(Icons.Default.Edit, "Editar Perfil")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Configurações")
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
                val userColor = try {
                    Color(android.graphics.Color.parseColor(uiState.userColor))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                
                val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
                val borderColor = if (isDarkTheme) Color.White else Color.DarkGray
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = userColor,
                    border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.nickname.take(1).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = if (com.crimetracker.app.util.ColorUtils.isDarkColor(userColor)) Color.White else Color.Black
                        )
                    }
                }
            }

            // Nome/Apelido
            Text(
                text = uiState.nickname.ifEmpty { uiState.username },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Email
            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Descrição
            if (uiState.description.isNotEmpty()) {
                Text(
                    text = uiState.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Divider()
            
            // Posts
            Text(
                text = "Minhas Publicações",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            
            if (uiState.isLoadingPosts) {
                CircularProgressIndicator()
            } else if (uiState.posts.isEmpty()) {
                Text(
                    text = "Nenhuma publicação ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.posts.size) { index ->
                        val post = uiState.posts[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = post.conteudo,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = post.createdAt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

