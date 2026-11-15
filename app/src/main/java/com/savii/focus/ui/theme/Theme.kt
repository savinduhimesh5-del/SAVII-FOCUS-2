package com.savii.focus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SaviiDarkColors = darkColorScheme(
    primary = Color(0xFFA58BFF),
    onPrimary = Color(0xFF160E31),
    secondary = Color(0xFF54D6D0),
    onSecondary = Color(0xFF062B2A),
    background = Color(0xFF0D0B14),
    onBackground = Color(0xFFF7F3FF),
    surface = Color(0xFF181525),
    onSurface = Color(0xFFF7F3FF),
    surfaceVariant = Color(0xFF242039),
    onSurfaceVariant = Color(0xFFB9B1CB),
    outline = Color(0xFF514A66),
    error = Color(0xFFFF8B9B)
)

@Composable
fun SaviiFocusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SaviiDarkColors,
        content = content
    )
}
