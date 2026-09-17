package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.glowink.ui.theme.NeonLime
import kotlin.math.abs
import kotlin.math.sin

/**
 * Renderizador de Mundo 2: BOSQUE GLOW
 * Estética de musgo bioluminiscente, árboles cibernéticos, hongos brillantes y neblina viva.
 */
fun DrawScope.drawGlowForestTheme(
    col: Int,
    row: Int,
    tileLeft: Float,
    tileTop: Float,
    tileSize: Float,
    isPillar: Boolean,
    isPerimeter: Boolean,
    isCrateStanding: Boolean,
    timeMillis: Long
) {
    val lime = NeonLime
    val emerald = Color(0xFF00FF88)

    // 1. Suelo de musgo bioluminiscente
    drawRect(color = Color(0xFF041A0A), topLeft = Offset(tileLeft, tileTop), size = Size(tileSize, tileSize))

    // Hongo bioluminiscente parpadeante en casillas aleatorias
    if ((col * 5 + row) % 9 == 3) {
        val pulse = abs(sin(timeMillis / 300f))
        val mushroomCenter = Offset(tileLeft + tileSize * 0.7f, tileTop + tileSize * 0.3f)
        drawCircle(emerald.copy(alpha = 0.4f + pulse * 0.4f), radius = tileSize * 0.18f, center = mushroomCenter)
        drawCircle(lime, radius = tileSize * 0.08f, center = mushroomCenter)
    }

    // Bordes de vegetación cyber
    drawRect(
        brush = Brush.linearGradient(listOf(lime.copy(alpha = 0.5f), emerald.copy(alpha = 0.5f)), start = Offset(tileLeft, tileTop), end = Offset(tileLeft + tileSize, tileTop + tileSize)),
        topLeft = Offset(tileLeft, tileTop),
        size = Size(tileSize, tileSize),
        style = Stroke(width = 1.2f)
    )

    if (isPerimeter || isPillar) {
        // Árbol Cibernético con Raíces de Circuito
        val pad = tileSize * 0.05f
        val wallOffset = Offset(tileLeft + pad, tileTop + pad)
        val wallSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = Color(0xFF082E12), topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.20f))
        drawRoundRect(color = lime, topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.20f), style = Stroke(width = 2.5f))

        // Copa de Árbol Cyber
        drawCircle(emerald, radius = tileSize * 0.22f, center = Offset(tileLeft + tileSize / 2f, tileTop + tileSize / 2f))
    } else if (isCrateStanding) {
        // Caja de Raíces Bioluminiscentes
        val pad = tileSize * 0.08f
        val crateOffset = Offset(tileLeft + pad, tileTop + pad)
        val crateSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = Color(0x3300FF88), topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.15f))
        drawRoundRect(color = emerald, topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.15f), style = Stroke(width = 2f))
        drawCircle(lime, radius = tileSize * 0.12f, center = Offset(crateOffset.x + crateSize.width / 2f, crateOffset.y + crateSize.height / 2f))
    }
}
