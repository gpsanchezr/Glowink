package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.glowink.ui.games.glowblast.engine.BlastTile
import com.example.glowink.ui.games.glowblast.model.GlowAvatarConfig
import com.example.glowink.ui.games.glowblast.model.GlowBomb
import com.example.glowink.ui.games.glowblast.model.MapTheme
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.NeonLime

/**
 * Motor de Renderizado 2D de la Arena y Mapa para Glow Blast.
 */
fun DrawScope.drawCyberArena(
    mapTheme: MapTheme,
    gridWidth: Int = 15,
    gridHeight: Int = 11,
    tileSize: Float,
    selectedCharacter: GlowAvatarConfig,
    playerX: Float = 1.0f,
    playerY: Float = 1.0f,
    isMoving: Boolean = false,
    walkCycle: Float = 0f,
    destroyedCrates: Set<Pair<Int, Int>> = emptySet(),
    placedBombs: List<GlowBomb> = emptyList(),
    activeExplosionTiles: List<BlastTile> = emptyList(),
    timeMillis: Long = System.currentTimeMillis()
) {
    val mapGradients = when (mapTheme) {
        MapTheme.CIUDAD_NEON -> listOf(ElectricCyan, Color(0xFF9D4EDD))
        MapTheme.BOSQUE_GLOW -> listOf(NeonLime, Color(0xFF00FF77))
        MapTheme.LABORATORIO -> listOf(ElectricCyan, Color(0xFF0077FF))
        MapTheme.ISLA_CELESTIAL -> listOf(Color(0xFFFF007F), Color(0xFF9D4EDD))
        MapTheme.RUINAS_CYBER -> listOf(Color(0xFFFF2222), Color(0xFFFFD700))
    }

    val primaryThemeColor = mapGradients.first()
    val secondaryThemeColor = mapGradients.last()

    // 1. DIBUJO DE BALDOSAS DEL SUELO (GRID PROCEDURAL)
    for (row in 0 until gridHeight) {
        for (col in 0 until gridWidth) {
            val tileLeft = col * tileSize
            val tileTop = row * tileSize
            val tileOffset = Offset(tileLeft, tileTop)
            val tileSizeObj = Size(tileSize, tileSize)

            // Fondo Oscuro de Baldosa
            drawRect(
                color = Color(0xFF0B071E),
                topLeft = tileOffset,
                size = tileSizeObj
            )

            // Bordes Iluminados por Gradiente Neón
            drawRect(
                brush = Brush.linearGradient(mapGradients, start = tileOffset, end = Offset(tileLeft + tileSize, tileTop + tileSize)),
                topLeft = tileOffset,
                size = tileSizeObj,
                style = Stroke(width = 1.5f)
            )

            // Ranura / Panel Tecnológico Interior
            if ((row + col) % 2 == 0) {
                drawRect(
                    color = primaryThemeColor.copy(alpha = 0.08f),
                    topLeft = Offset(tileLeft + tileSize * 0.15f, tileTop + tileSize * 0.15f),
                    size = Size(tileSize * 0.70f, tileSize * 0.70f)
                )
            }
        }
    }

    // 2. DIBUJO DE ESTRUCTURAS (MUROS INDESTRUCTIBLES Y CAJAS DESTRUCTIBLES)
    for (row in 0 until gridHeight) {
        for (col in 0 until gridWidth) {
            val tileLeft = col * tileSize
            val tileTop = row * tileSize

            val isPerimeter = row == 0 || row == gridHeight - 1 || col == 0 || col == gridWidth - 1
            val isIndestructiblePillar = (row % 2 == 0) && (col % 2 == 0)

            if (isPerimeter || isIndestructiblePillar) {
                // MURO INDESTRUCTIBLE (Bloque de Acero Cyber)
                val pad = tileSize * 0.05f
                val wallOffset = Offset(tileLeft + pad, tileTop + pad)
                val wallSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

                drawRoundRect(
                    color = Color(0xFF140D2A),
                    topLeft = wallOffset,
                    size = wallSize,
                    cornerRadius = CornerRadius(tileSize * 0.15f)
                )
                drawRoundRect(
                    color = Color(0xFF2A1C52),
                    topLeft = Offset(wallOffset.x + tileSize * 0.1f, wallOffset.y + tileSize * 0.1f),
                    size = Size(wallSize.width * 0.8f, wallSize.height * 0.8f),
                    cornerRadius = CornerRadius(tileSize * 0.10f)
                )
                drawRoundRect(
                    color = primaryThemeColor,
                    topLeft = wallOffset,
                    size = wallSize,
                    cornerRadius = CornerRadius(tileSize * 0.15f),
                    style = Stroke(width = 2.5f)
                )

                // Remaches Metálicos en Esquinas
                val rivetR = tileSize * 0.04f
                drawCircle(primaryThemeColor, radius = rivetR, center = Offset(wallOffset.x + pad * 2f, wallOffset.y + pad * 2f))
                drawCircle(primaryThemeColor, radius = rivetR, center = Offset(wallOffset.x + wallSize.width - pad * 2f, wallOffset.y + pad * 2f))
                drawCircle(primaryThemeColor, radius = rivetR, center = Offset(wallOffset.x + pad * 2f, wallOffset.y + wallSize.height - pad * 2f))
                drawCircle(primaryThemeColor, radius = rivetR, center = Offset(wallOffset.x + wallSize.width - pad * 2f, wallOffset.y + wallSize.height - pad * 2f))

            } else {
                val isCrateInitial = (row + col * 3) % 5 == 1 && !(row <= 2 && col <= 2) && !(row >= gridHeight - 3 && col >= gridWidth - 3)
                val isCrateStanding = isCrateInitial && !destroyedCrates.contains(Pair(col, row))

                if (isCrateStanding) {
                    // CAJA DESTRUCTIBLE (Caja de Cristal Neón con 'X' Interior)
                    val pad = tileSize * 0.08f
                    val crateOffset = Offset(tileLeft + pad, tileTop + pad)
                    val crateSize = Size(tileSize - pad * 2f, tileSize - pad * 2f)

                    drawRoundRect(
                        color = secondaryThemeColor.copy(alpha = 0.25f),
                        topLeft = crateOffset,
                        size = crateSize,
                        cornerRadius = CornerRadius(tileSize * 0.12f)
                    )
                    drawRoundRect(
                        color = secondaryThemeColor,
                        topLeft = crateOffset,
                        size = crateSize,
                        cornerRadius = CornerRadius(tileSize * 0.12f),
                        style = Stroke(width = 2f)
                    )

                    // Patrón 'X' Neón Interior
                    drawLine(
                        color = secondaryThemeColor.copy(alpha = 0.8f),
                        start = Offset(crateOffset.x + pad, crateOffset.y + pad),
                        end = Offset(crateOffset.x + crateSize.width - pad, crateOffset.y + crateSize.height - pad),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = secondaryThemeColor.copy(alpha = 0.8f),
                        start = Offset(crateOffset.x + crateSize.width - pad, crateOffset.y + pad),
                        end = Offset(crateOffset.x + pad, crateOffset.y + crateSize.height - pad),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }

    // 3. RENDERIZADO DE BOMBAS COLOCADAS
    placedBombs.forEach { bomb ->
        drawGlowBomb(
            gridX = bomb.gridX,
            gridY = bomb.gridY,
            tileSize = tileSize,
            timerMs = bomb.timerMs,
            timeMillis = timeMillis
        )
    }

    // 4. RENDERIZADO DE EXPLOSIONES ACTIVAS (RAYOS EN CRUZ)
    if (activeExplosionTiles.isNotEmpty()) {
        drawGlowExplosion(
            blastTiles = activeExplosionTiles,
            tileSize = tileSize,
            timeMillis = timeMillis
        )
    }

    // 5. RENDERIZADO DEL AVATAR DEL JUGADOR
    val px = playerX * tileSize
    val py = playerY * tileSize

    val playerSize = tileSize * 0.95f
    val playerOffset = Offset(px + (tileSize - playerSize) / 2f, py + (tileSize - playerSize) / 2f)

    with(this) {
        drawContext.canvas.save()
        drawContext.transform.translate(playerOffset.x, playerOffset.y)

        val scaleX = playerSize / size.width
        val scaleY = playerSize / size.height
        drawContext.transform.scale(scaleX, scaleY)

        drawGlowCharacter(
            config = selectedCharacter,
            isMoving = isMoving,
            walkCycle = walkCycle
        )

        drawContext.canvas.restore()
    }
}
