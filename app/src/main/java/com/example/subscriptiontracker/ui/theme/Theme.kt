package com.example.subscriptiontracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.subscriptiontracker.utils.AppTheme
import com.example.subscriptiontracker.utils.ThemeManager

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = BlackBackground,
    surface = DarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = WhiteBackground,
    surface = LightSurface
)

@Composable
fun SubscriptionTrackerTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeFlow = remember(context) { ThemeManager.getThemeFlow(context) }
    val appTheme by themeFlow.collectAsState(initial = AppTheme.SYSTEM)
    
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    
    // Kullanıcı seçimine göre kesin tema kullan (dynamic color yok)
    val colorScheme = remember(darkTheme) {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}