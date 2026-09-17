package com.example.glowink.ui.games.glowblast.engine

import android.content.Context
import com.example.glowink.util.GlowHapticManager

/**
 * Gestor de Vibración Háptica Táctil para Glow Blast.
 * Permite activar/desactivar la vibración desde los ajustes del juego.
 */
object HapticManager {

    var isHapticsEnabled: Boolean = true

    fun initSettings(context: Context) {
        val prefs = context.getSharedPreferences("GlowBlastSettings", Context.MODE_PRIVATE)
        isHapticsEnabled = prefs.getBoolean("haptics_enabled", true)
    }

    fun toggleHaptics(context: Context, enabled: Boolean) {
        isHapticsEnabled = enabled
        val prefs = context.getSharedPreferences("GlowBlastSettings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
    }

    fun vibrateBombPlaced(context: Context) {
        if (!isHapticsEnabled) return
        GlowHapticManager.vibrateImpact(context)
    }

    fun vibrateDamage(context: Context) {
        if (!isHapticsEnabled) return
        GlowHapticManager.vibrateError(context)
    }

    fun vibrateExplosion(context: Context) {
        if (!isHapticsEnabled) return
        GlowHapticManager.vibrateImpact(context)
    }

    fun vibratePowerUp(context: Context) {
        if (!isHapticsEnabled) return
        GlowHapticManager.vibrateSuccess(context)
    }

    fun vibrateVictory(context: Context) {
        if (!isHapticsEnabled) return
        GlowHapticManager.vibrateSuccess(context)
    }
}
