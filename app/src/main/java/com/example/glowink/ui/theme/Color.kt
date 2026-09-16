package com.example.glowink.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// GLOWINK COLOR PALETTE - ULTRA-YOUTHFUL, NEON GAMING & SOCIAL SYSTEM
// ============================================================================

// --- Core Palette Brand Colors ---
/** Primary Accent: Púrpura Ultravioleta (#7B2CBF) */
val UltravioletPurple = Color(0xFF7B2CBF)
val UltravioletPurpleLight = Color(0xFF9D4EDD)
val UltravioletPurpleDark = Color(0xFF5A189A)
val UltravioletPurpleGlow = Color(0xFFC77DFF)

/** Secondary Accent: Verde Lima Neón (#39FF14) */
val NeonLime = Color(0xFF39FF14)
val NeonLimeLight = Color(0xFF70FF52)
val NeonLimeDark = Color(0xFF23B800)
val NeonLimeGlow = Color(0xFF8FFF66)

/** Tertiary Accent: Azul Cian Eléctrico (#00F0FF) */
val ElectricCyan = Color(0xFF00F0FF)
val ElectricCyanLight = Color(0xFF66F5FF)
val ElectricCyanDark = Color(0xFF00B3C2)
val ElectricCyanGlow = Color(0xFFB3FAFF)

// --- Deep Dark Mode Background & Surfaces ---
val ObsidianBackground = Color(0xFF0A0714)
val DeepVioletSurface = Color(0xFF140F24)
val DeepVioletSurfaceVariant = Color(0xFF1E1735)
val SurfaceHighlight = Color(0xFF2A2048)

// --- Glassmorphism Base Colors ---
val GlassBackgroundDark = Color(0x2B1E1735) // 17% opacity dark glass base
val GlassBackgroundLight = Color(0x1FFFFFFF) // Subtle translucent light tint
val GlassBorderNeonDefault = Color(0x8000F0FF) // Semi-transparent cyan glow border
val GlassBorderLime = Color(0x8039FF14) // Semi-transparent lime border
val GlassBorderPurple = Color(0x807B2CBF) // Semi-transparent purple border

// --- Content / On-Colors ---
val OnPrimaryText = Color(0xFFFFFFFF)
val OnSecondaryText = Color(0xFF0A0714) // High contrast dark on lime
val OnTertiaryText = Color(0xFF0A0714) // High contrast dark on cyan
val OnBackgroundText = Color(0xFFF4F0FF)
val OnSurfaceText = Color(0xFFECE6FA)
val OnSurfaceMuted = Color(0xFFAAA3C0)

// --- Pre-defined Neon & Glass Brushes / Gradients ---
val NeonGradientPrimary = Brush.linearGradient(
    colors = listOf(UltravioletPurple, ElectricCyan, NeonLime)
)

val NeonGradientLimeCyan = Brush.linearGradient(
    colors = listOf(NeonLime, ElectricCyan)
)

val NeonGradientPurpleCyan = Brush.linearGradient(
    colors = listOf(UltravioletPurple, ElectricCyan)
)

val NeonBorderGradientDefault = Brush.linearGradient(
    colors = listOf(
        ElectricCyan.copy(alpha = 0.9f),
        UltravioletPurple.copy(alpha = 0.8f),
        NeonLime.copy(alpha = 0.9f)
    )
)

val GlassBubbleSelfGradient = Brush.linearGradient(
    colors = listOf(
        NeonLime.copy(alpha = 0.35f),
        ElectricCyan.copy(alpha = 0.25f)
    )
)

val GlassBubbleOtherGradient = Brush.linearGradient(
    colors = listOf(
        DeepVioletSurfaceVariant.copy(alpha = 0.60f),
        UltravioletPurple.copy(alpha = 0.25f)
    )
)
