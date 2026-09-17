package com.example.glowink.ui.games.glowblast.model

/**
 * Máquina de estados de Inteligencia Artificial para Enemigos/Bots.
 */
enum class AIState { IDLE, PATROL, CHASE, ATTACK, FLEE, DEAD }

/**
 * Tipos de Enemigos en Glow Blast.
 */
enum class EnemyType { DRONE, ANDROID }

/**
 * Modelo de Entidad Enemigo / Bot IA.
 */
data class Enemy(
    val id: String = "",
    val name: String = "Bot AI",
    val type: EnemyType = EnemyType.DRONE,
    val avatarConfig: GlowAvatarConfig = GlowAvatarConfig(characterId = "ROK", auraColor = "#FF007F"),
    var x: Float = 13.0f,
    var y: Float = 9.0f,
    var lives: Int = 2,
    var speed: Float = 2.8f,
    var state: AIState = AIState.PATROL,
    var moveDirX: Float = 0f,
    var moveDirY: Float = 0f,
    var bombCooldownMs: Long = 0L,
    val maxBombs: Int = 1,
    val explosionRange: Int = 2,
    var isAlive: Boolean = true
)
