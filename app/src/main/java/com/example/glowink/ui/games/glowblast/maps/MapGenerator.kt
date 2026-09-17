package com.example.glowink.ui.games.glowblast.maps

import kotlin.random.Random

/**
 * Generador procedural de tableros basado en semillas (Seed) para reproducción exacta de mapas.
 */
object MapGenerator {

    fun generateCratePositions(config: MapConfig): Set<Pair<Int, Int>> {
        val random = Random(config.seed)
        val crates = mutableSetOf<Pair<Int, Int>>()

        for (row in 1 until config.gridHeight - 1) {
            for (col in 1 until config.gridWidth - 1) {
                val isPillar = (row % 2 == 0) && (col % 2 == 0)
                if (isPillar) continue

                val isSpawnZone = (row <= 2 && col <= 2) ||
                        (row <= 2 && col >= config.gridWidth - 3) ||
                        (row >= config.gridHeight - 3 && col <= 2) ||
                        (row >= config.gridHeight - 3 && col >= config.gridWidth - 3)

                if (!isSpawnZone) {
                    if (random.nextFloat() < config.crateDensity) {
                        crates.add(Pair(col, row))
                    }
                }
            }
        }
        return crates
    }
}
