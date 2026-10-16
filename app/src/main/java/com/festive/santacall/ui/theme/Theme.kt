package com.festive.santacall.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ChristmasRed,
    secondary = ChristmasGreen,
    tertiary = ChristmasGold,
    background = ChristmasDarkBg,
    surface = ChristmasSurface,
    onPrimary = ChristmasCream,
    onSecondary = ChristmasCream,
    onBackground = ChristmasTextPrimary,
    onSurface = ChristmasTextPrimary
)

@Composable
fun SantaCallTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = ChristmasDarkRed.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
