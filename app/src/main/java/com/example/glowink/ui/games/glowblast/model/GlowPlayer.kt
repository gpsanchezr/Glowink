package com.example.glowink.ui.games.glowblast.model

/**
 * Modelo de datos del Jugador en partida activa de Glow Blast.
 */
data class GlowPlayer(
    val id: String = "",
    val name: String = "Jugador",
    val avatar: GlowAvatarConfig = GlowAvatarConfig(),
    val x: Float = 1f, // Posición x en casillas de la cuadrícula
    val y: Float = 1f, // Posición y en casillas de la cuadrícula
    val lives: Int = 3,
    val maxBombs: Int = 1,
    val activeBombs: Int = 0,
    val explosionRange: Int = 2,
    val speed: Float = 3.2f, // Casillas por segundo
    val hasShield: Boolean = false,
    val hasKickPower: Boolean = false,
    val canPassWalls: Boolean = false,
    val isAlive: Boolean = true,
    val teamId: Int = 0, // 0 = sin equipo, 1 o 2 = equipos
    val score: Int = 0,
    val kills: Int = 0,
    val isHost: Boolean = false,
    val isReady: Boolean = false
)
