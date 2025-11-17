package com.crimetracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta baseada em azul marinho da logo
private val NavyBlue = Color(0xFF1E3A8A) // Azul marinho principal
private val DarkNavy = Color(0xFF1E40AF) // Azul marinho escuro
private val LightNavy = Color(0xFF3B82F6) // Azul claro
private val AccentBlue = Color(0xFF60A5FA) // Azul de destaque

private val DarkColorScheme = darkColorScheme(
    primary = NavyBlue,
    secondary = AccentBlue,
    tertiary = LightNavy,
    background = Color(0xFF0F172A), // Fundo escuro baseado no azul marinho
    surface = Color(0xFF1E293B), // Superfície escura
    surfaceVariant = Color(0xFF334155), // Variante de superfície
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFF1F5F9), // Texto claro em fundo escuro
    onSurface = Color(0xFFF1F5F9), // Texto claro em superfície escura
    onSurfaceVariant = Color(0xFFCBD5E1), // Texto cinza claro
    error = Color(0xFFEF4444), // Vermelho para erros
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    secondary = AccentBlue,
    tertiary = DarkNavy,
    background = Color(0xFFF8FAFC), // Fundo claro
    surface = Color.White, // Superfície branca
    surfaceVariant = Color(0xFFF1F5F9), // Variante de superfície
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1E293B), // Texto escuro em fundo claro
    onSurface = Color(0xFF1E293B), // Texto escuro em superfície
    onSurfaceVariant = Color(0xFF475569), // Texto cinza
    error = Color(0xFFDC2626), // Vermelho para erros
    onError = Color.White
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@Composable
fun CrimeTrackerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

