package com.example.glowink.ui.games.glowblast.engine

/**
 * Representa una casilla impactada por un rayo de explosión.
 */
data class BlastTile(
    val gridX: Int,
    val gridY: Int,
    val isCenter: Boolean = false
)

/**
 * Cálculo de ondas expansivas en cruz para Glow Blast.
 */
object BlastCalculator {

    fun calculateBlastTiles(
        bombX: Int,
        bombY: Int,
        range: Int,
        gridWidth: Int = 15,
        gridHeight: Int = 11,
        destroyedCrates: Set<Pair<Int, Int>>
    ): List<BlastTile> {
        val result = mutableListOf<BlastTile>()
        result.add(BlastTile(bombX, bombY, isCenter = true))

        val directions = listOf(
            Pair(0, -1), // Arriba
            Pair(0, 1),  // Abajo
            Pair(-1, 0), // Izquierda
            Pair(1, 0)   // Derecha
        )

        for ((dx, dy) in directions) {
            for (step in 1..range) {
                val cx = bombX + dx * step
                val cy = bombY + dy * step

                // Fuera de límites o Muro Indestructible
                if (cx <= 0 || cx >= gridWidth - 1 || cy <= 0 || cy >= gridHeight - 1) break
                val isPillar = (cy % 2 == 0) && (cx % 2 == 0)
                if (isPillar) break

                val isCrateInitial = (cy + cx * 3) % 5 == 1 && !(cy <= 2 && cx <= 2) && !(cy >= gridHeight - 3 && cx >= gridWidth - 3)
                val isCrateStillStanding = isCrateInitial && !destroyedCrates.contains(Pair(cx, cy))

                result.add(BlastTile(cx, cy))

                // Si impactó una caja destruible, la explosión rompe la caja y se detiene ahí
                if (isCrateStillStanding) {
                    break
                }
            }
        }
        return result
    }
}
