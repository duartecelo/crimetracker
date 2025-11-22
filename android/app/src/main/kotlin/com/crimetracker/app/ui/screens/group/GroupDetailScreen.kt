package com.crimetracker.app.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.ui.components.PostCard
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

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.nome ?: "A carregar...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = uiState.isImportantFilterActive,
                        onCheckedChange = { viewModel.toggleImportantFilter() }
                    ) {
                        Icon(
                            imageVector = if (uiState.isImportantFilterActive) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Filtrar Importantes",
                            tint = if (uiState.isImportantFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isMember) {
                FloatingActionButton(
                    onClick = { showCreatePostDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Edit, "Novo Post")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = uiState.group?.coverUrl ?: "https://via.placeholder.com/500",
                    contentDescription = "Capa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = uiState.group?.nome ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.group?.descricao ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2
                    )
                    Text(
                        text = "${uiState.group?.memberCount ?: 0} membros",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            if (!uiState.isMember && !uiState.isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Pré-visualização", 
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Entre na comunidade para interagir, postar fotos e vídeos.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.joinGroup() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Entrar na Comunidade")
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.posts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val postsToShow = uiState.filteredPosts
                    
                    if (postsToShow.isEmpty()) {
                        item {
                            Text(
                                "Ainda não há publicações.",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(postsToShow) { post ->
                            PostCard(
                                post = post,
                                onLikeClick = { if(uiState.isMember) viewModel.likePost(post.id) },
                                onDislikeClick = { if(uiState.isMember) viewModel.dislikePost(post.id) },
                                onCommentClick = { },
                                onShareClick = { }
                            )
                        }
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
                label = { Text("O que está a acontecer?") },
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
