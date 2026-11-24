package com.edblueprint.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The brand is black with red and orange, so the app uses one dark scheme.
private val EdColorScheme = darkColorScheme(
    primary = EdRed,
    onPrimary = Color.White,
    primaryContainer = EdMaroon,
    onPrimaryContainer = EdTextPrimary,
    secondary = EdOrange,
    onSecondary = Color.White,
    background = EdBlack,
    onBackground = EdTextPrimary,
    surface = EdSurface,
    onSurface = EdTextPrimary,
    surfaceVariant = EdSurfaceHigh,
    onSurfaceVariant = EdTextSecondary,
    outline = EdOutline,
    error = EdError,
    onError = Color.Black
)

@Composable
fun EdBlueprintTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EdColorScheme,
        typography = EdTypography,
        content = content
    )
}
