package com.crimetracker.app.ui.screens.report

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.util.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportCrimeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    var tipo by remember { mutableStateOf("Roubo/Assalto com violência ou ameaça") }
    var descricao by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isAnonymous by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Novas categorias unificadas
    val crimeTypes = listOf(
        "Roubo/Assalto com violência ou ameaça",
        "Furto (sem violência)",
        "Furto/Roubo de veículo",
        "Outros crimes patrimoniais"
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                val location = LocationHelper.getCurrentLocation(context)
                if (location != null) {
                    viewModel.createReport(tipo, descricao, location.first, location.second)
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permissão de localização negada. Usando localização padrão.")
                val location = Pair(-23.5505, -46.6333) // São Paulo
                viewModel.createReport(tipo, descricao, location.first, location.second)
            }
        }
    }

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
                title = { Text("Reportar Crime") },
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
                text = "Tipo de Crime",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    crimeTypes.forEach { crimeType ->
                        DropdownMenuItem(
                            text = { Text(crimeType) },
                            onClick = {
                                tipo = crimeType
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Descrição",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = descricao,
                onValueChange = { 
                    if (it.length <= 500) descricao = it 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Descreva o que aconteceu...") },
                supportingText = { Text("${descricao.length}/500") },
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Switch para modo anônimo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enviar como anônimo",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Sua identidade não será divulgada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isAnonymous,
                    onCheckedChange = { isAnonymous = it }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (LocationHelper.hasLocationPermission(context)) {
                        scope.launch {
                            val location = LocationHelper.getCurrentLocation(context)
                            if (location != null) {
                                viewModel.createReport(tipo, descricao, location.first, location.second)
                            }
                        }
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && descricao.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Reportar")
                }
            }
        }
    }
}

