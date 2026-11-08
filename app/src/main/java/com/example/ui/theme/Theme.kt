package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.auramusic.data.model.AuraThemeStyle

private val DarkColorScheme = darkColorScheme(
    primary = AuraLavender,
    secondary = AuraSoftPurple,
    tertiary = AuraBubblegumPink,
    background = AuraDarkBackground,
    surface = AuraDarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AuraSoftPurple,
    secondary = AuraElectricBlue,
    tertiary = AuraBubblegumPink,
    background = Color(0xFFF3F1FD),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF151528),
    onSurface = Color(0xFF151528)
)

@Composable
fun AuraMusicTheme(
    darkTheme: Boolean = true,
    themeStyle: AuraThemeStyle = AuraThemeStyle.AURORA_VIOLET,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
