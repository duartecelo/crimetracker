package com.crimetracker.app.ui.screens.post

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.ui.screens.feed.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    var conteudo by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensagens
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
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Conteúdo do Post",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = conteudo,
                onValueChange = { 
                    if (it.length <= 1000) conteudo = it 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = { Text("O que você quer compartilhar?") },
                supportingText = { Text("${conteudo.length}/1000") },
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    viewModel.createPost(groupId, conteudo)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && conteudo.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Publicar")
                }
            }
        }
    }
}

