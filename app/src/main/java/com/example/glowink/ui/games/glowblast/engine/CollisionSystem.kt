package com.example.glowink.ui.games.glowblast.engine

/**
 * Sistema de verificación de daño y colisión con explosiones.
 */
object CollisionSystem {

    fun isPlayerHitByBlast(
        playerX: Float,
        playerY: Float,
        blastTiles: List<BlastTile>,
        hitRadius: Float = 0.45f
    ): Boolean {
        for (tile in blastTiles) {
            val distDx = kotlin.math.abs(playerX - tile.gridX)
            val distDy = kotlin.math.abs(playerY - tile.gridY)
            if (distDx < hitRadius && distDy < hitRadius) {
                return true
            }
        }
        return false
    }
}
