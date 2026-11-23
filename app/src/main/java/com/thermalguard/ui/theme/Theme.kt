package com.thermalguard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.thermalguard.domain.model.UserSettings

private val DarkColorScheme = darkColorScheme(
    primary = ThermalPrimary,
    secondary = ThermalSecondary,
    background = ThermalBackgroundDark,
    surface = ThermalSurfaceDark,
    surfaceVariant = ThermalCardDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

private val LightColorScheme = lightColorScheme(
    primary = ThermalPrimary,
    secondary = ThermalSecondary,
    background = ThermalSurfaceLight,
    surface = Color.White,
    surfaceVariant = Color(0xFFEBF1F6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B)
)

@Composable
fun ThermalGuardTheme(
    themePreference: String = UserSettings.THEME_SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        UserSettings.THEME_DARK -> true
        UserSettings.THEME_LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

