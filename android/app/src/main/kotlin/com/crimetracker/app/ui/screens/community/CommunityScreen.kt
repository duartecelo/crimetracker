package com.crimetracker.app.ui.screens.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.data.model.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToGroup: (String) -> Unit, // groupId
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    // Não mostrar erros como pop-up
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidade") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateGroup) {
                Icon(Icons.Default.Add, "Criar grupo")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.groups.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "👥",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = "Nenhum grupo encontrado",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Crie ou participe de grupos da sua região",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.groups) { group ->
                        GroupCard(
                            group = group,
                            onClick = { onNavigateToGroup(group.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupCard(
    group: Group,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = group.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${group.memberCount} membros",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            group.descricao?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Bairro", // TODO: adicionar campo bairro no model Group
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (group.criadorUsername != null) {
                Text(
                    text = "Criado por ${group.criadorUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

