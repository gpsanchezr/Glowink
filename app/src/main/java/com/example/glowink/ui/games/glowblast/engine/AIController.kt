package com.example.glowink.ui.games.glowblast.engine

import com.example.glowink.ui.games.glowblast.model.AIState
import com.example.glowink.ui.games.glowblast.model.Enemy
import com.example.glowink.ui.games.glowblast.model.GlowBomb
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Motor de Inteligencia Artificial para Bots Rivales de Glow Blast.
 */
object AIController {

    fun updateEnemyAI(
        enemy: Enemy,
        playerX: Float,
        playerY: Float,
        placedBombs: List<GlowBomb>,
        activeExplosions: List<BlastTile>,
        gridWidth: Int = 15,
        gridHeight: Int = 11,
        isTileBlockedFunc: (Int, Int) -> Boolean,
        onPlaceBomb: (col: Int, row: Int) -> Unit
    ) {
        if (!enemy.isAlive || enemy.lives <= 0) {
            enemy.state = AIState.DEAD
            enemy.moveDirX = 0f
            enemy.moveDirY = 0f
            return
        }

        val eCol = (enemy.x + 0.5f).toInt().coerceIn(1, gridWidth - 2)
        val eRow = (enemy.y + 0.5f).toInt().coerceIn(1, gridHeight - 2)

        // 1. Detección de Peligro Inminente (FLEE)
        val isNearBomb = placedBombs.any { abs(it.gridX - eCol) <= 2 && abs(it.gridY - eRow) <= 2 }
        val isNearExplosion = activeExplosions.any { abs(it.gridX - eCol) <= 1 && abs(it.gridY - eRow) <= 1 }

        if (isNearBomb || isNearExplosion) {
            enemy.state = AIState.FLEE
            val dangerX = placedBombs.firstOrNull()?.gridX ?: eCol
            val dangerY = placedBombs.firstOrNull()?.gridY ?: eRow

            val escapeDirs = listOf(Pair(0, -1), Pair(0, 1), Pair(-1, 0), Pair(1, 0))
                .filter { (dx, dy) -> !isTileBlockedFunc(eCol + dx, eRow + dy) }

            if (escapeDirs.isNotEmpty()) {
                val best = escapeDirs.maxByOrNull { (dx, dy) ->
                    val nextCol = eCol + dx
                    val nextRow = eRow + dy
                    abs(nextCol - dangerX) + abs(nextRow - dangerY)
                }!!
                enemy.moveDirX = best.first.toFloat()
                enemy.moveDirY = best.second.toFloat()
            }
            return
        }

        // 2. Distancia al Jugador
        val distToPlayer = sqrt((enemy.x - playerX) * (enemy.x - playerX) + (enemy.y - playerY) * (enemy.y - playerY))

        if (distToPlayer < 1.8f && enemy.bombCooldownMs <= 0L) {
            // ATACAR: Colocar bomba táctica
            enemy.state = AIState.ATTACK
            enemy.moveDirX = 0f
            enemy.moveDirY = 0f
            onPlaceBomb(eCol, eRow)
            enemy.bombCooldownMs = 3500L
            return
        }

        if (distToPlayer < 6.0f) {
            // PERSECUCIÓN
            enemy.state = AIState.CHASE
            val pCol = (playerX + 0.5f).toInt()
            val pRow = (playerY + 0.5f).toInt()

            val dx = (pCol - eCol).coerceIn(-1, 1)
            val dy = (pRow - eRow).coerceIn(-1, 1)

            if (dx != 0 && !isTileBlockedFunc(eCol + dx, eRow)) {
                enemy.moveDirX = dx.toFloat()
                enemy.moveDirY = 0f
            } else if (dy != 0 && !isTileBlockedFunc(eCol, eRow + dy)) {
                enemy.moveDirX = 0f
                enemy.moveDirY = dy.toFloat()
            } else {
                choosePatrolDir(enemy, eCol, eRow, isTileBlockedFunc)
            }
        } else {
            // PATRULLAJE
            enemy.state = AIState.PATROL
            if ((enemy.moveDirX == 0f && enemy.moveDirY == 0f) || kotlin.random.Random.nextFloat() < 0.05f) {
                choosePatrolDir(enemy, eCol, eRow, isTileBlockedFunc)
            }
        }
    }

    private fun choosePatrolDir(
        enemy: Enemy,
        eCol: Int,
        eRow: Int,
        isTileBlockedFunc: (Int, Int) -> Boolean
    ) {
        val validDirs = listOf(Pair(0, -1), Pair(0, 1), Pair(-1, 0), Pair(1, 0))
            .filter { (dx, dy) -> !isTileBlockedFunc(eCol + dx, eRow + dy) }

        if (validDirs.isNotEmpty()) {
            val dir = validDirs.random()
            enemy.moveDirX = dir.first.toFloat()
            enemy.moveDirY = dir.second.toFloat()
        } else {
            enemy.moveDirX = 0f
            enemy.moveDirY = 0f
        }
    }
}
