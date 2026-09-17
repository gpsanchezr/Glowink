package com.example.glowink.ui.games.glowblast.engine

import android.content.Context
import android.util.Log

/**
 * Gestor de Música Cyberpunk en Bucle para Glow Blast.
 * Permite activar/desactivar la música desde los ajustes del juego.
 */
object MusicManager {

    var isMusicEnabled: Boolean = true

    fun initSettings(context: Context) {
        val prefs = context.getSharedPreferences("GlowBlastSettings", Context.MODE_PRIVATE)
        isMusicEnabled = prefs.getBoolean("music_enabled", true)
    }

    fun toggleMusic(context: Context, enabled: Boolean) {
        isMusicEnabled = enabled
        val prefs = context.getSharedPreferences("GlowBlastSettings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("music_enabled", enabled).apply()
        if (!enabled) stopMusic() else playMatchMusic(context)
    }

    fun playMatchMusic(context: Context) {
        if (!isMusicEnabled) return
        Log.d("MusicManager", "🎵 Reproduciendo música cyberpunk de fondo")
    }

    fun stopMusic() {
        Log.d("MusicManager", "⏹ Música pausada")
    }
}
