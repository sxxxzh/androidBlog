package com.blog.myandroidblog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.blog.myandroidblog.data.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFCACACA)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666)
)

// Reading-optimized color schemes
private val ReadingDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8A9A5B), // Soft green for reduced eye strain
    secondary = Color(0xFF6B8E23),
    tertiary = Color(0xFF556B2F),
    background = Color(0xFF1C1C1C), // Warmer dark background
    surface = Color(0xFF252525),
    onPrimary = Color(0xFF1C1C1C),
    onSecondary = Color(0xFF1C1C1C),
    onTertiary = Color.White,
    onBackground = Color(0xFFE8E8E8), // Warmer text color
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFB8B8B8)
)

private val ReadingLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32), // Softer green
    secondary = Color(0xFF388E3C),
    tertiary = Color(0xFF1B5E20),
    background = Color(0xFFF8F8F6), // Warmer light background
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2C2C2C), // Softer text color
    onSurface = Color(0xFF2C2C2C),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF555555)
)

@Composable
fun PersonalBlogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    readingMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    
    val isDarkMode by settingsManager.isDarkMode.collectAsState(initial = darkTheme)
    
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        readingMode -> {
            if (isDarkMode) ReadingDarkColorScheme else ReadingLightColorScheme
        }
        else -> {
            if (isDarkMode) DarkColorScheme else LightColorScheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun AppTheme(
    settingsManager: SettingsManager,
    content: @Composable () -> Unit
) {
    val isDarkMode by settingsManager.isDarkMode.collectAsState(initial = false)
    
    PersonalBlogTheme(
        darkTheme = isDarkMode,
        content = content
    )
}

// Typography for reading optimization
@Composable
fun ReadingTypography(): Typography {
    return Typography(
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.6
        ),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.6
        ),
        bodySmall = MaterialTheme.typography.bodySmall.copy(
            lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.6
        )
    )
}

// Font size utilities
@Composable
fun getScaledTypography(baseFontSize: Float): Typography {
    val scaleFactor = baseFontSize / 16f // 16sp is the default size
    
    return Typography(
        displayLarge = MaterialTheme.typography.displayLarge.copy(
            fontSize = MaterialTheme.typography.displayLarge.fontSize * scaleFactor
        ),
        displayMedium = MaterialTheme.typography.displayMedium.copy(
            fontSize = MaterialTheme.typography.displayMedium.fontSize * scaleFactor
        ),
        displaySmall = MaterialTheme.typography.displaySmall.copy(
            fontSize = MaterialTheme.typography.displaySmall.fontSize * scaleFactor
        ),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(
            fontSize = MaterialTheme.typography.headlineLarge.fontSize * scaleFactor
        ),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(
            fontSize = MaterialTheme.typography.headlineMedium.fontSize * scaleFactor
        ),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(
            fontSize = MaterialTheme.typography.headlineSmall.fontSize * scaleFactor
        ),
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontSize = MaterialTheme.typography.titleLarge.fontSize * scaleFactor
        ),
        titleMedium = MaterialTheme.typography.titleMedium.copy(
            fontSize = MaterialTheme.typography.titleMedium.fontSize * scaleFactor
        ),
        titleSmall = MaterialTheme.typography.titleSmall.copy(
            fontSize = MaterialTheme.typography.titleSmall.fontSize * scaleFactor
        ),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize * scaleFactor,
            lineHeight = MaterialTheme.typography.bodyLarge.fontSize * scaleFactor * 1.5
        ),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(
            fontSize = MaterialTheme.typography.bodyMedium.fontSize * scaleFactor,
            lineHeight = MaterialTheme.typography.bodyMedium.fontSize * scaleFactor * 1.5
        ),
        bodySmall = MaterialTheme.typography.bodySmall.copy(
            fontSize = MaterialTheme.typography.bodySmall.fontSize * scaleFactor,
            lineHeight = MaterialTheme.typography.bodySmall.fontSize * scaleFactor * 1.5
        ),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            fontSize = MaterialTheme.typography.labelLarge.fontSize * scaleFactor
        ),
        labelMedium = MaterialTheme.typography.labelMedium.copy(
            fontSize = MaterialTheme.typography.labelMedium.fontSize * scaleFactor
        ),
        labelSmall = MaterialTheme.typography.labelSmall.copy(
            fontSize = MaterialTheme.typography.labelSmall.fontSize * scaleFactor
        )
    )
}