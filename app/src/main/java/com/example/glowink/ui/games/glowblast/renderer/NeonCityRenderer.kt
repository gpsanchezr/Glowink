package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.glowink.ui.theme.ElectricCyan

/**
 * Renderizador de Mundo 1: CIUDAD NEÓN
 * Estética de asfalto mohoso, reflexiones neón en charcos, hologramas y tubos de cableado cyber.
 */
fun DrawScope.drawNeonCityTheme(
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
    val cyan = ElectricCyan
    val purple = Color(0xFF9D4EDD)

    // 1. Asfalto urbano oscuro
    drawRect(color = Color(0xFF090618), topLeft = Offset(tileLeft, tileTop), size = Size(tileSize, tileSize))

    // Charco con reflejo neón en casillas impares
    if ((row * 3 + col) % 7 == 2) {
        drawOval(
            color = cyan.copy(alpha = 0.18f),
            topLeft = Offset(tileLeft + tileSize * 0.2f, tileTop + tileSize * 0.25f),
            size = Size(tileSize * 0.6f, tileSize * 0.4f)
        )
    }

    // Cuadrícula de asfalto iluminada
    drawRect(
        brush = Brush.linearGradient(listOf(cyan.copy(alpha = 0.6f), purple.copy(alpha = 0.6f)), start = Offset(tileLeft, tileTop), end = Offset(tileLeft + tileSize, tileTop + tileSize)),
        topLeft = Offset(tileLeft, tileTop),
        size = Size(tileSize, tileSize),
        style = Stroke(width = 1.2f)
    )

    if (isPerimeter || isPillar) {
        // Muro Rascacielos con Tubos de Cableado
        val pad = tileSize * 0.05f
        val wallOffset = Offset(tileLeft + pad, tileTop + pad)
        val wallSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = Color(0xFF120B2E), topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.12f))
        drawRoundRect(color = cyan, topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.12f), style = Stroke(width = 2f))

        // Tubería de Neón Vertical
        drawLine(cyan, Offset(wallOffset.x + wallSize.width * 0.3f, wallOffset.y), Offset(wallOffset.x + wallSize.width * 0.3f, wallOffset.y + wallSize.height), strokeWidth = 2.5f)
    } else if (isCrateStanding) {
        // Caja Holográfica de Ciudad
        val pad = tileSize * 0.08f
        val crateOffset = Offset(tileLeft + pad, tileTop + pad)
        val crateSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = purple.copy(alpha = 0.3f), topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.12f))
        drawRoundRect(color = purple, topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.12f), style = Stroke(width = 2f))
        drawLine(cyan, Offset(crateOffset.x + pad, crateOffset.y + pad), Offset(crateOffset.x + crateSize.width - pad, crateOffset.y + crateSize.height - pad), strokeWidth = 2f)
        drawLine(cyan, Offset(crateOffset.x + crateSize.width - pad, crateOffset.y + pad), Offset(crateOffset.x + pad, crateOffset.y + crateSize.height - pad), strokeWidth = 2f)
    }
}
