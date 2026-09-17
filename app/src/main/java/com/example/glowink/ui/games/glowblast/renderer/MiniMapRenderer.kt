package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.games.glowblast.model.BossEntity
import com.example.glowink.ui.games.glowblast.model.Enemy
import com.example.glowink.ui.games.glowblast.model.GlowBomb
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.NeonLime
import kotlin.math.cos
import kotlin.math.sin

/**
 * Composable que renderiza el Radar Táctico / Minimapa Flotante de Glow Blast.
 */
@Composable
fun GlowTacticalMiniMap(
    playerX: Float,
    playerY: Float,
    enemies: List<Enemy>,
    boss: BossEntity?,
    placedBombs: List<GlowBomb>,
    destroyedCrates: Set<Pair<Int, Int>>,
    gridWidth: Int = 15,
    gridHeight: Int = 11,
    timeMillis: Long = System.currentTimeMillis(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xDD070414))
            .border(1.5.dp, ElectricCyan, RoundedCornerShape(16.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RADAR TÁCTICO",
            color = ElectricCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Canvas(
            modifier = Modifier
                .width(110.dp)
                .height(80.dp)
        ) {
            drawTacticalMiniMap(
                playerX = playerX,
                playerY = playerY,
                enemies = enemies,
                boss = boss,
                placedBombs = placedBombs,
                destroyedCrates = destroyedCrates,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                timeMillis = timeMillis
            )
        }
    }
}

/**
 * Renderizador de Minimapa y Radar Táctico en Tiempo Real para Glow Blast (100% Canvas).
 */
fun DrawScope.drawTacticalMiniMap(
    playerX: Float,
    playerY: Float,
    enemies: List<Enemy>,
    boss: BossEntity?,
    placedBombs: List<GlowBomb>,
    destroyedCrates: Set<Pair<Int, Int>>,
    gridWidth: Int = 15,
    gridHeight: Int = 11,
    timeMillis: Long = System.currentTimeMillis()
) {
    val mapW = size.width
    val mapH = size.height
    val tileW = mapW / gridWidth
    val tileH = mapH / gridHeight

    // 1. Fondo Oscuro Glassmorphic del Radar
    drawRoundRect(
        color = Color(0xDD070414),
        topLeft = Offset(0f, 0f),
        size = Size(mapW, mapH),
        cornerRadius = CornerRadius(12f)
    )

    // Marco con Borde Neón
    drawRoundRect(
        color = ElectricCyan,
        topLeft = Offset(0f, 0f),
        size = Size(mapW, mapH),
        cornerRadius = CornerRadius(12f),
        style = Stroke(width = 2f)
    )

    // 2. Líneas de Cuadrícula Sutiles
    for (r in 0 until gridHeight step 2) {
        drawLine(
            color = ElectricCyan.copy(alpha = 0.15f),
            start = Offset(0f, r * tileH),
            end = Offset(mapW, r * tileH),
            strokeWidth = 1f
        )
    }
    for (c in 0 until gridWidth step 2) {
        drawLine(
            color = ElectricCyan.copy(alpha = 0.15f),
            start = Offset(c * tileW, 0f),
            end = Offset(c * tileW, mapH),
            strokeWidth = 1f
        )
    }

    // 3. Renderizado de Estructuras (Muros y Cajas) en el Radar
    for (r in 0 until gridHeight) {
        for (c in 0 until gridWidth) {
            val isPerimeter = r == 0 || r == gridHeight - 1 || c == 0 || c == gridWidth - 1
            val isPillar = (r % 2 == 0) && (c % 2 == 0)

            if (isPerimeter || isPillar) {
                drawRect(
                    color = Color(0xFF2E1C55),
                    topLeft = Offset(c * tileW + 0.5f, r * tileH + 0.5f),
                    size = Size(tileW - 1f, tileH - 1f)
                )
            } else {
                val isCrateInitial = (r + c * 3) % 5 == 1 && !(r <= 2 && c <= 2) && !(r >= gridHeight - 3 && c >= gridWidth - 3)
                if (isCrateInitial && !destroyedCrates.contains(Pair(c, r))) {
                    drawRect(
                        color = Color(0x669D4EDD),
                        topLeft = Offset(c * tileW + 0.5f, r * tileH + 0.5f),
                        size = Size(tileW - 1f, tileH - 1f)
                    )
                }
            }
        }
    }

    // 4. Barrido de Radar Procedural (Línea Giratoria de Escaneo)
    val sweepAngle = (timeMillis / 12f) % 360f
    val sweepRad = Math.toRadians(sweepAngle.toDouble())
    val centerX = mapW / 2f
    val centerY = mapH / 2f
    val scanX = centerX + cos(sweepRad).toFloat() * (mapW * 0.7f)
    val scanY = centerY + sin(sweepRad).toFloat() * (mapH * 0.7f)

    drawLine(
        brush = Brush.linearGradient(listOf(NeonLime.copy(alpha = 0.6f), Color.Transparent)),
        start = Offset(centerX, centerY),
        end = Offset(scanX, scanY),
        strokeWidth = 1.5f
    )

    // 5. Bombas Activas (Puntos Amarillos)
    placedBombs.forEach { bomb ->
        val bx = (bomb.gridX + 0.5f) * tileW
        val by = (bomb.gridY + 0.5f) * tileH
        drawCircle(color = Color(0xFFFFD24C), radius = tileW * 0.75f, center = Offset(bx, by))
    }

    // 6. Enemigos Bots de IA (Puntos Rojos)
    enemies.forEach { enemy ->
        if (enemy.isAlive) {
            val ex = (enemy.x + 0.5f) * tileW
            val ey = (enemy.y + 0.5f) * tileH
            drawCircle(color = Color(0xFFFF007F), radius = tileW * 0.85f, center = Offset(ex, ey))
        }
    }

    // 7. Jefe Cyber (Punto Violeta/Magenta Grande)
    boss?.let { b ->
        if (b.isAlive) {
            val bx = (b.x + 0.5f) * tileW
            val by = (b.y + 0.5f) * tileH
            drawCircle(color = Color(0xFF9D4EDD), radius = tileW * 1.4f, center = Offset(bx, by))
        }
    }

    // 8. Posición del Jugador (Punto Cian Brillante Concéntrico)
    val px = (playerX + 0.5f) * tileW
    val py = (playerY + 0.5f) * tileH

    drawCircle(color = Color.White, radius = tileW * 1.10f, center = Offset(px, py))
    drawCircle(color = ElectricCyan, radius = tileW * 0.75f, center = Offset(px, py))
}
