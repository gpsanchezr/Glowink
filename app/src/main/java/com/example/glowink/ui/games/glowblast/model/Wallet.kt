package com.example.glowink.ui.games.glowblast.model

/**
 * Modelo de Billetera y Economía de Glow Blast.
 */
data class Wallet(
    val glowCoins: Int = 1500,
    val gems: Int = 45,
    val xp: Int = 500,
    val tokens: Int = 10,
    val level: Int = 1
)
