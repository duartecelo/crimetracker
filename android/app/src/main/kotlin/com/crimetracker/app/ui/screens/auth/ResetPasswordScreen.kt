package com.crimetracker.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    onNavigateBack: () -> Unit,
    onPasswordReset: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Validação de senha
    val passwordRequirements = remember(newPassword) {
        mapOf(
            "Mínimo 8 caracteres" to (newPassword.length >= 8),
            "Pelo menos uma letra maiúscula" to newPassword.any { it.isUpperCase() },
            "Pelo menos uma letra minúscula" to newPassword.any { it.isLowerCase() },
            "Pelo menos um número" to newPassword.any { it.isDigit() },
            "Pelo menos um caractere especial" to newPassword.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }
        )
    }
    
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

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
            onPasswordReset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Redefinir Senha") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🔑",
                style = MaterialTheme.typography.displayMedium
            )
            
            Text(
                text = "Redefinir Senha",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Código enviado para: $email",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                label = { Text("Código de verificação") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                placeholder = { Text("000000") }
            )
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nova senha") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle password visibility"
                        )
                    }
                },
                enabled = !uiState.isLoading
            )
            
            // Requisitos de senha
            if (newPassword.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Requisitos:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        passwordRequirements.forEach { (requirement, met) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (met) "✓" else "○",
                                    color = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = requirement,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar senha") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle password visibility"
                        )
                    }
                },
                enabled = !uiState.isLoading,
                supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    { Text("As senhas não coincidem", color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val isFormValid = code.length == 6 &&
                    passwordRequirements.values.all { it } &&
                    passwordsMatch
            
            Button(
                onClick = {
                    if (isFormValid) {
                        viewModel.resetPassword(email, code, newPassword)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Preencha todos os campos corretamente")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && isFormValid
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Redefinir Senha")
                }
            }
        }
    }
}

