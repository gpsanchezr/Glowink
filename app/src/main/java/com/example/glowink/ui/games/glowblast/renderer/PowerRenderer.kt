package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.glowink.ui.games.glowblast.model.GlowPower
import com.example.glowink.ui.games.glowblast.model.PowerType
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.NeonLime
import kotlin.math.abs
import kotlin.math.sin

/**
 * Renderizador Vectorial 2D de Potenciadores (Power-Ups) en Canvas (0% imágenes PNG).
 */
fun DrawScope.drawGlowPowerUp(
    power: GlowPower,
    tileSize: Float,
    timeMillis: Long
) {
    val px = power.gridX * tileSize + tileSize / 2f
    val py = power.gridY * tileSize + tileSize / 2f
    val radius = tileSize * 0.35f

    val color = when (power.type) {
        PowerType.EXPANSION_FUEGO -> Color(0xFFFF007F) // Rosa Neón Fuego
        PowerType.BOMBA_EXTRA -> ElectricCyan           // Cian Eléctrico Bomba
        PowerType.VELOCIDAD_SUPER -> NeonLime           // Verde Lima Velocidad
        PowerType.ESCUDO_NEON -> Color(0xFFFFD24C)       // Dorado Escudo
        else -> ElectricCyan
    }

    val pulse = abs(sin(timeMillis / 200f))
    val currentRadius = radius * (0.90f + pulse * 0.12f)

    // 1. Halo Neón difuminado de fondo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.1f), Color.Transparent),
            center = Offset(px, py),
            radius = currentRadius * 1.5f
        ),
        radius = currentRadius * 1.5f,
        center = Offset(px, py)
    )

    // 2. Chasis contenedor cuadrado redondeado con borde brillante
    val pad = tileSize * 0.18f
    val boxSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)
    val boxTopLeft = Offset(px - boxSize.width / 2f, py - boxSize.height / 2f)

    drawRoundRect(
        color = Color(0xFF140D2A),
        topLeft = boxTopLeft,
        size = boxSize,
        cornerRadius = CornerRadius(tileSize * 0.15f)
    )
    drawRoundRect(
        color = color,
        topLeft = boxTopLeft,
        size = boxSize,
        cornerRadius = CornerRadius(tileSize * 0.15f),
        style = Stroke(width = 2.5f)
    )

    // 3. Símbolo / Ícono Vectorial Neón
    when (power.type) {
        PowerType.EXPANSION_FUEGO -> {
            val path = Path().apply {
                moveTo(px + tileSize * 0.02f, py - tileSize * 0.22f)
                lineTo(px - tileSize * 0.14f, py + tileSize * 0.02f)
                lineTo(px + tileSize * 0.02f, py + tileSize * 0.02f)
                lineTo(px - tileSize * 0.02f, py + tileSize * 0.22f)
                lineTo(px + tileSize * 0.14f, py - tileSize * 0.02f)
                lineTo(px + tileSize * 0.02f, py - tileSize * 0.02f)
                close()
            }
            drawPath(path = path, color = color)
        }
        PowerType.BOMBA_EXTRA -> {
            drawCircle(color = color, radius = tileSize * 0.14f, center = Offset(px, py + tileSize * 0.03f))
            drawCircle(color = Color.White, radius = tileSize * 0.04f, center = Offset(px - tileSize * 0.04f, py - tileSize * 0.01f))
            drawLine(Color(0xFFFFD24C), Offset(px, py - tileSize * 0.10f), Offset(px + tileSize * 0.08f, py - tileSize * 0.18f), strokeWidth = 2.5f)
        }
        PowerType.VELOCIDAD_SUPER -> {
            val path = Path().apply {
                moveTo(px - tileSize * 0.14f, py - tileSize * 0.12f)
                lineTo(px, py - tileSize * 0.02f)
                lineTo(px + tileSize * 0.14f, py - tileSize * 0.12f)

                moveTo(px - tileSize * 0.14f, py + tileSize * 0.04f)
                lineTo(px, py + tileSize * 0.14f)
                lineTo(px + tileSize * 0.14f, py + tileSize * 0.04f)
            }
            drawPath(path = path, color = color, style = Stroke(width = 3.5f))
        }
        PowerType.ESCUDO_NEON -> {
            val path = Path().apply {
                moveTo(px - tileSize * 0.12f, py - tileSize * 0.14f)
                lineTo(px + tileSize * 0.12f, py - tileSize * 0.14f)
                lineTo(px + tileSize * 0.14f, py)
                quadraticTo(px, py + tileSize * 0.22f, px, py + tileSize * 0.22f)
                quadraticTo(px, py + tileSize * 0.22f, px - tileSize * 0.14f, py)
                close()
            }
            drawPath(path = path, color = color, style = Stroke(width = 2.5f))
            drawCircle(color = color, radius = tileSize * 0.04f, center = Offset(px, py - tileSize * 0.02f))
        }
        else -> {
            drawCircle(color = color, radius = tileSize * 0.12f, center = Offset(px, py))
        }
    }
}
