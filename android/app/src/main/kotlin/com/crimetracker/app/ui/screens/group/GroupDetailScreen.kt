package com.crimetracker.app.ui.screens.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.ui.components.PostCard
import com.crimetracker.app.ui.components.CreatePostDialog
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onBackClick: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreatePostDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.nome ?: "Carregando...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleImportantFilter() }) {
                        Icon(
                            imageVector = if (uiState.isImportantFilterActive) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Filtrar Importantes",
                            tint = if (uiState.isImportantFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isMember) {
                FloatingActionButton(onClick = { showCreatePostDialog = true }) {
                    Icon(Icons.Default.Add, "Novo Post")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            
            // Header com Capa
            uiState.group?.coverUrl?.let { coverUrl ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "Capa do Grupo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradiente para texto legível se necessário
                }
            }

            // Banner de Preview / Botão de Entrar
            if (!uiState.isMember && !uiState.isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Você está em modo de visualização",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = "Entre no grupo para interagir e postar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.joinGroup() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Entrar no Grupo")
                        }
                    }
                }
            }

            // Lista de Posts
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Nenhum post encontrado.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredPosts) { post ->
                        PostCard(
                            post = post,
                            onLikeClick = { 
                                if (uiState.isMember) viewModel.likePost(post.id) 
                                else { /* Mostrar snackbar pedindo pra entrar */ }
                            },
                            onDislikeClick = { 
                                if (uiState.isMember) viewModel.dislikePost(post.id)
                                else { /* Mostrar snackbar */ }
                            },
                            onCommentClick = { /* TODO */ },
                            onShareClick = { /* TODO */ }
                        )
                    }
                }
            }
        }

        if (showCreatePostDialog) {
            SimpleCreatePostDialog(
                onDismiss = { showCreatePostDialog = false },
                onPost = { content ->
                    viewModel.createPost(content)
                    showCreatePostDialog = false
                }
            )
        }
    }
}

@Composable
fun SimpleCreatePostDialog(
    onDismiss: () -> Unit,
    onPost: (String) -> Unit
) {
    var content by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Post") },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("O que está acontecendo?") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5
            )
        },
        confirmButton = {
            Button(
                onClick = { onPost(content) },
                enabled = content.isNotBlank()
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
