package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AyraDarkColorScheme = darkColorScheme(
    primary = AyraCyan,
    onPrimary = Color(0xFF001F29),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = AyraViolet,
    onSecondary = Color(0xFF280056),
    secondaryContainer = Color(0xFF4C218B),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = AyraPink,
    onTertiary = Color(0xFF49001E),
    tertiaryContainer = Color(0xFF7B0038),
    onTertiaryContainer = Color(0xFFFFD9E2),
    background = AyraMidnightBg,
    onBackground = AyraTextPrimary,
    surface = AyraSurfaceDark,
    onSurface = AyraTextPrimary,
    surfaceVariant = AyraSurfaceVariant,
    onSurfaceVariant = AyraTextSecondary,
    outline = AyraCardBorder,
    error = AyraError,
    onError = Color(0xFF60000A)
)

@Composable
fun AyraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AyraDarkColorScheme,
        typography = Typography,
        content = content
    )
}
