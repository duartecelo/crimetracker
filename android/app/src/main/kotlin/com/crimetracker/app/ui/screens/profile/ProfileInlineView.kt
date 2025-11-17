package com.crimetracker.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.data.model.Post
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInlineView(
    modifier: Modifier = Modifier,
    onNavigateToFullProfile: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Informações, 1 = Posts
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var editingDescription by remember { mutableStateOf(uiState.description) }

    LaunchedEffect(Unit) {
        viewModel.loadUserPosts()
    }

    LaunchedEffect(uiState.description) {
        editingDescription = uiState.description
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 72.dp) // Espaço para o botão de logout
        ) {
            // Header com foto e nome
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto de perfil
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color(android.graphics.Color.parseColor(uiState.userColor))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.nickname.ifEmpty { uiState.username }.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = uiState.nickname.ifEmpty { uiState.username },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Botão para ver perfil completo
                IconButton(
                    onClick = onNavigateToFullProfile,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, "Ver perfil completo")
                }
            }

            // Descrição
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDescriptionDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Descrição",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.description.isNotEmpty()) {
                                uiState.description
                            } else {
                                "Adicione uma descrição sobre você"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.description.isNotEmpty()) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(
                        onClick = { showDescriptionDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            "Editar descrição",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs: Informações e Posts
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Informações") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { 
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Posts")
                            if (uiState.posts.isNotEmpty()) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${uiState.posts.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conteúdo das tabs
            when (selectedTab) {
                0 -> {
                    // Informações
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Personalização
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Personalização",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

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
                                        listOf(
                                            "#1E3A8A", "#3B82F6", "#60A5FA", "#10B981", "#F59E0B"
                                        ).forEach { color ->
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

                        // Botão de configurações
                        Button(
                            onClick = onNavigateToFullProfile,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Settings, "Configurações", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configurações")
                        }
                    }
                }
                1 -> {
                    // Posts
                    if (uiState.isLoadingPosts) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.posts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Forum,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Nenhum post ainda",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Seus posts em comunidades aparecerão aqui",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.posts) { post ->
                                PostCard(post = post)
                            }
                        }
                    }
                }
            }
        }

        // Botão de logout fixo no bottom
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Logout, "Sair", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sair da Conta")
        }
    }

    // Dialog de descrição
    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text("Editar Descrição") },
            text = {
                OutlinedTextField(
                    value = editingDescription,
                    onValueChange = { 
                        if (it.length <= 200) editingDescription = it 
                    },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${editingDescription.length}/200") },
                    maxLines = 4
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateDescription(editingDescription)
                        showDescriptionDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDescriptionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sair da Conta") },
            text = { Text("Tem certeza que deseja sair?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout {
                            showLogoutDialog = false
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    post.groupName?.let { groupName ->
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = formatDate(post.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = post.conteudo,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        try {
            val inputFormat2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
            val date = inputFormat2.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e2: Exception) {
            dateString
        }
    }
}
