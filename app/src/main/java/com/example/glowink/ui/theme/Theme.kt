package com.example.glowink.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================================
// GLOWINK THEME CONFIGURATION
// ============================================================================

/**
 * Custom extra colors for Glowink design system (Neons, Glass, Gradients).
 */
@Immutable
data class GlowinkCustomColors(
    val ultravioletPurple: Color = UltravioletPurple,
    val neonLime: Color = NeonLime,
    val electricCyan: Color = ElectricCyan,
    val glassBackgroundDark: Color = GlassBackgroundDark,
    val glassBorderDefault: Color = GlassBorderNeonDefault,
    val neonGradientPrimary: Brush = NeonGradientPrimary,
    val neonGradientLimeCyan: Brush = NeonGradientLimeCyan,
    val neonGradientPurpleCyan: Brush = NeonGradientPurpleCyan,
    val glassBubbleSelf: Brush = GlassBubbleSelfGradient,
    val glassBubbleOther: Brush = GlassBubbleOtherGradient
)

val LocalGlowinkCustomColors = staticCompositionLocalOf { GlowinkCustomColors() }

/**
 * Dark Color Scheme styled for Glowink.
 */
private val GlowinkDarkColorScheme = darkColorScheme(
    primary = UltravioletPurple,
    onPrimary = OnPrimaryText,
    primaryContainer = UltravioletPurpleDark,
    onPrimaryContainer = UltravioletPurpleGlow,

    secondary = NeonLime,
    onSecondary = OnSecondaryText,
    secondaryContainer = NeonLimeDark,
    onSecondaryContainer = NeonLimeGlow,

    tertiary = ElectricCyan,
    onTertiary = OnTertiaryText,
    tertiaryContainer = ElectricCyanDark,
    onTertiaryContainer = ElectricCyanGlow,

    background = ObsidianBackground,
    onBackground = OnBackgroundText,

    surface = DeepVioletSurface,
    onSurface = OnSurfaceText,
    surfaceVariant = DeepVioletSurfaceVariant,
    onSurfaceVariant = OnSurfaceMuted,

    outline = ElectricCyan.copy(alpha = 0.5f)
)

@Composable
fun GlowinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Glowink is styled with a rich deep dark mode by default
    val colorScheme = GlowinkDarkColorScheme
    val customColors = GlowinkCustomColors()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ObsidianBackground.toArgb()
            window.navigationBarColor = ObsidianBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(
        LocalGlowinkCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Easy accessor for Glowink custom color tokens & gradients.
 * Example: `GlowinkTheme.colors.neonLime` or `GlowinkTheme.colors.electricCyan`
 */
object GlowinkTheme {
    val colors: GlowinkCustomColors
        @Composable
        get() = LocalGlowinkCustomColors.current
}
