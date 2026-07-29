package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Emerald Green (Current Default)
private val EmeraldLightScheme = lightColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2F1),
    onPrimaryContainer = Color(0xFF004D40),
    secondary = Color(0xFF00ACC1),
    onSecondary = Color.White,
    tertiary = GoldAccent,
    background = Color(0xFFF7F9FB),
    surface = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val EmeraldDarkScheme = darkColorScheme(
    primary = Color(0xFF4EB2A6),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF005B4F),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF00ACC1),
    onSecondary = Color.Black,
    tertiary = GoldAccent,
    background = Color(0xFF121417),
    surface = Color(0xFF1E2024),
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6)
)

// 2. Deep Blue
private val DeepBlueLightScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF0288D1),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF8F00),
    background = Color(0xFFF0F4F8),
    surface = Color.White,
    onBackground = Color(0xFF101B2B),
    onSurface = Color(0xFF101B2B)
)

private val DeepBlueDarkScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFB300),
    background = Color(0xFF0D1B2A),
    surface = Color(0xFF1B263B),
    onBackground = Color(0xFFE0E8F5),
    onSurface = Color(0xFFE0E8F5)
)

// 3. Midnight Black (Dark)
private val MidnightBlackScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFB8F3FF),
    secondary = Color(0xFF80D8FF),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFD54F),
    background = Color(0xFF05070A),
    surface = Color(0xFF11161F),
    surfaceVariant = Color(0xFF1E2633),
    onBackground = Color(0xFFE0E6ED),
    onSurface = Color(0xFFE0E6ED)
)

// 4. Royal Purple
private val RoyalPurpleLightScheme = lightColorScheme(
    primary = Color(0xFF7B1FA2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E5F5),
    onPrimaryContainer = Color(0xFF4A148C),
    secondary = Color(0xFFAB47BC),
    onSecondary = Color.White,
    tertiary = GoldAccent,
    background = Color(0xFFF6F2FA),
    surface = Color.White,
    onBackground = Color(0xFF21152B),
    onSurface = Color(0xFF21152B)
)

private val RoyalPurpleDarkScheme = darkColorScheme(
    primary = Color(0xFFCE93D8),
    onPrimary = Color(0xFF4A148C),
    primaryContainer = Color(0xFF7B1FA2),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFE1BEE7),
    onSecondary = Color.Black,
    tertiary = GoldAccent,
    background = Color(0xFF1A0E26),
    surface = Color(0xFF271738),
    onBackground = Color(0xFFEDE0F5),
    onSurface = Color(0xFFEDE0F5)
)

// 5. Classic Teal
private val ClassicTealLightScheme = lightColorScheme(
    primary = Color(0xFF00838F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF004D40),
    secondary = Color(0xFF26A69A),
    onSecondary = Color.White,
    tertiary = GoldAccent,
    background = Color(0xFFF0F7F7),
    surface = Color.White,
    onBackground = Color(0xFF122224),
    onSurface = Color(0xFF122224)
)

private val ClassicTealDarkScheme = darkColorScheme(
    primary = Color(0xFF80DEEA),
    onPrimary = Color(0xFF004D40),
    primaryContainer = Color(0xFF00838F),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color.Black,
    tertiary = GoldAccent,
    background = Color(0xFF0B1B1D),
    surface = Color(0xFF162A2D),
    onBackground = Color(0xFFE0F2F1),
    onSurface = Color(0xFFE0F2F1)
)

@Composable
fun SmartHishabTheme(
    themeMode: String = "SYSTEM",
    themePreset: String = "EMERALD_GREEN",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> darkTheme
    }

    val colorScheme: ColorScheme = when (themePreset) {
        "DEEP_BLUE" -> if (isDark) DeepBlueDarkScheme else DeepBlueLightScheme
        "MIDNIGHT_BLACK" -> MidnightBlackScheme
        "ROYAL_PURPLE" -> if (isDark) RoyalPurpleDarkScheme else RoyalPurpleLightScheme
        "CLASSIC_TEAL" -> if (isDark) ClassicTealDarkScheme else ClassicTealLightScheme
        else -> if (isDark) EmeraldDarkScheme else EmeraldLightScheme // EMERALD_GREEN
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
