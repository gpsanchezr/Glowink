package com.example.glowink.ui.games.glowblast.model

/**
 * Estado general centralizado de la partida de Glow Blast.
 */
data class GlowBlastGameState(
    val gameId: String = "",
    val mode: GameMode = GameMode.CLASICO,
    val mapTheme: MapTheme = MapTheme.CIUDAD_NEON,
    val players: List<GlowPlayer> = emptyList(),
    val bombs: List<GlowBomb> = emptyList(),
    val powers: List<GlowPower> = emptyList(),
    val gridWidth: Int = 15,
    val gridHeight: Int = 11,
    val timeRemainingSec: Int = 165, // 02:45 minutos
    val isMatchStarted: Boolean = false,
    val isMatchFinished: Boolean = false,
    val winnerPlayerId: String? = null,
    val winnerTeamId: Int? = null
)
