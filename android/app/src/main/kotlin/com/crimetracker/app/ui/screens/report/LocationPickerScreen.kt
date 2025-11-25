package com.crimetracker.app.ui.screens.report

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.crimetracker.app.data.local.MapTheme
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    initialLat: Double,
    initialLon: Double,
    mapTheme: MapTheme = MapTheme.SYSTEM,
    onLocationSelected: (Double, Double) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isSystemDark = isSystemInDarkTheme()
    
    // Determine if map should be dark based on theme setting
    val isDarkMap = when (mapTheme) {
        MapTheme.LIGHT -> false
        MapTheme.DARK -> true
        MapTheme.SYSTEM -> isSystemDark
        MapTheme.AUTO -> {
            // Get current hour in GMT-3
            val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT-3"))
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            // Dark mode from 18:00 to 6:00
            hour < 6 || hour >= 18
        }
    }
    
    // Debug: Log the current state
    LaunchedEffect(mapTheme, isDarkMap) {
        android.util.Log.d("LocationPicker", "MapTheme: $mapTheme, isDarkMap: $isDarkMap, isSystemDark: $isSystemDark")
    }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, try to move to location
            // We need to call a function here, but moveToCurrentLocation is defined below.
            // We'll handle this by triggering a side effect or just letting the user click the button again.
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permissão de localização necessária.")
            }
        }
    }
    
    var mapView: MapView? by remember { mutableStateOf(null) }
    var centerLat by remember { mutableStateOf(initialLat) }
    var centerLon by remember { mutableStateOf(initialLon) }

    // Configure OSM
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "CrimeTracker/1.0"
    }

    // Lifecycle management
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
    
    // Function to move map to current location
    fun moveToCurrentLocation() {
        scope.launch {
            if (com.crimetracker.app.util.LocationHelper.hasLocationPermission(context)) {
                val location = com.crimetracker.app.util.LocationHelper.getCurrentLocation(context)
                if (location != null) {
                    centerLat = location.first
                    centerLon = location.second
                    mapView?.controller?.animateTo(GeoPoint(centerLat, centerLon))
                } else {
                    snackbarHostState.showSnackbar("Não foi possível obter a localização atual.")
                }
            } else {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Try to get location on start if we are at default or 0,0
    LaunchedEffect(Unit) {
        val isDefault = (initialLat == -23.5505 && initialLon == -46.6333)
        val isZero = (initialLat == 0.0 && initialLon == 0.0)
        
        if (isZero || isDefault) {
            moveToCurrentLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecionar Local") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Cancelar")
                    }
                },
                actions = {
                    TextButton(onClick = { onLocationSelected(centerLat, centerLon) }) {
                        Text("Confirmar", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = { moveToCurrentLocation() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.MyLocation, "Minha Localização")
                }
                
                FloatingActionButton(
                    onClick = { onLocationSelected(centerLat, centerLon) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Check, "Confirmar")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        // Always use standard MAPNIK tiles
                        setTileSource(TileSourceFactory.MAPNIK)
                        minZoomLevel = 5.0
                        maxZoomLevel = 19.0
                        setMultiTouchControls(true)
                        setBuiltInZoomControls(false)
                        
                        controller.setCenter(GeoPoint(centerLat, centerLon))
                        controller.setZoom(17.0)
                        
                        // Add listener to track map movement
                        addMapListener(object : org.osmdroid.events.MapListener {
                            override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                                val center = mapCenter
                                centerLat = center.latitude
                                centerLon = center.longitude
                                return true
                            }

                            override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                                return true
                            }
                        })
                        
                        mapView = this
                    }
                },
                update = { view ->
                    // Apply dark mode filter if needed
                    if (isDarkMap) {
                        val matrix = android.graphics.ColorMatrix()
                        // Invert colors
                        val invert = floatArrayOf(
                            -1f,  0f,  0f, 0f, 255f,
                             0f, -1f,  0f, 0f, 255f,
                             0f,  0f, -1f, 0f, 255f,
                             0f,  0f,  0f, 1f,   0f
                        )
                        matrix.set(invert)
                        
                        // Reduce saturation for better dark mode
                        val sat = android.graphics.ColorMatrix()
                        sat.setSaturation(0.4f)
                        matrix.postConcat(sat)
                        
                        // Adjust brightness
                        val brightness = android.graphics.ColorMatrix()
                        val scale = 0.85f
                        val brightnessArray = floatArrayOf(
                            scale, 0f, 0f, 0f, -15f,
                            0f, scale, 0f, 0f, -15f,
                            0f, 0f, scale + 0.1f, 0f, -5f,
                            0f, 0f, 0f, 1f, 0f
                        )
                        brightness.set(brightnessArray)
                        matrix.postConcat(brightness)
                        
                        view.overlayManager.tilesOverlay.setColorFilter(
                            android.graphics.ColorMatrixColorFilter(matrix)
                        )
                    } else {
                        view.overlayManager.tilesOverlay.setColorFilter(null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Fixed Center Pin
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Centro",
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .offset(y = (-24).dp), // Adjust so the bottom of the pin is at the center
                tint = Color.Red
            )
            
            // Coordinates display
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "${String.format("%.5f", centerLat)}, ${String.format("%.5f", centerLon)}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
