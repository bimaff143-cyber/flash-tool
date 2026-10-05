package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val RomFlashDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF041824),
    primaryContainer = Color(0xFF0D324D),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = Color(0xFF00E676),
    onSecondary = Color(0xFF00220F),
    secondaryContainer = Color(0xFF0F3B20),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFFFFB300),
    onTertiary = Color(0xFF261900),
    tertiaryContainer = Color(0xFF4A3400),
    onTertiaryContainer = Color(0xFFFDE68A),
    error = Color(0xFFFF3D71),
    onError = Color.White,
    errorContainer = Color(0xFF4C0B1B),
    onErrorContainer = Color(0xFFFFD1DC),
    background = Color(0xFF090D16),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF111726),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF192237),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF263352)
)

private val RomFlashLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF16A34A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF15803D),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFFB45309),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0)
)

@Composable
fun RomFlashToolTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) RomFlashDarkColorScheme else RomFlashLightColorScheme
    val appColors = if (isDark) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
