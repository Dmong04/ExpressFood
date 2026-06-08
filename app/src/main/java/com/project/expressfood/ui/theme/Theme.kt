package com.project.expressfood.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ExpressFoodColorScheme = lightColorScheme(
    primary             = ExpressRed,
    onPrimary           = Color.White,
    primaryContainer    = ExpressRedLight,
    onPrimaryContainer  = ExpressRedDark,
    secondary           = ExpressOrange,
    onSecondary         = Color.White,
    secondaryContainer  = ExpressAmber,
    onSecondaryContainer = ExpressOrangeDark,
    background          = ExpressBackground,
    onBackground        = ExpressOnSurface,
    surface             = ExpressSurface,
    onSurface           = ExpressOnSurface,
    surfaceVariant      = ExpressSurfaceVariant,
    onSurfaceVariant    = ExpressOnSurfaceVariant,
    outline             = ExpressOutline,
    error               = Color(0xFFB00020),
    onError             = Color.White,
)

@Composable
fun ExpressFoodTheme(content: @Composable () -> Unit) {
    val colorScheme = ExpressFoodColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}
