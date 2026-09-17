package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Renderizador de Mundo 4: ISLA CELESTIAL
 * Estética de abismo estelar, cristales celestiales flotantes y portales cósmicos.
 */
fun DrawScope.drawSkyIslandsTheme(
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
    val magenta = Color(0xFFFF007F)
    val purple = Color(0xFF9D4EDD)

    // 1. Suelo de piedra cósmica flotante
    drawRect(color = Color(0xFF130326), topLeft = Offset(tileLeft, tileTop), size = Size(tileSize, tileSize))

    // Estrellas y polvo estelar
    if ((col * 7 + row * 11) % 13 == 1) {
        drawCircle(Color.White.copy(alpha = 0.8f), radius = 2f, center = Offset(tileLeft + tileSize * 0.4f, tileTop + tileSize * 0.4f))
    }

    drawRect(
        brush = Brush.linearGradient(listOf(magenta.copy(alpha = 0.6f), purple.copy(alpha = 0.6f)), start = Offset(tileLeft, tileTop), end = Offset(tileLeft + tileSize, tileTop + tileSize)),
        topLeft = Offset(tileLeft, tileTop),
        size = Size(tileSize, tileSize),
        style = Stroke(width = 1.2f)
    )

    if (isPerimeter || isPillar) {
        // Obelisco / Cristal Energético Flotante
        val pad = tileSize * 0.05f
        val wallOffset = Offset(tileLeft + pad, tileTop + pad)
        val wallSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = Color(0xFF2B004F), topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.15f))
        drawRoundRect(color = magenta, topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.15f), style = Stroke(width = 2.5f))

        // Cristal en Diamante
        val center = Offset(tileLeft + tileSize / 2f, tileTop + tileSize / 2f)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x, center.y - tileSize * 0.25f)
            lineTo(center.x + tileSize * 0.18f, center.y)
            lineTo(center.x, center.y + tileSize * 0.25f)
            lineTo(center.x - tileSize * 0.18f, center.y)
            close()
        }
        drawPath(path = path, color = magenta)
    } else if (isCrateStanding) {
        // Caja de Cristal Estelar
        val pad = tileSize * 0.08f
        val crateOffset = Offset(tileLeft + pad, tileTop + pad)
        val crateSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = Color(0x339D4EDD), topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.15f))
        drawRoundRect(color = purple, topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.15f), style = Stroke(width = 2f))
        drawCircle(magenta, radius = tileSize * 0.10f, center = Offset(crateOffset.x + crateSize.width / 2f, crateOffset.y + crateSize.height / 2f))
    }
}
