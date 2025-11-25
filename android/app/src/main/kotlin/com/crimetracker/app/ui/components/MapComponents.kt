package com.crimetracker.app.ui.components

import androidx.compose.ui.graphics.Color

/**
 * Retorna a cor padrão para cada tipo de crime (Backend Types)
 */
fun getCrimeTypeColor(tipo: String): Color {
    return when (tipo.lowercase().trim()) {
        "assalto" -> Color(0xFFD32F2F)    // Vermelho Sangue
        "roubo" -> Color(0xFFC62828)      // Vermelho Escuro
        "furto" -> Color(0xFFF57C00)      // Laranja
        "agressão", "agressao" -> Color(0xFFC2185B) // Rosa/Roxo
        "vandalismo" -> Color(0xFF5D4037) // Marrom
        "homicídio", "homicidio" -> Color(0xFF000000) // Preto
        "tráfico", "trafico" -> Color(0xFF2E7D32) // Verde
        else -> Color(0xFF1976D2)         // Azul (Outros)
    }
}

// Função auxiliar para converter Color do Compose para Int (para o OSMDroid)
fun getCrimeTypeColorInt(tipo: String): Int {
    val color = getCrimeTypeColor(tipo)
    return android.graphics.Color.argb(
        (color.alpha * 255).toInt(),
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )
}
