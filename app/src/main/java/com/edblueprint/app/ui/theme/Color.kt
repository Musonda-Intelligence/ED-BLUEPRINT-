package com.edblueprint.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Palette taken from the ED Blueprint logo: black base, deep maroon shadows,
// bright red and orange highlights.
val EdBlack = Color(0xFF0B0B0B)
val EdSurface = Color(0xFF151212)
val EdSurfaceHigh = Color(0xFF1F1A1A)
val EdOutline = Color(0xFF3A2A2A)

val EdRed = Color(0xFFE02020)
val EdRedDeep = Color(0xFF8E1B1B)
val EdMaroon = Color(0xFF3A0F12)
val EdOrange = Color(0xFFF05A20)

val EdTextPrimary = Color(0xFFF4ECEA)
val EdTextSecondary = Color(0xFFB9A9A6)

val EdError = Color(0xFFFF6B5E)
val EdSuccess = Color(0xFF4CAF7A)

/** The red-to-orange sweep used on the logo, used for primary buttons and highlights. */
val EdBrandBrush: Brush = Brush.horizontalGradient(listOf(EdRed, EdOrange))
