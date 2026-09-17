package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.glowink.ui.games.glowblast.engine.BlastTile
import com.example.glowink.ui.games.glowblast.model.BossEntity
import com.example.glowink.ui.games.glowblast.model.Enemy
import com.example.glowink.ui.games.glowblast.model.GlowAvatarConfig
import com.example.glowink.ui.games.glowblast.model.GlowBomb
import com.example.glowink.ui.games.glowblast.model.GlowPower
import com.example.glowink.ui.games.glowblast.model.MapTheme

/**
 * Motor de Renderizado 2D de la Arena y Mapa para Glow Blast.
 * Delega en los renderizadores especializados según el mundo/tema de mapa seleccionado.
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
    activePowers: List<GlowPower> = emptyList(),
    enemies: List<Enemy> = emptyList(),
    boss: BossEntity? = null,
    timeMillis: Long = System.currentTimeMillis()
) {
    // 1. Y 2. DIBUJO DE BALDOSAS Y ESTRUCTURAS DELEGADO AL RENDERIZADOR DE MUNDO CORRESPONDIENTE
    for (row in 0 until gridHeight) {
        for (col in 0 until gridWidth) {
            val tileLeft = col * tileSize
            val tileTop = row * tileSize

            val isPerimeter = row == 0 || row == gridHeight - 1 || col == 0 || col == gridWidth - 1
            val isPillar = (row % 2 == 0) && (col % 2 == 0)

            val isCrateInitial = (row + col * 3) % 5 == 1 && !(row <= 2 && col <= 2) && !(row >= gridHeight - 3 && col >= gridWidth - 3)
            val isCrateStanding = isCrateInitial && !destroyedCrates.contains(Pair(col, row))

            when (mapTheme) {
                MapTheme.CIUDAD_NEON -> drawNeonCityTheme(col, row, tileLeft, tileTop, tileSize, isPillar, isPerimeter, isCrateStanding, timeMillis)
                MapTheme.BOSQUE_GLOW -> drawGlowForestTheme(col, row, tileLeft, tileTop, tileSize, isPillar, isPerimeter, isCrateStanding, timeMillis)
                MapTheme.LABORATORIO -> drawCyberLabTheme(col, row, tileLeft, tileTop, tileSize, isPillar, isPerimeter, isCrateStanding, timeMillis)
                MapTheme.ISLA_CELESTIAL -> drawSkyIslandsTheme(col, row, tileLeft, tileTop, tileSize, isPillar, isPerimeter, isCrateStanding, timeMillis)
                MapTheme.RUINAS_CYBER -> drawPrismRuinsTheme(col, row, tileLeft, tileTop, tileSize, isPillar, isPerimeter, isCrateStanding, timeMillis)
            }
        }
    }

    // 3. RENDERIZADO DE POTENCIADORES (POWER-UPS) EN EL SUELO
    activePowers.forEach { power ->
        if (!power.isCollected) {
            drawGlowPowerUp(
                power = power,
                tileSize = tileSize,
                timeMillis = timeMillis
            )
        }
    }

    // 4. RENDERIZADO DE BOMBAS COLOCADAS
    placedBombs.forEach { bomb ->
        drawGlowBomb(
            gridX = bomb.gridX,
            gridY = bomb.gridY,
            tileSize = tileSize,
            timerMs = bomb.timerMs,
            timeMillis = timeMillis
        )
    }

    // 5. RENDERIZADO DE EXPLOSIONES ACTIVAS (RAYOS EN CRUZ)
    if (activeExplosionTiles.isNotEmpty()) {
        drawGlowExplosion(
            blastTiles = activeExplosionTiles,
            tileSize = tileSize,
            timeMillis = timeMillis
        )
    }

    // 6. RENDERIZADO DE ENEMIGOS / BOTS DE IA
    enemies.forEach { enemy ->
        if (enemy.isAlive) {
            drawGlowEnemy(
                enemy = enemy,
                tileSize = tileSize,
                timeMillis = timeMillis
            )
        }
    }

    // 7. RENDERIZADO DEL JEFE CYBERNÉTICO (SI ESTÁ PRESENTE)
    boss?.let { b ->
        if (b.isAlive) {
            drawCyberBoss(
                boss = b,
                tileSize = tileSize,
                timeMillis = timeMillis
            )
        }
    }

    // 8. RENDERIZADO DEL AVATAR DEL JUGADOR
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

    // 9. RENDERIZADO DE PARTÍCULAS DEL SISTEMA OBJECT POOLING
    ParticlePool.updateAndDraw(this, 0.016f)
}
