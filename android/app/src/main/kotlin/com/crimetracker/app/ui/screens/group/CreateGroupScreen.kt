package com.crimetracker.app.ui.screens.group

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    viewModel: GroupViewModel = hiltViewModel()
) {
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    
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
                title = { Text("Criar Grupo") },
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
                text = "Nome do Grupo",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: Bairro Centro") },
                singleLine = true,
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Descrição (opcional)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Descreva o objetivo do grupo...") },
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    viewModel.createGroup(nome, descricao.ifBlank { null })
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && nome.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Criar Grupo")
                }
            }
        }
    }
}

