package com.crimetracker.app.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object ColorUtils {
    fun isDarkColor(color: Color): Boolean {
        // Calculate luminance
        // Formula: 0.299*R + 0.587*G + 0.114*B
        val red = color.red
        val green = color.green
        val blue = color.blue
        
        val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
        return luminance < 0.5
    }

    fun parseColor(colorString: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            Color.Gray // Fallback
        }
    }

    fun toHexString(color: Color): String {
        val argb = color.toArgb()
        return String.format("#%06X", (0xFFFFFF and argb))
    }
}
