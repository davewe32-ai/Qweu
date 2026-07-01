package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldDark,
    onPrimaryContainer = Color.White,
    secondary = GoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = NavyLight,
    onSecondaryContainer = TextLight,
    background = DarkBackground,
    onBackground = TextLight,
    surface = NavySurface,
    onSurface = TextLight,
    surfaceVariant = NavyLight,
    onSurfaceVariant = TextLight,
    outline = GoldPrimary.copy(alpha = 0.5f),
    error = StatusSold,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldSecondary,
    onPrimaryContainer = Color.Black,
    secondary = NavyLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5E7EB),
    onSecondaryContainer = TextDark,
    background = LightBackground,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = TextDark,
    outline = Color(0xFFD1D5DB),
    error = StatusSold,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // default to luxury dark theme!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
