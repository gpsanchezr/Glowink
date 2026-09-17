package com.example.glowink.ui.games.glowblast.maps

import com.example.glowink.ui.games.glowblast.model.MapTheme

/**
 * Configuración paramétrica de mapa procedural para Glow Blast.
 */
data class MapConfig(
    val theme: MapTheme = MapTheme.CIUDAD_NEON,
    val seed: Long = 12345L,
    val gridWidth: Int = 15,
    val gridHeight: Int = 11,
    val crateDensity: Float = 0.55f,
    val ambientParticleCount: Int = 30
)
