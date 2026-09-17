package com.example.glowink.ui.games.glowblast.engine

import android.content.Context
import android.util.Log
import com.example.glowink.util.GlowSoundManager

/**
 * Gestor de Audio Sintetizado y Efectos Sonoros Neón para Glow Blast.
 * Permite activar/desactivar el sonido desde los ajustes del juego.
 */
object GlowBlastSoundManager {

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

    fun playButtonClick(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun playBombPlaced(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun playBombWarning(context: Context) {
        if (!isSoundEnabled) return
        Log.d("GlowBlastSound", "🔊 [Sound] Advertencia de bomba (Beep parpadeo)")
    }

    fun playExplosion(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun playPowerUp(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun playDamage(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playVictory(context)
    }

    fun playShield(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun playTeleport(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }

    fun playVictory(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playVictory(context)
    }

    fun playDefeat(context: Context) {
        if (!isSoundEnabled) return
        GlowSoundManager.playGameAction(context)
    }
}
