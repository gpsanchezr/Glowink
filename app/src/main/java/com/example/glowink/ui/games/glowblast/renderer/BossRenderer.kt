package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.glowink.ui.games.glowblast.model.BossEntity
import com.example.glowink.ui.games.glowblast.model.BossPhase
import com.example.glowink.ui.theme.ElectricCyan
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renderizador Vectorial 2D del Jefe Cybernético Multi-Fase en Canvas.
 */
fun DrawScope.drawCyberBoss(
    boss: BossEntity,
    tileSize: Float,
    timeMillis: Long
) {
    if (!boss.isAlive) return

    val px = boss.x * tileSize + tileSize / 2f
    val py = boss.y * tileSize + tileSize / 2f
    val radius = tileSize * 0.65f

    val phaseColor = when (boss.phase) {
        BossPhase.PHASE_1 -> ElectricCyan
        BossPhase.PHASE_2 -> Color(0xFFFFD24C)
        BossPhase.PHASE_3_CYBER_NOVA -> Color(0xFFFF007F)
    }

    val pulse = abs(sin(timeMillis / 100f))

    // 1. Aura pesada e Inducción de Energía
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(phaseColor.copy(alpha = 0.60f), phaseColor.copy(alpha = 0.15f), Color.Transparent),
            center = Offset(px, py),
            radius = radius * (1.6f + pulse * 0.2f)
        ),
        radius = radius * (1.6f + pulse * 0.2f),
        center = Offset(px, py)
    )

    // Escudo Neón Activo
    if (boss.shieldActive) {
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.8f),
            radius = radius * 1.35f,
            center = Offset(px, py),
            style = Stroke(width = 4f)
        )
    }

    // 2. Chasis Imponente de Armadura Mech
    val armorPath = Path().apply {
        moveTo(px, py - radius * 1.1f)
        lineTo(px + radius * 0.9f, py - radius * 0.5f)
        lineTo(px + radius * 1.1f, py + radius * 0.3f)
        lineTo(px + radius * 0.6f, py + radius * 1.0f)
        lineTo(px - radius * 0.6f, py + radius * 1.0f)
        lineTo(px - radius * 1.1f, py + radius * 0.3f)
        lineTo(px - radius * 0.9f, py - radius * 0.5f)
        close()
    }

    drawPath(path = armorPath, color = Color(0xFF100B26))
    drawPath(path = armorPath, color = phaseColor, style = Stroke(width = 3.5f))

    // 3. Múltiples Núcleos de Reactor Brillantes
    drawCircle(color = Color.White, radius = radius * 0.35f, center = Offset(px, py))
    drawCircle(color = phaseColor, radius = radius * 0.22f, center = Offset(px, py))

    drawCircle(color = phaseColor, radius = radius * 0.18f, center = Offset(px - radius * 0.6f, py - radius * 0.3f))
    drawCircle(color = phaseColor, radius = radius * 0.18f, center = Offset(px + radius * 0.6f, py - radius * 0.3f))

    drawRoundRect(
        color = phaseColor,
        topLeft = Offset(px - radius * 0.45f, py - radius * 0.7f),
        size = Size(radius * 0.9f, radius * 0.22f),
        cornerRadius = CornerRadius(6f)
    )

    // 4. Ataque Especial "Cyber Nova" (Ráfagas Radiales)
    if (boss.isNovaActive) {
        for (angleDeg in 0 until 360 step 45) {
            val rad = Math.toRadians(angleDeg.toDouble())
            val endX = px + cos(rad).toFloat() * tileSize * 3.5f
            val endY = py + sin(rad).toFloat() * tileSize * 3.5f

            drawLine(
                color = Color(0xFFFF007F).copy(alpha = 0.85f),
                start = Offset(px, py),
                end = Offset(endX, endY),
                strokeWidth = 5f
            )
        }
    }
}
