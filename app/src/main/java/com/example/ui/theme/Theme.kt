package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeMode

private val ClassicLightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue900,
    secondary = Slate800,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    tertiary = Emerald600,
    onTertiary = Color.White,
    tertiaryContainer = Emerald50,
    onTertiaryContainer = Emerald800,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Slate400,
    error = Red600,
    onError = Color.White,
    errorContainer = Red50,
    onErrorContainer = Red800
)

private val MonochromeLightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = MonoZinc100,
    onPrimaryContainer = MonoBlack,
    secondary = MonoZinc800,
    onSecondary = MonoWhite,
    secondaryContainer = MonoZinc100,
    onSecondaryContainer = MonoBlack,
    tertiary = MonoZinc900,
    onTertiary = MonoWhite,
    tertiaryContainer = MonoZinc100,
    onTertiaryContainer = MonoBlack,
    background = MonoWhite,
    onBackground = MonoBlack,
    surface = MonoWhite,
    onSurface = MonoBlack,
    surfaceVariant = MonoZinc100,
    onSurfaceVariant = MonoZinc700,
    outline = MonoZinc300,
    outlineVariant = MonoZinc400,
    error = MonoBlack,
    onError = MonoWhite,
    errorContainer = MonoZinc200,
    onErrorContainer = MonoBlack
)

private val MonochromeDarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoZinc900,
    onPrimaryContainer = MonoWhite,
    secondary = MonoZinc200,
    onSecondary = MonoBlack,
    secondaryContainer = MonoZinc800,
    onSecondaryContainer = MonoWhite,
    tertiary = MonoZinc300,
    onTertiary = MonoBlack,
    tertiaryContainer = MonoZinc900,
    onTertiaryContainer = MonoWhite,
    background = MonoBlack,
    onBackground = MonoWhite,
    surface = MonoZinc950,
    onSurface = MonoWhite,
    surfaceVariant = MonoZinc900,
    onSurfaceVariant = MonoZinc400,
    outline = MonoZinc700,
    outlineVariant = MonoZinc600,
    error = MonoWhite,
    onError = MonoBlack,
    errorContainer = MonoZinc800,
    onErrorContainer = MonoWhite
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.CLASSIC,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.CLASSIC -> ClassicLightColorScheme
        AppThemeMode.MONOCHROME_LIGHT -> MonochromeLightColorScheme
        AppThemeMode.MONOCHROME_DARK -> MonochromeDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
