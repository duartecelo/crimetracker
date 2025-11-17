package com.crimetracker.app.ui.screens.map

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.crimetracker.app.data.model.Report
import com.crimetracker.app.ui.components.ReportFeedbackSection
import com.crimetracker.app.ui.components.ReportAbuseDialog
import com.crimetracker.app.util.LocationHelper
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onReportClick: (Report) -> Unit = {},
    onNavigateToCreateReport: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showAbuseDialog by remember { mutableStateOf(false) }
    var mapViewRef: MapView? by remember { mutableStateOf(null) }

    // Configurar OSM
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "CrimeTracker/1.0"
    }

    // Solicitar permissão de localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                val location = LocationHelper.getCurrentLocation(context)
                location?.let { (lat, lon) ->
                    viewModel.setUserLocation(lat, lon)
                    viewModel.loadReports(lat, lon)
                }
            }
        }
    }

    // Carregar localização inicial
    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            val location = LocationHelper.getCurrentLocation(context)
            location?.let { (lat, lon) ->
                viewModel.setUserLocation(lat, lon)
                viewModel.loadReports(lat, lon)
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Não mostrar erros como pop-up, apenas limpar estado
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearError()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView: MapView? by remember { mutableStateOf(null) }
    
    // Gerenciar ciclo de vida do MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onPause()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Mapa OSM
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    minZoomLevel = 5.0
                    maxZoomLevel = 19.0
                    setMultiTouchControls(true)
                    
                    // Localização inicial (São Paulo)
                    val initialLocation = GeoPoint(-23.5505, -46.6333)
                    controller.setCenter(initialLocation)
                    controller.setZoom(15.0)
                    
                    mapView = this
                    mapViewRef = this
                }
            },
            update = { view ->
                // Alternar entre mapa normal e satélite
                val tileSource = if (uiState.isSatelliteMode) {
                    TileSourceFactory.USGS_SAT
                } else {
                    TileSourceFactory.MAPNIK
                }
                if (view.tileProvider.tileSource != tileSource) {
                    view.setTileSource(tileSource)
                }
                
                // Atualizar marcadores quando reports mudarem
                view.overlays.clear()
                
                // Overlay de localização do usuário
                if (uiState.userLocation != null) {
                    val locationOverlay = MyLocationNewOverlay(view)
                    locationOverlay.enableMyLocation()
                    view.overlays.add(locationOverlay)
                    
                    view.controller.setCenter(
                        GeoPoint(uiState.userLocation!!.first, uiState.userLocation!!.second)
                    )
                    view.controller.setZoom(15.0)
                }
                
                // Adicionar marcadores de crimes
                uiState.reports.forEach { report ->
                    val marker = org.osmdroid.views.overlay.Marker(view).apply {
                        position = GeoPoint(report.lat, report.lon)
                        title = report.tipo
                        snippet = report.descricao.take(100)
                        setOnMarkerClickListener { _, _ ->
                            viewModel.selectReport(report)
                            onReportClick(report)
                            true
                        }
                    }
                    view.overlays.add(marker)
                }
                
                view.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Botões flutuantes no topo direito
        Column(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botão de satélite
            FloatingActionButton(
                onClick = { viewModel.toggleSatelliteMode() },
                modifier = Modifier.size(48.dp),
                containerColor = if (uiState.isSatelliteMode) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                }
            ) {
                Icon(
                    if (uiState.isSatelliteMode) Icons.Default.Layers else Icons.Default.Map,
                    "Satélite"
                )
            }

            // Botão de filtros
            FloatingActionButton(
                onClick = { /* Filtros */ },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Icon(Icons.Default.FilterList, "Filtros")
            }

            // Botão de centralizar localização
            if (uiState.userLocation != null) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            val location = LocationHelper.getCurrentLocation(context)
                            location?.let { (lat, lon) ->
                                viewModel.setUserLocation(lat, lon)
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Icon(Icons.Default.MyLocation, "Minha localização")
                }
            }
        }

        // FAB para criar denúncia (vermelho)
        FloatingActionButton(
            onClick = onNavigateToCreateReport,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.error
        ) {
            Icon(Icons.Default.Add, "Reportar Crime")
        }

        // Card de detalhes do report selecionado
        uiState.selectedReport?.let { report ->
            Card(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = report.tipo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.selectReport(null) }) {
                            Icon(Icons.Default.Close, "Fechar")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = report.descricao,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Data: ${report.createdAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (report.authorUsername != null && report.authorUsername != "Anônimo") {
                        Text(
                            text = "Autor: ${report.authorUsername}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Autor: Anônimo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Seção de feedback
                    ReportFeedbackSection(
                        report = report,
                        onUsefulClick = {
                            scope.launch {
                                viewModel.submitFeedback(report.id, "useful")
                            }
                        },
                        onNotUsefulClick = {
                            scope.launch {
                                viewModel.submitFeedback(report.id, "not_useful")
                            }
                        },
                        onReportAbuse = {
                            showAbuseDialog = true
                        }
                    )
                }
            }
        }
        
        // Dialog de reportar abuso
        if (showAbuseDialog && uiState.selectedReport != null) {
            ReportAbuseDialog(
                onDismiss = { showAbuseDialog = false },
                onConfirm = { reason, description ->
                    scope.launch {
                        viewModel.reportAbuse(uiState.selectedReport!!.id, reason, description)
                        showAbuseDialog = false
                    }
                }
            )
        }

    }
}
