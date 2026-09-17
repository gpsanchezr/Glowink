package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.sin

/**
 * Renderizador Vectorial 2D de Bombas Neón (0% imágenes PNG).
 */
fun DrawScope.drawGlowBomb(
    gridX: Int,
    gridY: Int,
    tileSize: Float,
    timerMs: Long,
    timeMillis: Long
) {
    val px = gridX * tileSize + tileSize / 2f
    val py = gridY * tileSize + tileSize / 2f
    val radius = tileSize * 0.36f

    // Pulsación de energía basada en el tiempo restante
    val pulseFreq = if (timerMs < 800L) 30f else 10f
    val pulseAlpha = 0.4f + 0.6f * abs(sin(timeMillis / pulseFreq))

    // 1. Sombra base
    drawOval(
        color = Color.Black.copy(alpha = 0.4f),
        topLeft = Offset(px - radius * 0.9f, py + radius * 0.5f),
        size = Size(radius * 1.8f, radius * 0.5f)
    )

    // 2. Núcleo metálico esférico
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF2A1C4E), Color(0xFF0F0A28), Color(0xFF050310)),
            center = Offset(px - radius * 0.3f, py - radius * 0.3f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = Offset(px, py)
    )

    // 3. Anillo de energía concéntrico pulsante
    val energyColor = if (timerMs < 800L) Color(0xFFFF2222) else Color(0xFF00F0FF)
    drawCircle(
        color = energyColor.copy(alpha = pulseAlpha),
        radius = radius * 1.15f,
        center = Offset(px, py),
        style = Stroke(width = 3f)
    )
    drawCircle(
        color = energyColor,
        radius = radius * 0.85f,
        center = Offset(px, py),
        style = Stroke(width = 2.5f)
    )

    // 4. Mecha encendida con chispas neón
    val fuseTop = Offset(px + radius * 0.2f, py - radius * 1.1f)
    drawLine(
        color = Color(0xFFFFD24C),
        start = Offset(px, py - radius * 0.7f),
        end = fuseTop,
        strokeWidth = 3f
    )

    val sparkAlpha = 0.6f + 0.4f * abs(sin(timeMillis / 5f))
    drawCircle(
        color = Color(0xFFFF007F).copy(alpha = sparkAlpha),
        radius = radius * 0.35f,
        center = fuseTop
    )
    drawCircle(
        color = Color.White,
        radius = radius * 0.18f,
        center = fuseTop
    )
}
