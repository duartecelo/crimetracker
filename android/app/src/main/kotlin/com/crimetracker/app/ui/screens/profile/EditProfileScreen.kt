package com.crimetracker.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var nickname by remember { mutableStateOf(uiState.nickname) }
    var description by remember { mutableStateOf(uiState.description) }
    
    // Initialize state when loaded
    LaunchedEffect(uiState) {
        if (nickname.isEmpty() && uiState.nickname.isNotEmpty()) nickname = uiState.nickname
        if (description.isEmpty() && uiState.description.isNotEmpty()) description = uiState.description
    }

    // Cores padrão solicitadas: Vermelho, Verde, Azul, Amarelo, Roxo, Ciano, Laranja
    val availableColors = listOf(
        "#EF4444", // Vermelho
        "#10B981", // Verde
        "#3B82F6", // Azul
        "#F59E0B", // Amarelo (Amber/Yellow)
        "#8B5CF6", // Roxo
        "#06B6D4", // Ciano
        "#F97316"  // Laranja
    )
    
    var showColorPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.updateNickname(nickname)
                        viewModel.updateDescription(description)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Check, "Salvar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Nome/Apelido
            OutlinedTextField(
                value = nickname,
                onValueChange = { if (it.length <= 30) nickname = it },
                label = { Text("Apelido") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("${nickname.length}/30") },
                singleLine = true
            )

            // Descrição
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 150) description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("${description.length}/150") },
                minLines = 3,
                maxLines = 5
            )

            // Cor de destaque
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Cor de destaque",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    availableColors.forEach { color ->
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { viewModel.updateUserColor(color) },
                            shape = CircleShape,
                            color = Color(android.graphics.Color.parseColor(color)),
                            border = if (uiState.userColor.equals(color, ignoreCase = true)) {
                                androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                            } else null
                        ) {
                            if (uiState.userColor.equals(color, ignoreCase = true)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Check, 
                                        contentDescription = null,
                                        tint = if (com.crimetracker.app.util.ColorUtils.isDarkColor(Color(android.graphics.Color.parseColor(color)))) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                    
                    // Botão de cor personalizada (+)
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { showColorPicker = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = if (!availableColors.any { it.equals(uiState.userColor, ignoreCase = true) }) {
                            androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                        } else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!availableColors.any { it.equals(uiState.userColor, ignoreCase = true) }) {
                                // Se a cor atual não está na lista, mostra ela aqui
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = try { Color(android.graphics.Color.parseColor(uiState.userColor)) } catch(e: Exception) { Color.Gray }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Edit, null, tint = if (com.crimetracker.app.util.ColorUtils.isDarkColor(try { Color(android.graphics.Color.parseColor(uiState.userColor)) } catch(e: Exception) { Color.Gray })) Color.White else Color.Black)
                                    }
                                }
                            } else {
                                Icon(Icons.Default.Add, "Cor Personalizada")
                            }
                        }
                    }
                }
            }
        }
        
        if (showColorPicker) {
            CustomColorDialog(
                initialColor = uiState.userColor,
                onDismiss = { showColorPicker = false },
                onColorSelected = { color ->
                    viewModel.updateUserColor(color)
                    showColorPicker = false
                }
            )
        }
    }
}

@Composable
fun CustomColorDialog(
    initialColor: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var red by remember { mutableStateOf(0f) }
    var green by remember { mutableStateOf(0f) }
    var blue by remember { mutableStateOf(0f) }
    
    LaunchedEffect(initialColor) {
        try {
            val color = android.graphics.Color.parseColor(initialColor)
            red = android.graphics.Color.red(color) / 255f
            green = android.graphics.Color.green(color) / 255f
            blue = android.graphics.Color.blue(color) / 255f
        } catch (e: Exception) {
            // Default to black if parse fails
        }
    }

    val currentColor = Color(red, green, blue)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cor Personalizada") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Preview
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = currentColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {}
                
                Text(com.crimetracker.app.util.ColorUtils.toHexString(currentColor))

                // Sliders
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Vermelho", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = red,
                        onValueChange = { red = it },
                        colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                    )
                    
                    Text("Verde", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = green,
                        onValueChange = { green = it },
                        colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                    )
                    
                    Text("Azul", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = blue,
                        onValueChange = { blue = it },
                        colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onColorSelected(com.crimetracker.app.util.ColorUtils.toHexString(currentColor))
            }) {
                Text("Selecionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
