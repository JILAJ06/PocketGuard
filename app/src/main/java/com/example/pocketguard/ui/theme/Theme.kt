package com.example.pocketguard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definimos el esquema de colores. Usamos LightColorScheme para forzar el modo claro por ahora.
private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    background = BackgroundLight,
    surface = White,
    onPrimary = White,
    onBackground = TextDark,
    onSurface = TextDark,
    secondary = TextGray,
    surfaceVariant = InputBackground
)

@Composable
fun PocketGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos dynamicColor para que respete TU verde y no use el color del fondo de pantalla del usuario
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Por ahora forzamos el esquema claro para que se vea igual al diseño PDF
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Aquí pintamos la barra de estado (donde va la hora y batería) de color Verde
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de que Type.kt esté bien
        content = content
    )
}