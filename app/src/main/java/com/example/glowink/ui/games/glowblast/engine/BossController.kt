package com.example.glowink.ui.games.glowblast.engine

import com.example.glowink.ui.games.glowblast.model.BossEntity
import com.example.glowink.ui.games.glowblast.model.BossPhase

/**
 * Control del Jefe Cybernético Multi-Fase para Glow Blast.
 */
object BossController {

    fun updateBoss(
        boss: BossEntity,
        playerX: Float,
        playerY: Float,
        delta: Float,
        timeMillis: Long,
        gridWidth: Int = 15,
        gridHeight: Int = 11,
        isTileBlockedFunc: (Int, Int) -> Boolean,
        onPlaceBomb: (col: Int, row: Int, range: Int) -> Unit
    ) {
        if (!boss.isAlive || boss.hp <= 0) {
            boss.isAlive = false
            return
        }

        // 1. Transición Dinámica de Fases según HP
        val hpPercentage = boss.hp.toFloat() / boss.maxHp.toFloat()

        boss.phase = when {
            hpPercentage > 0.65f -> BossPhase.PHASE_1
            hpPercentage > 0.30f -> BossPhase.PHASE_2
            else -> BossPhase.PHASE_3_CYBER_NOVA
        }

        // 2. Ajuste de Parámetros según Fase
        when (boss.phase) {
            BossPhase.PHASE_1 -> {
                boss.speed = 2.2f
                boss.shieldActive = false
            }
            BossPhase.PHASE_2 -> {
                boss.speed = 3.2f
                boss.shieldActive = (timeMillis / 2000L % 2L == 0L)
            }
            BossPhase.PHASE_3_CYBER_NOVA -> {
                boss.speed = 4.0f
                boss.shieldActive = false
            }
        }

        val deltaMs = (delta * 1000f).toLong()
        if (boss.attackCooldownMs > 0L) boss.attackCooldownMs = (boss.attackCooldownMs - deltaMs).coerceAtLeast(0L)
        if (boss.novaTimerMs > 0L) boss.novaTimerMs = (boss.novaTimerMs - deltaMs).coerceAtLeast(0L)

        val bCol = (boss.x + 0.5f).toInt().coerceIn(1, gridWidth - 2)
        val bRow = (boss.y + 0.5f).toInt().coerceIn(1, gridHeight - 2)

        // 3. Patrones de Ataque
        when (boss.phase) {
            BossPhase.PHASE_1 -> {
                if (boss.attackCooldownMs <= 0L) {
                    onPlaceBomb(bCol, bRow, 2)
                    boss.attackCooldownMs = 3000L
                }
            }
            BossPhase.PHASE_2 -> {
                if (boss.attackCooldownMs <= 0L) {
                    onPlaceBomb(bCol, bRow, 3)
                    onPlaceBomb((bCol + 1).coerceIn(1, gridWidth - 2), bRow, 3)
                    boss.attackCooldownMs = 2000L
                }
            }
            BossPhase.PHASE_3_CYBER_NOVA -> {
                if (boss.novaTimerMs <= 0L) {
                    boss.isNovaActive = true
                    boss.novaTimerMs = 4000L
                    onPlaceBomb(bCol, bRow, 5)
                } else if (boss.novaTimerMs < 3000L) {
                    boss.isNovaActive = false
                }
            }
        }

        // 4. Movimiento del Jefe en Cuadrícula
        val pCol = (playerX + 0.5f).toInt()
        val pRow = (playerY + 0.5f).toInt()

        val targetDx = (pCol - bCol).coerceIn(-1, 1)
        val targetDy = (pRow - bRow).coerceIn(-1, 1)

        if (targetDx != 0 && !isTileBlockedFunc(bCol + targetDx, bRow)) {
            boss.moveDirX = targetDx.toFloat()
            boss.moveDirY = 0f
        } else if (targetDy != 0 && !isTileBlockedFunc(bCol, bRow + targetDy)) {
            boss.moveDirX = 0f
            boss.moveDirY = targetDy.toFloat()
        }

        val nextX = boss.x + boss.moveDirX * boss.speed * delta
        val nextY = boss.y + boss.moveDirY * boss.speed * delta

        if (!isTileBlockedFunc((nextX + 0.45f * boss.moveDirX).toInt(), (boss.y + 0.45f).toInt())) {
            boss.x = nextX.coerceIn(1.0f, (gridWidth - 2).toFloat())
        }
        if (!isTileBlockedFunc((boss.x + 0.45f).toInt(), (nextY + 0.45f * boss.moveDirY).toInt())) {
            boss.y = nextY.coerceIn(1.0f, (gridHeight - 2).toFloat())
        }
    }
}
