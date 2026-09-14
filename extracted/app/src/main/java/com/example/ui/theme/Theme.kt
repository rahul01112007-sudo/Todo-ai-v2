package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurplePrimary,
    onPrimary = DeepBlack,
    primaryContainer = DeepViolet,
    onPrimaryContainer = GlowPurple,
    secondary = NeonPurpleSecondary,
    onSecondary = DeepBlack,
    secondaryContainer = DarkCardElevated,
    onSecondaryContainer = GlowPurple,
    tertiary = ElectricViolet,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = DarkCanvas,
    onSurface = TextPrimary,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceStroke,
    outlineVariant = NeonGlowBorder,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Keep consistent futuristic branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
