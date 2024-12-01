package com.example.screentimetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat

// Modern, futuristic color scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = Color(0xFF303030),
    tertiary = Color.White,
    background = Color.Black,
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1C1C1C),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f)
)

// We'll force dark theme for consistency
@Composable
fun ScreenTimeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    val context = LocalContext.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            
            // Make status bar transparent and handle system bars
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Set status bar and navigation bar colors to match app background
            window.statusBarColor = Color.Black.toArgb()
            window.navigationBarColor = Color.Black.toArgb()
            
            // Make system bars icons light (white)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
                // Hide navigation bar
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getCyberpunkTypography(),
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                content()
            }
        }
    )
} 