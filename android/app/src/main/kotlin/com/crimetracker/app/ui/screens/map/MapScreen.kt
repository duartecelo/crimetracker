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

    // Fontes de mapa (Mundo todo)
    val standardSource = remember { TileSourceFactory.MAPNIK }
    val satelliteSource = remember {
        object : org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
            "Esri World Imagery",
            0, 19, 256, ".jpg",
            arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                return baseUrl + org.osmdroid.util.MapTileIndex.getZoom(pMapTileIndex) + "/" +
                        org.osmdroid.util.MapTileIndex.getY(pMapTileIndex) + "/" +
                        org.osmdroid.util.MapTileIndex.getX(pMapTileIndex)
            }
        }
    }

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
                    setBuiltInZoomControls(false) // Desabilitar botões padrão do OSMDroid
                    
                    // Localização inicial (São Paulo)
                    val initialLocation = GeoPoint(-23.5505, -46.6333)
                    controller.setCenter(initialLocation)
                    controller.setZoom(15.0)
                    
                    mapView = this
                    mapViewRef = this
                }
            },
            update = { view ->
                // Configurar tiles apenas se mudar
                val currentTileSource = view.tileProvider.tileSource
                val targetTileSource = if (uiState.isSatelliteMode) satelliteSource else standardSource
                
                if (currentTileSource.name() != targetTileSource.name()) {
                    view.setTileSource(targetTileSource)
                }
                
                // Aplicar filtro baseado em hora do dia (Auto Day/Night)
                if (!uiState.isSatelliteMode) {
                    val calendar = java.util.Calendar.getInstance()
                    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                    val isDaytime = hour in 6..17 // 6am-6pm = dia
                    
                    if (isDaytime) {
                        // Modo dia: sem filtro (mapa padrão claro)
                        view.overlayManager.tilesOverlay.setColorFilter(null)
                    } else {
                        // Modo noite: filtro escuro clean
                        val matrix = android.graphics.ColorMatrix()
                        // 1. Inverter cores para base escura
                        val invert = floatArrayOf(
                            -1f,  0f,  0f, 0f, 255f,
                             0f, -1f,  0f, 0f, 255f,
                             0f,  0f, -1f, 0f, 255f,
                             0f,  0f,  0f, 1f,   0f
                        )
                        matrix.set(invert)
                        
                        // 2. Desaturar para evitar cores gritantes
                        val sat = android.graphics.ColorMatrix()
                        sat.setSaturation(0.3f) 
                        matrix.postConcat(sat)
                        
                        // 3. Ajuste de brilho/contraste
                        val contrast = android.graphics.ColorMatrix()
                        val scale = 0.9f
                        val contrastArray = floatArrayOf(
                            scale, 0f, 0f, 0f, -10f,
                            0f, scale, 0f, 0f, -10f,
                            0f, 0f, scale + 0.1f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                        contrast.set(contrastArray)
                        matrix.postConcat(contrast)

                        view.overlayManager.tilesOverlay.setColorFilter(android.graphics.ColorMatrixColorFilter(matrix))
                    }
                } else {
                    view.overlayManager.tilesOverlay.setColorFilter(null)
                }
                
                // Limpar overlays (exceto rotação que é fixa)
                view.overlays.removeIf { it !is org.osmdroid.views.overlay.gestures.RotationGestureOverlay }
                
                // Adicionar overlay de rotação se não existir
                if (view.overlays.none { it is org.osmdroid.views.overlay.gestures.RotationGestureOverlay }) {
                    val rotationGestureOverlay = org.osmdroid.views.overlay.gestures.RotationGestureOverlay(view)
                    rotationGestureOverlay.isEnabled = true
                    view.overlays.add(rotationGestureOverlay)
                }

                // Overlay de "Pulso" para localização do usuário (Efeito Waze/Radar)
                if (uiState.userLocation != null) {
                    val pulseOverlay = object : org.osmdroid.views.overlay.Overlay() {
                        var radius = 0f
                        var alpha = 255
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.CYAN
                            style = android.graphics.Paint.Style.FILL
                            isAntiAlias = true
                        }
                        
                        override fun draw(c: android.graphics.Canvas?, osmv: MapView?, shadow: Boolean) {
                            if (shadow) return
                            val loc = uiState.userLocation ?: return
                            val gp = GeoPoint(loc.first, loc.second)
                            val pt = android.graphics.Point()
                            osmv?.projection?.toPixels(gp, pt) ?: return
                            
                            // Animação simples baseada no tempo
                            val time = System.currentTimeMillis() % 2000
                            val progress = time / 2000f
                            radius = 100f * progress
                            alpha = (255 * (1 - progress)).toInt()
                            
                            paint.alpha = alpha
                            c?.drawCircle(pt.x.toFloat(), pt.y.toFloat(), radius, paint)
                            
                            // Desenhar ponto central fixo
                            paint.alpha = 255
                            c?.drawCircle(pt.x.toFloat(), pt.y.toFloat(), 15f, paint)
                            
                            osmv?.postInvalidateDelayed(16) // 60 FPS
                        }
                    }
                    view.overlays.add(pulseOverlay)
                }
                
                // Adicionar marcadores de crimes com visual moderno
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

        // Botões flutuantes no topo direito (Estilo Waze)
        Column(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botão de satélite
            SmallFloatingActionButton(
                onClick = { viewModel.toggleSatelliteMode() },
                containerColor = if (uiState.isSatelliteMode) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                },
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(
                    if (uiState.isSatelliteMode) Icons.Default.Layers else Icons.Default.Map,
                    "Satélite",
                    tint = if (uiState.isSatelliteMode) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }

            // Botão de filtros
            SmallFloatingActionButton(
                onClick = { /* Filtros */ },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(
                    Icons.Default.FilterList, 
                    "Filtros",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Botão de centralizar localização
            if (uiState.userLocation != null) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            val location = LocationHelper.getCurrentLocation(context)
                            location?.let { (lat, lon) ->
                                viewModel.setUserLocation(lat, lon)
                                // Forçar centralização direta no mapa
                                mapViewRef?.controller?.animateTo(GeoPoint(lat, lon))
                                mapViewRef?.controller?.setZoom(17.0)
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(
                        Icons.Default.MyLocation, 
                        "Minha localização",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Zoom In (+)
            SmallFloatingActionButton(
                onClick = {
                    mapViewRef?.controller?.zoomIn()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ampliar"
                )
            }
            
            // Zoom Out (-)
            SmallFloatingActionButton(
                onClick = {
                    mapViewRef?.controller?.zoomOut()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Reduzir"
                )
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
