package com.archi.dreaming.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Rose500,
    onPrimary = Color.White,
    primaryContainer = Peach200,
    onPrimaryContainer = Cocoa900,
    secondary = Peach300,
    secondaryContainer = Peach200,
    onSecondaryContainer = Cocoa900,
    background = Peach50,
    onBackground = Cocoa900,
    surface = Peach50,
    onSurface = Cocoa900,
    surfaceVariant = Peach100,
    onSurfaceVariant = Cocoa600,
    surfaceContainer = Peach100,
)

private val DarkColors = darkColorScheme(
    primary = RoseLight,
    onPrimary = RoseDeep,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Peach100,
    secondary = PeachMuted,
    secondaryContainer = DarkPrimaryContainer,
    onSecondaryContainer = Peach100,
    background = DarkBackground,
    onBackground = Peach100,
    surface = DarkBackground,
    onSurface = Peach100,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = CocoaLight,
    surfaceContainer = DarkSurface,
)

@Composable
fun DreamingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
