package com.example.glowink.ui.games.glowblast.model

/**
 * Fases del Jefe Cybernético Multi-Fase.
 */
enum class BossPhase { PHASE_1, PHASE_2, PHASE_3_CYBER_NOVA }

/**
 * Modelo de Entidad del Jefe (Boss) en Glow Blast.
 */
data class BossEntity(
    val id: String = "cyber_boss_01",
    val name: String = "CYBER MECH TITAN",
    var phase: BossPhase = BossPhase.PHASE_1,
    var hp: Int = 100,
    val maxHp: Int = 100,
    var x: Float = 7.0f,
    var y: Float = 5.0f,
    var speed: Float = 2.0f,
    var isAlive: Boolean = true,
    var shieldActive: Boolean = false,
    var moveDirX: Float = 1f,
    var moveDirY: Float = 0f,
    var attackCooldownMs: Long = 0L,
    var novaTimerMs: Long = 0L,
    var isNovaActive: Boolean = false
)
