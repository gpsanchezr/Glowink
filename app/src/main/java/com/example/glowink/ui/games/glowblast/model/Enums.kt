package com.example.glowink.ui.games.glowblast.model

/**
 * Modos de Juego disponibles en Glow Blast.
 */
enum class GameMode(val title: String, val description: String) {
    CLASICO("CLÁSICO", "Elimina a tus rivales"),
    EQUIPOS("EQUIPOS", "2 vs 2 en cooperación"),
    TODOS_CONTRA_TODOS("TODOS CONTRA TODOS", "4 jugadores en batalla libre"),
    TORNEO("TORNEO", "Sobrevive y gana puntos extra")
}

/**
 * Temas visuales de Mapas Neón en Glow Blast.
 */
enum class MapTheme(val displayName: String, val difficultyLabel: String) {
    CIUDAD_NEON("Ciudad Neón", "Media"),
    BOSQUE_GLOW("Bosque Glow", "Fácil"),
    LABORATORIO("Laboratorio Cyber", "Alta"),
    ISLA_CELESTIAL("Isla Celestial", "Media"),
    RUINAS_CYBER("Ruinas Cyber", "Extrema")
}

/**
 * Tipos de Potenciadores (Power-Ups) en el campo de juego.
 */
enum class PowerType(val displayName: String, val icon: String) {
    EXPANSION_FUEGO("Expansión Fuego", "⚡"),
    BOMBA_EXTRA("Bomba Extra", "💣"),
    VELOCIDAD_SUPER("Súper Velocidad", "👟"),
    ESCUDO_NEON("Escudo Neón", "🛡️"),
    PATADA_BOMBA("Patada Bomba", "🥾"),
    PASAR_PAREDES("Pasar Paredes", "👻")
}
