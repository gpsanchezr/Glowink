package com.example.glowink.ui.games.glowblast.model

/**
 * Modelo de Potenciador (Power-Up) suelto en la cuadrícula de Glow Blast.
 */
data class GlowPower(
    val id: String = "",
    val type: PowerType = PowerType.EXPANSION_FUEGO,
    val gridX: Int = 0,
    val gridY: Int = 0,
    val durationSec: Int = 0,
    val isCollected: Boolean = false
)
