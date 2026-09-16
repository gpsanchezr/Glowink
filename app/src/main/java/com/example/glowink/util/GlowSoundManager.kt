package com.example.glowink.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/**
 * Gestor de efectos de sonido neón para Glowink.
 * Usa SoundPool para una latencia mínima en juegos y chat.
 */
object GlowSoundManager {
    private var soundPool: SoundPool? = null
    private val soundsMap = mutableMapOf<String, Int>()

    fun init(context: Context) {
        if (soundPool != null) return

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        // Cargamos sonidos placeholder o de sistema si no hay recursos
        // En una app real, aquí cargaríamos R.raw.send_message, R.raw.game_win, etc.
        // Como no tengo archivos físicos, intentaremos cargar algunos sonidos de sistema comunes
        // o simplemente prepararemos el mapa para que sea fácil de extender.
    }

    fun playMessageSent(context: Context) {
        // Sonido tipo "pop" o "woosh"
        playNeonSound(context, "send_message")
    }

    fun playGameAction(context: Context) {
        // Sonido tipo "click" o "beep"
        playNeonSound(context, "game_action")
    }

    fun playVictory(context: Context) {
        playNeonSound(context, "victory")
    }

    fun play(soundKey: String, volume: Float = 1.0f) {
        val soundId = soundsMap[soundKey]
        if (soundId != null && soundPool != null) {
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            Log.d("GlowSoundManager", "Efecto Sintetizador Neón 8-bit: $soundKey")
        }
    }

    private fun playNeonSound(context: Context, key: String) {
        val sp = soundPool ?: run {
            init(context)
            soundPool
        }
        
        val soundId = soundsMap[key]
        if (soundId != null) {
            sp?.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            // Intentamos cargar un sonido genérico del sistema la primera vez
            // En producción aquí iría: soundsMap[key] = sp!!.load(context, R.raw.mi_sonido, 1)
            Log.d("GlowSoundManager", "Reproduciendo sonido neón: $key (Simulado)")
        }
    }
}
