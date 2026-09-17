package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.glowink.ui.games.glowblast.model.Enemy
import com.example.glowink.ui.games.glowblast.model.EnemyType
import kotlin.math.abs
import kotlin.math.sin

/**
 * Renderizador Vectorial 2D de Enemigos/Bots de IA para Glow Blast.
 */
fun DrawScope.drawGlowEnemy(
    enemy: Enemy,
    tileSize: Float,
    timeMillis: Long
) {
    if (!enemy.isAlive) return

    val px = enemy.x * tileSize + tileSize / 2f
    val py = enemy.y * tileSize + tileSize / 2f
    val radius = tileSize * 0.38f

    val auraColor = if (enemy.type == EnemyType.DRONE) Color(0xFFFF007F) else Color(0xFFFF2222)
    val pulse = abs(sin(timeMillis / 120f))

    when (enemy.type) {
        EnemyType.DRONE -> {
            // DRON CIBERNÉTICO DESLIZANTE
            // 1. Sombra e Inducción Magnética
            drawOval(
                color = auraColor.copy(alpha = 0.35f),
                topLeft = Offset(px - radius, py + radius * 0.4f),
                size = Size(radius * 2f, radius * 0.5f)
            )

            // 2. Chasis Esférico Dron
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF38084B), Color(0xFF15002A), Color(0xFF070414)),
                    center = Offset(px - radius * 0.3f, py - radius * 0.3f),
                    radius = radius * 1.2f
                ),
                radius = radius,
                center = Offset(px, py)
            )
            drawCircle(color = auraColor, radius = radius, center = Offset(px, py), style = Stroke(width = 2.5f))

            // 3. Lente de Ojo Cíclope Neón
            drawCircle(color = Color.White, radius = radius * 0.38f, center = Offset(px, py))
            drawCircle(color = auraColor, radius = radius * 0.22f, center = Offset(px, py))

            // 4. Helices / Rotores Laterales
            val wingW = radius * 0.5f
            drawRoundRect(color = auraColor, topLeft = Offset(px - radius * 1.4f, py - radius * 0.2f), size = Size(wingW, radius * 0.4f), cornerRadius = CornerRadius(4f))
            drawRoundRect(color = auraColor, topLeft = Offset(px + radius * 0.9f, py - radius * 0.2f), size = Size(wingW, radius * 0.4f), cornerRadius = CornerRadius(4f))
        }

        EnemyType.ANDROID -> {
            // ANDROIDE DE COMBATE BI-PEDESTRE
            // 1. Chasis Torso Pentagonal
            val path = Path().apply {
                moveTo(px, py - radius * 1.1f)
                lineTo(px + radius * 0.8f, py - radius * 0.4f)
                lineTo(px + radius * 0.6f, py + radius * 0.8f)
                lineTo(px - radius * 0.6f, py + radius * 0.8f)
                lineTo(px - radius * 0.8f, py - radius * 0.4f)
                close()
            }
            drawPath(path = path, color = Color(0xFF1E1735))
            drawPath(path = path, color = auraColor, style = Stroke(width = 3f))

            // 2. Visor Láser
            drawRoundRect(
                color = auraColor.copy(alpha = 0.8f + pulse * 0.2f),
                topLeft = Offset(px - radius * 0.5f, py - radius * 0.6f),
                size = Size(radius * 1.0f, radius * 0.3f),
                cornerRadius = CornerRadius(6f)
            )

            // 3. Núcleo Reactivo
            drawCircle(color = Color.White, radius = radius * 0.25f, center = Offset(px, py + radius * 0.2f))
            drawCircle(color = auraColor, radius = radius * 0.15f, center = Offset(px, py + radius * 0.2f))
        }
    }
}
