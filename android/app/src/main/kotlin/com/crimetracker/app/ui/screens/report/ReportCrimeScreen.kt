package com.crimetracker.app.ui.screens.report

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crimetracker.app.data.local.MapTheme
import com.crimetracker.app.data.local.UserPreferences
import com.crimetracker.app.util.LocationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportCrimeScreen(
    onNavigateBack: () -> Unit,
    initialLat: Double? = null,
    initialLon: Double? = null,
    viewModel: ReportViewModel = hiltViewModel()
) {
    var tipo by remember { mutableStateOf("Roubo/Assalto com violência ou ameaça") }
    var descricao by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var useCurrentLocation by remember { mutableStateOf(true) }
    
    // Initialize manual coords with passed values or 0.0
    var manualLatitude by remember { mutableStateOf(initialLat ?: 0.0) }
    var manualLongitude by remember { mutableStateOf(initialLon ?: 0.0) }
    
    var currentLatitude by remember { mutableStateOf<Double?>(null) }
    var currentLongitude by remember { mutableStateOf<Double?>(null) }
    
    // State to control showing the picker
    var showLocationPicker by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Get map theme from preferences (reactive)
    val userPreferences = remember { UserPreferences(context) }
    val mapTheme by userPreferences.mapTheme.collectAsState(initial = MapTheme.SYSTEM)

    // Novas categorias unificadas
    val crimeTypes = listOf(
        "Roubo/Assalto com violência ou ameaça",
        "Furto (sem violência)",
        "Furto/Roubo de veículo",
        "Outros crimes patrimoniais"
    )
    
    // Mapeamento para valores do backend
    val crimeTypeMap = mapOf(
        "Roubo/Assalto com violência ou ameaça" to "Roubo",
        "Furto (sem violência)" to "Furto",
        "Furto/Roubo de veículo" to "Roubo",
        "Outros crimes patrimoniais" to "Outro"
    )

    // Carregar localização atual ao abrir a tela
    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            val location = LocationHelper.getCurrentLocation(context)
            if (location != null) {
                currentLatitude = location.first
                currentLongitude = location.second
                // Initialize manual coords with current location if not set and no initial passed
                if (manualLatitude == 0.0) {
                    manualLatitude = location.first
                    manualLongitude = location.second
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                val location = LocationHelper.getCurrentLocation(context)
                if (location != null) {
                    currentLatitude = location.first
                    currentLongitude = location.second
                    if (useCurrentLocation) {
                        val backendTipo = crimeTypeMap[tipo] ?: "Outro"
                        viewModel.createReport(backendTipo, descricao, location.first, location.second)
                    }
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permissão de localização negada.")
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

    if (showLocationPicker) {
        LocationPickerScreen(
            initialLat = if (manualLatitude != 0.0) manualLatitude else (currentLatitude ?: -23.5505),
            initialLon = if (manualLongitude != 0.0) manualLongitude else (currentLongitude ?: -46.6333),
            mapTheme = mapTheme,
            onLocationSelected = { lat, lon ->
                manualLatitude = lat
                manualLongitude = lon
                useCurrentLocation = false
                showLocationPicker = false
            },
            onCancel = {
                showLocationPicker = false
            }
        )
    } else {
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
                

                
                // Seção de Localização
                Text(
                    text = "Localização",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botão Localização Atual
                    OutlinedButton(
                        onClick = { useCurrentLocation = true },
                        modifier = Modifier.weight(1f),
                        colors = if (useCurrentLocation) 
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) 
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Atual")
                    }
                    
                    // Botão Selecionar no Mapa
                    OutlinedButton(
                        onClick = { showLocationPicker = true },
                        modifier = Modifier.weight(1f),
                        colors = if (!useCurrentLocation) 
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) 
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Mapa")
                    }
                }
                
                if (!useCurrentLocation) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selecionado: ${String.format("%.4f", manualLatitude)}, ${String.format("%.4f", manualLongitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        val lat: Double
                        val lon: Double
                        
                        if (useCurrentLocation) {
                            if (currentLatitude != null && currentLongitude != null) {
                                lat = currentLatitude!!
                                lon = currentLongitude!!
                                val backendTipo = crimeTypeMap[tipo] ?: "Outro"
                                viewModel.createReport(backendTipo, descricao, lat, lon)
                            } else if (LocationHelper.hasLocationPermission(context)) {
                                scope.launch {
                                    val location = LocationHelper.getCurrentLocation(context)
                                    if (location != null) {
                                        val backendTipo = crimeTypeMap[tipo] ?: "Outro"
                                        viewModel.createReport(backendTipo, descricao, location.first, location.second)
                                    }
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        } else {
                            // Usar posição manual (do mapa)
                            lat = manualLatitude
                            lon = manualLongitude
                            val backendTipo = crimeTypeMap[tipo] ?: "Outro"
                            viewModel.createReport(backendTipo, descricao, lat, lon)
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
}

