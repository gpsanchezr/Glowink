package com.example.glowink.ui.games.glowblast.model

/**
 * Modelo de datos para las Bombas colocadas en el mapa de Glow Blast.
 */
data class GlowBomb(
    val id: String = "",
    val ownerId: String = "",
    val gridX: Int = 0,
    val gridY: Int = 0,
    val range: Int = 2,
    val timerMs: Long = 2500L,
    val isMoving: Boolean = false,
    val moveDirX: Int = 0,
    val moveDirY: Int = 0,
    val isDetonated: Boolean = false
)
