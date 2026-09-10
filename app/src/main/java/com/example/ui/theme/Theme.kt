package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FzPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = FzPurpleContainer,
    onPrimaryContainer = FzPurpleLight,
    secondary = FzCyanSecondary,
    onSecondary = Color.Black,
    secondaryContainer = FzCyanContainer,
    onSecondaryContainer = FzCyanLight,
    tertiary = FzPurpleLight,
    background = FzBackground,
    onBackground = FzTextPrimary,
    surface = FzSurface,
    onSurface = FzTextPrimary,
    surfaceVariant = FzSurfaceCard,
    onSurfaceVariant = FzTextSecondary,
    outline = FzSurfaceBorder,
    outlineVariant = FzSurfaceBorderHighlight
)

@Composable
fun FzEngineTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FzEngineTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

