package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.core.graphics.toColorInt
import com.example.glowink.ui.games.glowblast.model.GlowAvatarConfig

private val reusableHairPath = Path()

/**
 * Preajustes de Personajes para Glow Blast.
 */
object GlowCharacterPresets {
    val NEO = GlowAvatarConfig(
        characterId = "NEO",
        skinColor = "#F4C2A1",
        hairStyle = "MOHAWK_NEON",
        hairColor = "#00F0FF",
        clothingStyle = "HOODIE_NEON",
        clothingColor = "#1E1735",
        accessoryId = "NEON_GLASSES",
        auraColor = "#00F0FF"
    )

    val LUNA = GlowAvatarConfig(
        characterId = "LUNA",
        skinColor = "#F6D9C4",
        hairStyle = "LARGO_ONDULADO",
        hairColor = "#FF007F",
        clothingStyle = "CHAQUETA_CIRCUITO",
        clothingColor = "#3A002A",
        accessoryId = "AURA_NEON",
        auraColor = "#FF007F"
    )

    val ZETA = GlowAvatarConfig(
        characterId = "ZETA",
        skinColor = "#D9986B",
        hairStyle = "BOB_PIXIE",
        hairColor = "#9D4EDD",
        clothingStyle = "BOMBER_HOLO",
        clothingColor = "#23003A",
        accessoryId = "VR_HEADSET",
        auraColor = "#9D4EDD"
    )

    val VOLT = GlowAvatarConfig(
        characterId = "VOLT",
        skinColor = "#AD6E42",
        hairStyle = "CORTO_CLASICO",
        hairColor = "#39FF14",
        clothingStyle = "OVEROL_CYBER",
        clothingColor = "#0A290C",
        accessoryId = "AUDIFONOS_GLOW",
        auraColor = "#39FF14"
    )

    val ALL_PRESETS = listOf(NEO, LUNA, ZETA, VOLT)
}

/**
 * Composable que renderiza un personaje de Glow Blast en 100% Canvas 2D Vectorial.
 */
@Composable
fun GlowCharacterCanvas(
    config: GlowAvatarConfig,
    modifier: Modifier = Modifier,
    isMoving: Boolean = false,
    walkCycle: Float = 0f
) {
    Canvas(modifier = modifier) {
        drawGlowCharacter(config = config, isMoving = isMoving, walkCycle = walkCycle)
    }
}

/**
 * Función de dibujado en DrawScope para renderizar el personaje con primitivas vectoriales 2D.
 */
fun DrawScope.drawGlowCharacter(
    config: GlowAvatarConfig,
    isMoving: Boolean = false,
    walkCycle: Float = 0f
) {
    val w = size.width
    val h = size.height

    val skinColor = parseColorSafely(config.skinColor, Color(0xFFF4C2A1))
    val hairColor = parseColorSafely(config.hairColor, Color(0xFF00F0FF))
    val auraColor = parseColorSafely(config.auraColor, Color(0xFF00F0FF))
    val clothingColor = parseColorSafely(config.clothingColor, Color(0xFF1E1735))

    // 1. Sombra / Aura Neón en el Suelo
    val baseCenter = Offset(w * 0.5f, h * 0.88f)
    drawOval(
        color = auraColor.copy(alpha = 0.45f),
        topLeft = Offset(baseCenter.x - w * 0.35f, baseCenter.y - h * 0.05f),
        size = Size(w * 0.70f, h * 0.10f)
    )
    drawOval(
        color = auraColor,
        topLeft = Offset(baseCenter.x - w * 0.35f, baseCenter.y - h * 0.05f),
        size = Size(w * 0.70f, h * 0.10f),
        style = Stroke(width = 3.5f)
    )

    // 2. Piernas y Botas Cyber
    val legY = h * 0.64f
    val legW = w * 0.12f
    val legH = h * 0.22f

    // Pierna Izquierda
    drawRoundRect(
        color = Color(0xFF100B26),
        topLeft = Offset(w * 0.34f, legY),
        size = Size(legW, legH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = auraColor,
        topLeft = Offset(w * 0.32f, legY + legH - h * 0.06f),
        size = Size(legW + w * 0.04f, h * 0.06f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Pierna Derecha
    drawRoundRect(
        color = Color(0xFF100B26),
        topLeft = Offset(w * 0.54f, legY),
        size = Size(legW, legH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = auraColor,
        topLeft = Offset(w * 0.54f, legY + legH - h * 0.06f),
        size = Size(legW + w * 0.04f, h * 0.06f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // 3. Torso y Chaqueta Cyber
    val torsoTop = h * 0.38f
    val torsoW = w * 0.46f
    val torsoH = h * 0.28f
    val torsoLeft = w * 0.5f - torsoW / 2f

    drawRoundRect(
        color = clothingColor,
        topLeft = Offset(torsoLeft, torsoTop),
        size = Size(torsoW, torsoH),
        cornerRadius = CornerRadius(16f, 16f)
    )
    drawRoundRect(
        color = auraColor,
        topLeft = Offset(torsoLeft, torsoTop),
        size = Size(torsoW, torsoH),
        cornerRadius = CornerRadius(16f, 16f),
        style = Stroke(width = 3f)
    )

    // Detalle de Cierre / Neón en el Pecho
    drawLine(
        color = auraColor,
        start = Offset(w * 0.5f, torsoTop + 6f),
        end = Offset(w * 0.5f, torsoTop + torsoH - 6f),
        strokeWidth = 3.5f
    )

    // 4. Cabeza
    val headW = w * 0.42f
    val headH = h * 0.32f
    val headTop = h * 0.10f
    val headLeft = w * 0.5f - headW / 2f

    // Cuello
    drawRect(
        color = skinColor,
        topLeft = Offset(w * 0.45f, headTop + headH * 0.7f),
        size = Size(w * 0.10f, h * 0.10f)
    )

    // Cabeza Base Oval
    drawOval(
        color = skinColor,
        topLeft = Offset(headLeft, headTop),
        size = Size(headW, headH)
    )

    // Ojos Neón
    val eyeY = headTop + headH * 0.45f
    val eyeW = headW * 0.22f
    val eyeH = headH * 0.18f

    // Ojo Izquierdo
    drawOval(color = Color.White, topLeft = Offset(w * 0.38f - eyeW / 2f, eyeY - eyeH / 2f), size = Size(eyeW, eyeH))
    drawCircle(color = auraColor, radius = eyeW * 0.35f, center = Offset(w * 0.38f, eyeY))
    drawCircle(color = Color.Black, radius = eyeW * 0.18f, center = Offset(w * 0.38f, eyeY))

    // Ojo Derecho
    drawOval(color = Color.White, topLeft = Offset(w * 0.62f - eyeW / 2f, eyeY - eyeH / 2f), size = Size(eyeW, eyeH))
    drawCircle(color = auraColor, radius = eyeW * 0.35f, center = Offset(w * 0.62f, eyeY))
    drawCircle(color = Color.Black, radius = eyeW * 0.18f, center = Offset(w * 0.62f, eyeY))

    // Boca
    drawLine(
        color = Color(0xFFC84B68),
        start = Offset(w * 0.44f, headTop + headH * 0.75f),
        end = Offset(w * 0.56f, headTop + headH * 0.75f),
        strokeWidth = 3f
    )

    // 5. Cabello Cyberpunk Anclado al Cráneo (Object Reuse para 60 FPS)
    val hairPath = reusableHairPath
    hairPath.reset()
    when (config.hairStyle.uppercase()) {
        "MOHAWK_NEON" -> {
            hairPath.moveTo(w * 0.44f, headTop + 10f)
            hairPath.lineTo(w * 0.42f, headTop - h * 0.08f)
            hairPath.lineTo(w * 0.50f, headTop - h * 0.12f)
            hairPath.lineTo(w * 0.58f, headTop - h * 0.08f)
            hairPath.lineTo(w * 0.56f, headTop + 10f)
            hairPath.close()
            drawPath(path = hairPath, color = hairColor)
        }
        "LARGO_ONDULADO" -> {
            hairPath.moveTo(w * 0.26f, headTop + headH * 0.6f)
            hairPath.quadraticTo(w * 0.28f, headTop - h * 0.04f, w * 0.5f, headTop - h * 0.04f)
            hairPath.quadraticTo(w * 0.72f, headTop - h * 0.04f, w * 0.74f, headTop + headH * 0.6f)
            hairPath.quadraticTo(w * 0.78f, headTop + headH * 1.2f, w * 0.68f, headTop + headH * 1.5f)
            hairPath.lineTo(w * 0.60f, headTop + headH * 0.9f)
            hairPath.quadraticTo(w * 0.5f, headTop + 20f, w * 0.40f, headTop + headH * 0.9f)
            hairPath.lineTo(w * 0.32f, headTop + headH * 1.5f)
            hairPath.quadraticTo(w * 0.22f, headTop + headH * 1.2f, w * 0.26f, headTop + headH * 0.6f)
            hairPath.close()
            drawPath(path = hairPath, color = hairColor)
        }
        else -> {
            hairPath.moveTo(w * 0.28f, headTop + headH * 0.4f)
            hairPath.quadraticTo(w * 0.30f, headTop - h * 0.03f, w * 0.5f, headTop - h * 0.03f)
            hairPath.quadraticTo(w * 0.70f, headTop - h * 0.03f, w * 0.72f, headTop + headH * 0.4f)
            hairPath.lineTo(w * 0.60f, headTop + headH * 0.25f)
            hairPath.quadraticTo(w * 0.45f, headTop + headH * 0.30f, w * 0.28f, headTop + headH * 0.4f)
            hairPath.close()
            drawPath(path = hairPath, color = hairColor)
        }
    }

    // 6. Accesorios (Gafas Píxel / Visor VR)
    if (config.accessoryId.equals("NEON_GLASSES", ignoreCase = true)) {
        val glassY = eyeY - 2f
        drawRect(
            color = Color(0xDD0A0518),
            topLeft = Offset(w * 0.28f, glassY - h * 0.03f),
            size = Size(w * 0.44f, h * 0.06f)
        )
        drawRect(
            color = auraColor,
            topLeft = Offset(w * 0.28f, glassY - h * 0.03f),
            size = Size(w * 0.44f, h * 0.06f),
            style = Stroke(width = 2.5f)
        )
    } else if (config.accessoryId.equals("VR_HEADSET", ignoreCase = true)) {
        val visorY = eyeY - 4f
        drawRoundRect(
            color = Color(0xFF140D2A),
            topLeft = Offset(w * 0.24f, visorY - h * 0.04f),
            size = Size(w * 0.52f, h * 0.08f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = auraColor,
            topLeft = Offset(w * 0.24f, visorY - h * 0.04f),
            size = Size(w * 0.52f, h * 0.08f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = 3f)
        )
    }
}

private fun parseColorSafely(hexString: String, default: Color): Color {
    if (hexString.isBlank()) return default
    return try {
        val cleanHex = hexString.trim()
        val formatted = if (cleanHex.startsWith("#")) cleanHex else "#$cleanHex"
        Color(formatted.toColorInt())
    } catch (_: Exception) {
        default
    }
}
