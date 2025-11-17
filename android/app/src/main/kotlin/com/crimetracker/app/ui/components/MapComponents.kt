package com.crimetracker.app.ui.components

import androidx.compose.ui.graphics.Color
import com.crimetracker.app.data.model.Report

/**
 * Retorna a cor para cada tipo de crime
 */
fun getCrimeTypeColor(tipo: String): Color {
    return when (tipo.lowercase()) {
        "roubo/assalto com violência ou ameaça",
        "roubo",
        "assalto" -> Color(0xFFDC2626) // Vermelho - crimes violentos
        
        "furto (sem violência)",
        "furto" -> Color(0xFFF59E0B) // Laranja - furtos
        
        "furto/roubo de veículo",
        "roubo de veículo" -> Color(0xFFEAB308) // Amarelo - suspeitas
        
        "outros crimes patrimoniais",
        "outro" -> Color(0xFF3B82F6) // Azul - avisos e informações
        
        else -> Color(0xFF6B7280) // Cinza - padrão
    }
}

/**
 * Retorna a cor para o heatmap baseado na intensidade
 */
fun getCrimeHeatColor(intensity: Int): Color {
    return when {
        intensity >= 10 -> Color(0xFFDC2626) // Vermelho intenso
        intensity >= 5 -> Color(0xFFF59E0B) // Laranja
        intensity >= 3 -> Color(0xFFEAB308) // Amarelo
        else -> Color(0xFF3B82F6) // Azul leve
    }
}

