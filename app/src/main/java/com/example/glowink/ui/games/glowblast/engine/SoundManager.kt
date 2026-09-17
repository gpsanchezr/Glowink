package com.example.glowink.ui.games.glowblast.engine

import android.content.Context
import android.util.Log
import com.example.glowink.util.GlowSoundManager

/**
 * Gestor de Audio Sintetizado y Efectos Sonoros para Glow Blast.
 * Permite activar/desactivar el sonido desde los ajustes del juego.
 */
object SoundManager {

    var isSoundEnabled: Boolean = true

    fun initSettings(context: Context) {
        val prefs = context.getSharedPreferences("GlowBlastSettings", Context.MODE_PRIVATE)
        isSoundEnabled = prefs.getBoolean("sound_enabled", true)
    }

    fun toggleSound(context: Context, enabled: Boolean) {
        isSoundEnabled = enabled
        val prefs = context.getSharedPreferences("GlowBlastSettings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun buttonClick(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun bombPlaced(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun bombWarning(context: Context) {
        if (!isSoundEnabled) return
        Log.d("SoundManager", "🔊 Advertencia de Bomba")
    }

    fun explosion(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun powerUp(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun damage(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playVictory(context)
    }

    fun shield(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun teleport(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun victory(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playVictory(context)
    }

    fun defeat(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }
}
