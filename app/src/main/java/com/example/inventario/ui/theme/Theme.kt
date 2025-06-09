// 📁 Theme.kt
package com.example.inventario.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val VibrantLightColors = lightColorScheme(
    primary = Color(0xFF4A90E2),        // Azul vivo
    onPrimary = Color.White,
    secondary = Color(0xFF50E3C2),      // Verde menta
    onSecondary = Color.Black,
    tertiary = Color(0xFFB76EF0),       // Morado
    onTertiary = Color.White,
    error = Color(0xFFD32F2F),          // Rojo
    onError = Color.White,
    background = Color(0xFFF5F5F5),     // Fondo suave
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

val VibrantDarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    secondary = Color(0xFF80CBC4),
    onSecondary = Color.Black,
    tertiary = Color(0xFFD1C4E9),
    onTertiary = Color.Black,
    error = Color(0xFFEF9A9A),
    onError = Color.Black,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun InventarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> VibrantDarkColors
        else -> VibrantLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}