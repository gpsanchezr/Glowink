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
 * Renderizador de Mundo 3: LABORATORIO CYBER
 * Estética de paneles de acero reforzado, reactores con plasma azul y tanques de contención.
 */
fun DrawScope.drawCyberLabTheme(
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
    val teal = Color(0xFF00E5FF)

    // 1. Placa de acero de laboratorio
    drawRect(color = Color(0xFF04121A), topLeft = Offset(tileLeft, tileTop), size = Size(tileSize, tileSize))

    // Trazos de circuito de datos en casillas
    if ((row + col) % 3 == 0) {
        drawLine(cyan.copy(alpha = 0.25f), Offset(tileLeft, tileTop + tileSize / 2f), Offset(tileLeft + tileSize, tileTop + tileSize / 2f), strokeWidth = 1.5f)
    }

    drawRect(
        brush = Brush.linearGradient(listOf(cyan.copy(alpha = 0.6f), teal.copy(alpha = 0.6f)), start = Offset(tileLeft, tileTop), end = Offset(tileLeft + tileSize, tileTop + tileSize)),
        topLeft = Offset(tileLeft, tileTop),
        size = Size(tileSize, tileSize),
        style = Stroke(width = 1.2f)
    )

    if (isPerimeter || isPillar) {
        // Reactor / Tanque de Contención con Líquido Plasma
        val pad = tileSize * 0.05f
        val wallOffset = Offset(tileLeft + pad, tileTop + pad)
        val wallSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = Color(0xFF0A2533), topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.10f))
        drawRoundRect(color = cyan, topLeft = wallOffset, size = wallSize, cornerRadius = CornerRadius(tileSize * 0.10f), style = Stroke(width = 2.5f))

        // Núcleo de Plasma Azu
        val center = Offset(tileLeft + tileSize / 2f, tileTop + tileSize / 2f)
        drawCircle(teal, radius = tileSize * 0.20f, center = center)
        drawCircle(Color.White, radius = tileSize * 0.08f, center = center)
    } else if (isCrateStanding) {
        // Tanque de Cristal con Muestra Célula
        val pad = tileSize * 0.08f
        val crateOffset = Offset(tileLeft + pad, tileTop + pad)
        val crateSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

        drawRoundRect(color = Color(0x3300E5FF), topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.12f))
        drawRoundRect(color = teal, topLeft = crateOffset, size = crateSize, cornerRadius = CornerRadius(tileSize * 0.12f), style = Stroke(width = 2f))
        drawRect(cyan, topLeft = Offset(crateOffset.x + crateSize.width * 0.3f, crateOffset.y + crateSize.height * 0.3f), size = Size(crateSize.width * 0.4f, crateSize.height * 0.4f))
    }
}
