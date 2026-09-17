package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Renderizador de Mundo 5: RUINAS PRISMA / CYBER
 * Estética de templo antiguo cyberpunk, baldosas de obsidiana con runas rojas/doradas y estatuas de prisma.
 */
fun DrawScope.drawPrismRuinsTheme(
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
    val red = Color(0xFFFF1744)
    val gold = Color(0xFFFFD700)

    // 1. Baldosa de obsidiana volcánica
    drawRect(color = Color(0xFF1F0307), topLeft = Offset(tileLeft, tileTop), size = Size(tileSize, tileSize))

    // Runas talladas iluminadas
    if ((col + row * 4) % 5 == 2) {
        drawLine(red.copy(alpha = 0.4f), Offset(tileLeft + tileSize * 0.3f, tileTop + tileSize * 0.3f), Offset(tileLeft + tileSize * 0.7f, tileTop + tileSize * 0.7f), strokeWidth = 2f)
        drawLine(red.copy(alpha = 0.4f), Offset(tileLeft + tileSize * 0.7f, tileTop + tileSize * 0.3f), Offset(tileLeft + tileSize * 0.3f, tileTop + tileSize * 0.7f), strokeWidth = 2f)
    }

    drawRect(
        brush = Brush.linearGradient(listOf(red.copy(alpha = 0.6f), gold.copy(alpha = 0.6f)), start = Offset(tileLeft, tileTop), end = Offset(tileLeft + tileSize, tileTop + tileSize)),
        topLeft = Offset(tileLeft, tileTop),
        size = Size(tileSize, tileSize),
        style = Stroke(width = 1.2f)
    )

    if (isPerimeter || isPillar) {
        // Columna de Templo Prisma Antiguo
        val pad = tileSize * 0.05f
        val wallOffset = Offset(tileLeft + pad, tileTop + pad)
        val wallSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = Color(0xFF38050C), topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.12f))
        drawRoundRect(color = red, topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.12f), style = Stroke(width = 2.5f))

        // Pilar Dorado
        drawRect(gold, topLeft = Offset(wallOffset.x + wallSize.width * 0.25f, wallOffset.y + wallSize.height * 0.25f), size = Size(wallSize.width * 0.5f, wallSize.height * 0.5f))
    } else if (isCrateStanding) {
        // Caja de Cristal Volcánico
        val pad = tileSize * 0.08f
        val crateOffset = Offset(tileLeft + pad, tileTop + pad)
        val crateSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = Color(0x33FF1744), topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.12f))
        drawRoundRect(color = gold, topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.12f), style = Stroke(width = 2f))
        drawCircle(red, radius = tileSize * 0.12f, center = Offset(crateOffset.x + crateSize.width / 2f, crateOffset.y + crateSize.height / 2f))
    }
}
