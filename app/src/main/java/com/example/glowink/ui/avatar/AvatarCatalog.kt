package com.example.glowink.ui.avatar

import androidx.compose.ui.graphics.Color

/**
 * Catálogo centralizado de todas las opciones de personalización de avatar disponibles
 * en Glowink (paso "Crea tu Personaje" del registro y la pantalla "Editar Avatar" del perfil).
 *
 * Es la ÚNICA fuente de verdad: para agregar un peinado, color u accesorio nuevo basta con
 * añadir una entrada aquí. [com.example.glowink.ui.avatar.GlowAvatar] dibuja cualquier
 * combinación automáticamente a partir de estos identificadores, y las pantallas de edición
 * generan sus grillas de selección iterando estas listas — nunca hay que tocar el dibujo ni
 * la UI de selección para sumar una variante nueva.
 */
object AvatarCatalog {

    data class SwatchOption(val id: String, val label: String, val color: Color)
    data class StyleOption(val id: String, val label: String, val emoji: String)

    val siluetas = listOf(
        StyleOption("FEMENINO", "Femenino", "\uD83D\uDC69"),
        StyleOption("MASCULINO", "Masculino", "\uD83D\uDC68")
    )

    val tonosPiel = listOf(
        SwatchOption("PIEL_PORCELANA", "Porcelana", Color(0xFFF6D9C4)),
        SwatchOption("PIEL_CLARA", "Clara", Color(0xFFF4C2A1)),
        SwatchOption("PIEL_MIEL", "Miel", Color(0xFFD9986B)),
        SwatchOption("PIEL_CANELA", "Canela", Color(0xFFAD6E42)),
        SwatchOption("PIEL_CACAO", "Cacao", Color(0xFF7A4B2A)),
        SwatchOption("PIEL_EBANO", "Ébano", Color(0xFF4A2F1F))
    )

    val estilosPelo = listOf(
        StyleOption("CORTO_CLASICO", "Corto Clásico", "\u2702\uFE0F"),
        StyleOption("MOHAWK_NEON", "Mohawk Neón", "\u26A1"),
        StyleOption("LARGO_ONDULADO", "Largo Ondulado", "\uD83C\uDF0A"),
        StyleOption("BOB_PIXIE", "Bob Pixie", "\u2728"),
        StyleOption("TRENZAS_GLOW", "Trenzas Glow", "\uD83E\uDDF6"),
        StyleOption("RAPADO", "Rapado", "\uD83D\uDCA0")
    )

    val coloresPelo = listOf(
        SwatchOption("PELO_VIOLETA", "Violeta", Color(0xFF9D4EDD)),
        SwatchOption("PELO_CIAN", "Cian", Color(0xFF00F0FF)),
        SwatchOption("PELO_LIMA", "Lima", Color(0xFF39FF14)),
        SwatchOption("PELO_ROSA", "Rosa Neón", Color(0xFFFF4FD8)),
        SwatchOption("PELO_NEGRO", "Negro", Color(0xFF2A2438)),
        SwatchOption("PELO_CASTANO", "Castaño", Color(0xFF6B4226)),
        SwatchOption("PELO_RUBIO", "Rubio", Color(0xFFE8C77E)),
        SwatchOption("PELO_ARCOIRIS", "Arcoíris", Color(0xFFFF9AD5))
    )

    val estilosOjos = listOf(
        StyleOption("REDONDOS", "Redondos", "\uD83D\uDD35"),
        StyleOption("GATUNOS", "Gatunos", "\uD83D\uDC31"),
        StyleOption("SOÑADORES", "Soñadores", "\uD83D\uDCAB"),
        StyleOption("DECIDIDOS", "Decididos", "\u26A1")
    )

    val coloresOjos = listOf(
        SwatchOption("OJOS_LIMA", "Lima", Color(0xFF39FF14)),
        SwatchOption("OJOS_CIAN", "Cian", Color(0xFF00F0FF)),
        SwatchOption("OJOS_VIOLETA", "Violeta", Color(0xFFC77DFF)),
        SwatchOption("OJOS_AMBAR", "Ámbar", Color(0xFFFFB84D)),
        SwatchOption("OJOS_CAFE", "Café", Color(0xFF6B4226)),
        SwatchOption("OJOS_GRIS", "Gris Acero", Color(0xFFAAB4C0))
    )

    val estilosRopa = listOf(
        StyleOption("HOODIE_NEON", "Hoodie Neón", "\uD83E\uDDE5"),
        StyleOption("CHAQUETA_CIRCUITO", "Chaqueta Circuito", "\u26A1"),
        StyleOption("CAMISETA_GLOW", "Camiseta Glow", "\uD83D\uDC55"),
        StyleOption("OVEROL_CYBER", "Overol Cyber", "\uD83D\uDEE0\uFE0F"),
        StyleOption("BOMBER_HOLO", "Bomber Holo", "\uD83C\uDF08")
    )

    val coloresRopa = listOf(
        SwatchOption("ROPA_OBSIDIANA", "Obsidiana", Color(0xFF1E1735)),
        SwatchOption("ROPA_VIOLETA", "Violeta", Color(0xFF5A189A)),
        SwatchOption("ROPA_CIAN", "Cian Oscuro", Color(0xFF00838F)),
        SwatchOption("ROPA_MAGENTA", "Magenta", Color(0xFF8E0B72)),
        SwatchOption("ROPA_BLANCO", "Blanco Holo", Color(0xFFE8E4F3))
    )

    val acentosRopa = listOf(
        SwatchOption("ACENTO_CIAN", "Cian", Color(0xFF00F0FF)),
        SwatchOption("ACENTO_LIMA", "Lima", Color(0xFF39FF14)),
        SwatchOption("ACENTO_ROSA", "Rosa", Color(0xFFFF4FD8)),
        SwatchOption("ACENTO_DORADO", "Dorado", Color(0xFFFFD24C))
    )

    val accesorios = listOf(
        StyleOption("NINGUNO", "Sin accesorio", "➖"),
        StyleOption("NEON_GLASSES", "Gafas Píxel", "👓"),
        StyleOption("VR_HEADSET", "Visor VR", "\uD83E\uDD7D"),
        StyleOption("GORRA_CYBER", "Gorra Cyber", "\uD83E\uDDE2"),
        StyleOption("AURA_NEON", "Aura Neón", "\uD83D\uDD2E"),
        StyleOption("AUDIFONOS_GLOW", "Audífonos Glow", "\uD83C\uDFA7")
    )

    fun swatch(list: List<SwatchOption>, id: String): SwatchOption = list.find { it.id == id } ?: list.first()
    fun style(list: List<StyleOption>, id: String): StyleOption = list.find { it.id == id } ?: list.first()
}
