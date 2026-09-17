package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.glowink.ui.games.glowblast.engine.BlastTile
import kotlin.math.abs
import kotlin.math.sin

/**
 * Renderizador Vectorial 2D de Explosiones y Ondas Expansivas Neón.
 */
fun DrawScope.drawGlowExplosion(
    blastTiles: List<BlastTile>,
    tileSize: Float,
    timeMillis: Long
) {
    val coreColor = Color(0xFFFFFFB0)
    val midColor = Color(0xFFFF6600)
    val outerColor = Color(0xFFFF007F)

    val pulse = abs(sin(timeMillis / 15f))

    for (tile in blastTiles) {
        val tx = tile.gridX * tileSize
        val ty = tile.gridY * tileSize
        val center = Offset(tx + tileSize / 2f, ty + tileSize / 2f)

        // 1. Halo Neón exterior
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(outerColor.copy(alpha = 0.85f), midColor.copy(alpha = 0.5f), Color.Transparent),
                center = center,
                radius = tileSize * (0.65f + pulse * 0.15f)
            ),
            radius = tileSize * (0.65f + pulse * 0.15f),
            center = center
        )

        // 2. Bloque interno de la onda expansiva
        val pad = tileSize * 0.10f
        drawRoundRect(
            color = outerColor,
            topLeft = Offset(tx + pad, ty + pad),
            size = Size(tileSize - pad * 2f, tileSize - pad * 2f),
            cornerRadius = CornerRadius(tileSize * 0.2f),
            style = Stroke(width = 3f)
        )

        // 3. Núcleo brillante encendido
        drawCircle(
            color = coreColor,
            radius = tileSize * (0.28f + pulse * 0.08f),
            center = center
        )

        // 4. Destello en cruz si es casilla central
        if (tile.isCenter) {
            drawCircle(
                color = Color.White,
                radius = tileSize * 0.38f,
                center = center
            )
            drawLine(
                color = Color.White,
                start = Offset(tx + pad, center.y),
                end = Offset(tx + tileSize - pad, center.y),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.White,
                start = Offset(center.x, ty + pad),
                end = Offset(center.x, ty + tileSize - pad),
                strokeWidth = 4f
            )
        }
    }
}
