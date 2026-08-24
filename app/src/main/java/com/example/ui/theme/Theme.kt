package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekColorScheme = lightColorScheme(
    primary = SleekPrimary,
    onPrimary = SleekWhite,
    primaryContainer = SleekPrimaryContainer,
    onPrimaryContainer = SleekOnPrimaryContainer,
    secondary = SleekSecondary,
    onSecondary = SleekWhite,
    secondaryContainer = SleekSecondaryContainer,
    onSecondaryContainer = SleekOnSecondaryContainer,
    tertiary = SleekAccentCyan,
    onTertiary = SleekWhite,
    tertiaryContainer = Color(0xFFCBE6FF),
    onTertiaryContainer = Color(0xFF001E2B),
    background = SleekCanvas,
    onBackground = TextPrimary,
    surface = SleekSurface,
    onSurface = TextPrimary,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SleekCardBorder,
    outlineVariant = SleekCardBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SleekColorScheme,
        typography = Typography,
        content = content
    )
}

