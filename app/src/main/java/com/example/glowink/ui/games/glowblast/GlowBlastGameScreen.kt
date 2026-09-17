package com.example.glowink.ui.games.glowblast

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.components.GlowSecondaryButton
import com.example.glowink.ui.games.glowblast.engine.BlastCalculator
import com.example.glowink.ui.games.glowblast.engine.BlastTile
import com.example.glowink.ui.games.glowblast.engine.CollisionSystem
import com.example.glowink.ui.games.glowblast.model.GlowAvatarConfig
import com.example.glowink.ui.games.glowblast.model.GlowBomb
import com.example.glowink.ui.games.glowblast.model.MapTheme
import com.example.glowink.ui.games.glowblast.renderer.GlowCharacterPresets
import com.example.glowink.ui.games.glowblast.renderer.drawCyberArena
import com.example.glowink.ui.theme.*
import com.example.glowink.util.GlowHapticManager
import com.example.glowink.util.GlowSoundManager
import kotlinx.coroutines.isActive

/**
 * FASE 8: Pantalla del Campo de Batalla de Glow Blast
 * con Colocación de Bombas Vectoriales, Temporizadores, Fuego en Cruz y Colisión de Daño.
 */
@Composable
fun GlowBlastGameScreen(
    mapTheme: MapTheme = MapTheme.CIUDAD_NEON,
    selectedCharacter: GlowAvatarConfig = GlowCharacterPresets.NEO,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    var zoom by remember { mutableFloatStateOf(1.25f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    // Posición del Jugador en coordenadas de cuadrícula
    var playerX by remember { mutableFloatStateOf(1.0f) }
    var playerY by remember { mutableFloatStateOf(1.0f) }
    var playerLives by remember { mutableIntStateOf(3) }
    var isMoving by remember { mutableStateOf(false) }
    var walkCycle by remember { mutableFloatStateOf(0f) }

    // Dirección activa desde el D-Pad (-1, 0, 1)
    var moveDirX by remember { mutableFloatStateOf(0f) }
    var moveDirY by remember { mutableFloatStateOf(0f) }

    // Estado del Mapa y Entidades
    val placedBombs = remember { mutableStateListOf<GlowBomb>() }
    val destroyedCrates = remember { mutableStateListOf<Pair<Int, Int>>() }
    val activeExplosions = remember { mutableStateListOf<Pair<Long, List<BlastTile>>>() }

    var timeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastDamageTime by remember { mutableLongStateOf(0L) }

    val gridWidth = 15
    val gridHeight = 11

    // Validación de Colisiones de Escenario
    fun isTileBlocked(col: Int, row: Int): Boolean {
        if (col < 0 || col >= gridWidth || row < 0 || row >= gridHeight) return true

        val isPerimeter = row == 0 || row == gridHeight - 1 || col == 0 || col == gridWidth - 1
        val isIndestructiblePillar = (row % 2 == 0) && (col % 2 == 0)
        if (isPerimeter || isIndestructiblePillar) return true

        // Cajas destruibles activas
        val isCrateInitial = (row + col * 3) % 5 == 1 && !(row <= 2 && col <= 2) && !(row >= gridHeight - 3 && col >= gridWidth - 3)
        val isCrateStanding = isCrateInitial && !destroyedCrates.contains(Pair(col, row))
        return isCrateStanding
    }

    fun canMoveTo(targetX: Float, targetY: Float, playerRadius: Float = 0.28f): Boolean {
        val minCol = (targetX - playerRadius).toInt()
        val maxCol = (targetX + playerRadius).toInt()
        val minRow = (targetY - playerRadius).toInt()
        val maxRow = (targetY + playerRadius).toInt()

        for (c in minCol..maxCol) {
            for (r in minRow..maxRow) {
                if (isTileBlocked(c, r)) return false
            }
        }
        return true
    }

    fun placeBomb() {
        if (playerLives <= 0) return
        val bCol = (playerX + 0.5f).toInt().coerceIn(1, gridWidth - 2)
        val bRow = (playerY + 0.5f).toInt().coerceIn(1, gridHeight - 2)

        val alreadyHasBomb = placedBombs.any { it.gridX == bCol && it.gridY == bRow }
        if (!alreadyHasBomb && placedBombs.size < 3) {
            val bomb = GlowBomb(
                id = "bomb_${System.currentTimeMillis()}",
                ownerId = "player1",
                gridX = bCol,
                gridY = bRow,
                range = 2,
                timerMs = 2500L
            )
            placedBombs.add(bomb)
            GlowSoundManager.playGameAction(context)
            GlowHapticManager.vibrateImpact(context)
        }
    }

    // BUCLE PRINCIPAL A 60 FPS (Física, Bombas, Explosiones y Colisiones)
    var lastFrameTime by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        lastFrameTime = 0L
        while (isActive) {
            withFrameMillis { time ->
                if (lastFrameTime == 0L) lastFrameTime = time
                val delta = (time - lastFrameTime) / 1000f
                lastFrameTime = time

                val now = System.currentTimeMillis()
                timeMillis = now

                // 1. Movimiento del Jugador
                if (playerLives > 0) {
                    val speed = 3.6f
                    if (moveDirX != 0f || moveDirY != 0f) {
                        val nextX = playerX + moveDirX * speed * delta
                        val nextY = playerY + moveDirY * speed * delta

                        var moved = false
                        if (canMoveTo(nextX, playerY)) {
                            playerX = nextX
                            moved = true
                        }
                        if (canMoveTo(playerX, nextY)) {
                            playerY = nextY
                            moved = true
                        }

                        isMoving = moved
                        if (moved) walkCycle += delta * 14f
                    } else {
                        isMoving = false
                    }
                } else {
                    isMoving = false
                }

                // 2. Temporizadores de Bombas y Detonación
                val deltaMs = (delta * 1000f).toLong()
                val itBomb = placedBombs.iterator()
                while (itBomb.hasNext()) {
                    val b = itBomb.next()
                    val newTimer = b.timerMs - deltaMs
                    if (newTimer <= 0L) {
                        // DETONACIÓN!
                        itBomb.remove()
                        GlowSoundManager.playGameAction(context)
                        GlowHapticManager.vibrateImpact(context)

                        val tiles = BlastCalculator.calculateBlastTiles(
                            bombX = b.gridX,
                            bombY = b.gridY,
                            range = b.range,
                            gridWidth = gridWidth,
                            gridHeight = gridHeight,
                            destroyedCrates = destroyedCrates.toSet()
                        )

                        tiles.forEach { tile ->
                            val isCrateInitial = (tile.gridY + tile.gridX * 3) % 5 == 1 && !(tile.gridY <= 2 && tile.gridX <= 2) && !(tile.gridY >= gridHeight - 3 && tile.gridX >= gridWidth - 3)
                            if (isCrateInitial) {
                                destroyedCrates.add(Pair(tile.gridX, tile.gridY))
                            }
                        }

                        activeExplosions.add(Pair(now + 600L, tiles))
                    } else {
                        val index = placedBombs.indexOf(b)
                        if (index != -1) {
                            placedBombs[index] = b.copy(timerMs = newTimer)
                        }
                    }
                }

                // 3. Limpieza de Explosiones Expiradas y Reunión de Casillas de Fuego
                val itExp = activeExplosions.iterator()
                val activeTilesList = mutableListOf<BlastTile>()
                while (itExp.hasNext()) {
                    val exp = itExp.next()
                    if (now > exp.first) {
                        itExp.remove()
                    } else {
                        activeTilesList.addAll(exp.second)
                    }
                }

                // 4. Verificación de Daño al Jugador
                if (playerLives > 0 && activeTilesList.isNotEmpty() && now - lastDamageTime > 1200L) {
                    if (CollisionSystem.isPlayerHitByBlast(playerX, playerY, activeTilesList)) {
                        playerLives = (playerLives - 1).coerceAtLeast(0)
                        lastDamageTime = now
                        GlowSoundManager.playVictory(context)
                        GlowHapticManager.vibrateError(context)
                    }
                }
            }
        }
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            com.example.glowink.ui.theme.GlowinkMainBackground()

            // CANVAS 2D DE LA ARENA CON CÁMARA
            BoxWithConstraints(modifier = Modifier.fillMaxSize().clipToBounds()) {
                val canvasW = this.maxWidth.value * 2.5f
                val canvasH = this.maxHeight.value * 2.5f

                val tileSize = minOf(canvasW / gridWidth, canvasH / gridHeight)

                val targetCameraPanX = -(playerX + 0.5f) * tileSize * zoom + (canvasW / 2f)
                val targetCameraPanY = -(playerY + 0.5f) * tileSize * zoom + (canvasH / 2f)

                panX += (targetCameraPanX - panX) * 0.15f
                panY += (targetCameraPanY - panY) * 0.15f

                val activeTilesFlattened = activeExplosions.flatMap { it.second }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                zoom = (zoom * gestureZoom).coerceIn(0.85f, 2.2f)
                            }
                        }
                ) {
                    withTransform({
                        translate(left = panX, top = panY)
                        scale(zoom, zoom, pivot = Offset(0f, 0f))
                    }) {
                        drawCyberArena(
                            mapTheme = mapTheme,
                            gridWidth = gridWidth,
                            gridHeight = gridHeight,
                            tileSize = tileSize,
                            selectedCharacter = selectedCharacter,
                            playerX = playerX,
                            playerY = playerY,
                            isMoving = isMoving,
                            walkCycle = walkCycle,
                            destroyedCrates = destroyedCrates.toSet(),
                            placedBombs = placedBombs,
                            activeExplosionTiles = activeTilesFlattened,
                            timeMillis = timeMillis
                        )
                    }
                }
            }

            // CONTROLES TÁCTILES VIRTUALES (D-PAD + BOTÓN BOMBA DE PANTALLA)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                GlowVirtualDPad(
                    modifier = Modifier.align(Alignment.BottomStart),
                    onDirectionChange = { dx, dy ->
                        moveDirX = dx
                        moveDirY = dy
                    }
                )

                GlowVirtualActionButton(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    onClick = { placeBomb() }
                )
            }

            // CABECERA FLOTANTE CON STATS Y SALIDA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowSecondaryButton(
                    text = "← SALIR",
                    onClick = onBack,
                    borderColor = ElectricCyan,
                    textColor = ElectricCyan,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 12.sp
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .background(Color(0xCC100B2A), RoundedCornerShape(12.dp))
                        .border(1.dp, ElectricCyan, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    val hearts = "❤️".repeat(playerLives).ifBlank { "💀 ELIMINADO" }
                    Text(
                        text = "$hearts | Bombas: ${placedBombs.size}/3",
                        color = if (playerLives > 0) NeonLime else Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // OVERLAY DE GAME OVER (SI PIERDE LAS VIDAS)
            if (playerLives <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💀 ¡JUGADOR ELIMINADO!", color = Color.Red, fontSize = 26.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                playerLives = 3
                                playerX = 1f
                                playerY = 1f
                                placedBombs.clear()
                                activeExplosions.clear()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reintentar Partida", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Control Virtual D-Pad Táctil Glassmorphism.
 */
@Composable
private fun GlowVirtualDPad(
    modifier: Modifier = Modifier,
    onDirectionChange: (dx: Float, dy: Float) -> Unit
) {
    var activeDirX by remember { mutableFloatStateOf(0f) }
    var activeDirY by remember { mutableFloatStateOf(0f) }

    fun updateDirection(dx: Float, dy: Float) {
        activeDirX = dx
        activeDirY = dy
        onDirectionChange(dx, dy)
    }

    Box(
        modifier = modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(Color(0x221E1735))
            .border(1.5.dp, ElectricCyan, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(46.dp)
                .clip(CircleShape)
                .background(if (activeDirY < 0) ElectricCyan else Color(0x33100B2A))
                .clickable { updateDirection(0f, -1f) },
            contentAlignment = Alignment.Center
        ) {
            Text("▲", color = ElectricCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(46.dp)
                .clip(CircleShape)
                .background(if (activeDirY > 0) ElectricCyan else Color(0x33100B2A))
                .clickable { updateDirection(0f, 1f) },
            contentAlignment = Alignment.Center
        ) {
            Text("▼", color = ElectricCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(46.dp)
                .clip(CircleShape)
                .background(if (activeDirX < 0) ElectricCyan else Color(0x33100B2A))
                .clickable { updateDirection(-1f, 0f) },
            contentAlignment = Alignment.Center
        ) {
            Text("◀", color = ElectricCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(46.dp)
                .clip(CircleShape)
                .background(if (activeDirX > 0) ElectricCyan else Color(0x33100B2A))
                .clickable { updateDirection(1f, 0f) },
            contentAlignment = Alignment.Center
        ) {
            Text("▶", color = ElectricCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xAA100B2A))
                .clickable { updateDirection(0f, 0f) },
            contentAlignment = Alignment.Center
        ) {
            Text("⏹", color = Color.White, fontSize = 14.sp)
        }
    }
}

/**
 * Botón Virtual de Colocar Bomba.
 */
@Composable
private fun GlowVirtualActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFFFF007F), Color(0xFF38084B))))
            .border(2.dp, Color.White, CircleShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("💣", fontSize = 34.sp)
    }
}
