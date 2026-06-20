package com.sh4wty.downloader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Mamão brand palette, seeded from the Claude-style orange #D97757. We deliberately do NOT use
// Material You dynamic color so the app always wears the brand orange instead of the wallpaper.
private val LightColors = lightColorScheme(
    primary = Color(0xFFD97757),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCD),
    onPrimaryContainer = Color(0xFF3B0A00),
    secondary = Color(0xFFB85C3E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCD),
    onSecondaryContainer = Color(0xFF341100),
    background = Color(0xFFFFFBF8),
    onBackground = Color(0xFF231A15),
    surface = Color(0xFFFFFBF8),
    onSurface = Color(0xFF231A15),
    surfaceVariant = Color(0xFFF5DED5),
    onSurfaceVariant = Color(0xFF53433D),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB59B),
    onPrimary = Color(0xFF5A1B07),
    primaryContainer = Color(0xFFB85C3E),
    onPrimaryContainer = Color(0xFFFFDBCD),
    secondary = Color(0xFFE7BDAC),
    onSecondary = Color(0xFF442A1E),
    secondaryContainer = Color(0xFF5D4033),
    onSecondaryContainer = Color(0xFFFFDBCD),
    background = Color(0xFF1A120E),
    onBackground = Color(0xFFF0DFD8),
    surface = Color(0xFF1A120E),
    onSurface = Color(0xFFF0DFD8),
    surfaceVariant = Color(0xFF53433D),
    onSurfaceVariant = Color(0xFFD8C2B8),
    error = Color(0xFFFFB4AB),
)

/** Mamão theme — always the brand orange, light or dark following the system. */
@Composable
fun DownloaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
