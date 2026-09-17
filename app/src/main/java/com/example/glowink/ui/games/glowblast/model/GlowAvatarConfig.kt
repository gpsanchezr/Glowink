package com.example.glowink.ui.games.glowblast.model

/**
 * Configuración paramétrica de personaje y avatar para Glow Blast.
 */
data class GlowAvatarConfig(
    val characterId: String = "NEO", // Neo, Luna, Zeta, Rok
    val skinColor: String = "#F4C2A1",
    val hairStyle: String = "MOHAWK_NEON",
    val hairColor: String = "#00F0FF",
    val clothingStyle: String = "HOODIE_NEON",
    val clothingColor: String = "#1E1735",
    val accessoryId: String = "NEON_GLASSES",
    val auraColor: String = "#39FF14"
)
