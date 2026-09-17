package com.example.glowink.ui.games.glowblast

import android.widget.Toast
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
import com.example.glowink.ui.components.GlowCard
import com.example.glowink.ui.components.GlowSecondaryButton
import com.example.glowink.ui.games.glowblast.engine.AIController
import com.example.glowink.ui.games.glowblast.engine.BlastCalculator
import com.example.glowink.ui.games.glowblast.engine.BlastTile
import com.example.glowink.ui.games.glowblast.engine.BossController
import com.example.glowink.ui.games.glowblast.engine.CollisionSystem
import com.example.glowink.ui.games.glowblast.engine.GlowBlastHapticManager
import com.example.glowink.ui.games.glowblast.engine.GlowBlastSoundManager
import com.example.glowink.ui.games.glowblast.model.BossEntity
import com.example.glowink.ui.games.glowblast.model.BossPhase
import com.example.glowink.ui.games.glowblast.model.Enemy
import com.example.glowink.ui.games.glowblast.model.EnemyType
import com.example.glowink.ui.games.glowblast.model.GlowAvatarConfig
import com.example.glowink.ui.games.glowblast.model.GlowBomb
import com.example.glowink.ui.games.glowblast.model.GlowPower
import com.example.glowink.ui.games.glowblast.model.MapTheme
import com.example.glowink.ui.games.glowblast.model.PowerType
import com.example.glowink.ui.games.glowblast.renderer.ParticlePool
import com.example.glowink.ui.games.glowblast.renderer.GlowCharacterPresets
import com.example.glowink.ui.games.glowblast.renderer.GlowTacticalMiniMap
import com.example.glowink.ui.games.glowblast.renderer.drawCyberArena
import com.example.glowink.ui.theme.*
import com.example.glowink.util.GlowHapticManager
import com.example.glowink.util.GlowSoundManager
import kotlinx.coroutines.isActive

/**
 * FASE 13: Pantalla del Campo de Batalla de Glow Blast
 * con HUD Profesional Cyberpunk Glassmorphism y Minimapa / Radar Táctico en Tiempo Real.
 */
@Composable
fun GlowBlastGameScreen(
    mapTheme: MapTheme = MapTheme.CIUDAD_NEON,
    selectedCharacter: GlowAvatarConfig = GlowCharacterPresets.NEO,
    onBack: () -> Unit = {},
    onMatchOver: (isVictory: Boolean, score: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    var zoom by remember { mutableFloatStateOf(1.25f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    // Estadísticas Escalables del Jugador
    var playerX by remember { mutableFloatStateOf(1.0f) }
    var playerY by remember { mutableFloatStateOf(1.0f) }
    var playerLives by remember { mutableIntStateOf(3) }
    var maxBombs by remember { mutableIntStateOf(1) }
    var explosionRange by remember { mutableIntStateOf(2) }
    var playerSpeed by remember { mutableFloatStateOf(3.6f) }
    var hasShield by remember { mutableStateOf(false) }

    var isMoving by remember { mutableStateOf(false) }
    var walkCycle by remember { mutableFloatStateOf(0f) }

    // Dirección activa desde el D-Pad (-1, 0, 1)
    var moveDirX by remember { mutableFloatStateOf(0f) }
    var moveDirY by remember { mutableFloatStateOf(0f) }

    // Estado del Mapa y Entidades
    val placedBombs = remember { mutableStateListOf<GlowBomb>() }
    val destroyedCrates = remember { mutableStateListOf<Pair<Int, Int>>() }
    val activeExplosions = remember { mutableStateListOf<Pair<Long, List<BlastTile>>>() }
    val activePowers = remember { mutableStateListOf<GlowPower>() }

    // Enemigos Bots de IA
    val activeEnemies = remember {
        mutableStateListOf(
            Enemy("bot1", "Dron Alpha", EnemyType.DRONE, x = 13.0f, y = 1.0f, speed = 2.6f),
            Enemy("bot2", "Androide Beta", EnemyType.ANDROID, x = 1.0f, y = 9.0f, speed = 2.4f),
            Enemy("bot3", "Dron Gamma", EnemyType.DRONE, x = 13.0f, y = 9.0f, speed = 2.8f)
        )
    }

    // Entidad del Jefe Cybernético
    val cyberBoss = remember { mutableStateOf(BossEntity(hp = 100, maxHp = 100, x = 7.0f, y = 5.0f)) }
    var showVictoryRewardDialog by remember { mutableStateOf(false) }
    var lastBossDamageTime by remember { mutableLongStateOf(0L) }

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
        if (!alreadyHasBomb && placedBombs.size < maxBombs) {
            val bomb = GlowBomb(
                id = "bomb_${System.currentTimeMillis()}",
                ownerId = "player1",
                gridX = bCol,
                gridY = bRow,
                range = explosionRange,
                timerMs = 2500L
            )
            placedBombs.add(bomb)
            GlowSoundManager.playGameAction(context)
            GlowHapticManager.vibrateImpact(context)
        }
    }

    // BUCLE PRINCIPAL A 60 FPS
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
                val deltaMs = (delta * 1000f).toLong()

                // 1. Movimiento del Jugador
                if (playerLives > 0) {
                    if (moveDirX != 0f || moveDirY != 0f) {
                        val nextX = playerX + moveDirX * playerSpeed * delta
                        val nextY = playerY + moveDirY * playerSpeed * delta

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

                // 2. Temporizadores de Bombas, Detonación y Generación de Power-Ups
                val itBomb = placedBombs.iterator()
                while (itBomb.hasNext()) {
                    val b = itBomb.next()
                    val newTimer = b.timerMs - deltaMs
                    if (newTimer <= 0L) {
                        itBomb.remove()
                        GlowBlastSoundManager.playExplosion(context)
                        GlowBlastHapticManager.vibrateExplosion(context)
                        ParticlePool.spawnExplosionBurst(b.gridX * 40f + 20f, b.gridY * 40f + 20f, Color(0xFFFF007F))

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
                            if (isCrateInitial && !destroyedCrates.contains(Pair(tile.gridX, tile.gridY))) {
                                destroyedCrates.add(Pair(tile.gridX, tile.gridY))

                                if (kotlin.random.Random.nextFloat() < 0.60f) {
                                    val availableTypes = listOf(
                                        PowerType.EXPANSION_FUEGO,
                                        PowerType.BOMBA_EXTRA,
                                        PowerType.VELOCIDAD_SUPER,
                                        PowerType.ESCUDO_NEON
                                    )
                                    val power = GlowPower(
                                        id = "power_${tile.gridX}_${tile.gridY}_$now",
                                        type = availableTypes.random(),
                                        gridX = tile.gridX,
                                        gridY = tile.gridY
                                    )
                                    activePowers.add(power)
                                }
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

                // 3. Recogida de Power-Ups
                val itPower = activePowers.iterator()
                while (itPower.hasNext()) {
                    val power = itPower.next()
                    if (!power.isCollected) {
                        val distDx = kotlin.math.abs(playerX - power.gridX)
                        val distDy = kotlin.math.abs(playerY - power.gridY)
                        if (distDx < 0.45f && distDy < 0.45f) {
                            itPower.remove()
                            GlowSoundManager.playGameAction(context)
                            GlowHapticManager.vibrateImpact(context)

                            when (power.type) {
                                PowerType.EXPANSION_FUEGO -> {
                                    explosionRange += 1
                                    Toast.makeText(context, "⚡ ¡Rango Fuego +1! ($explosionRange)", Toast.LENGTH_SHORT).show()
                                }
                                PowerType.BOMBA_EXTRA -> {
                                    maxBombs += 1
                                    Toast.makeText(context, "💣 ¡Bomba Extra! ($maxBombs)", Toast.LENGTH_SHORT).show()
                                }
                                PowerType.VELOCIDAD_SUPER -> {
                                    playerSpeed += 0.6f
                                    Toast.makeText(context, "👟 ¡Súper Velocidad! (${"%.1f".format(playerSpeed)})", Toast.LENGTH_SHORT).show()
                                }
                                PowerType.ESCUDO_NEON -> {
                                    hasShield = true
                                    Toast.makeText(context, "🛡️ ¡Escudo Neón Activo!", Toast.LENGTH_SHORT).show()
                                }
                                else -> Unit
                            }
                        }
                    }
                }

                // 4. Limpieza de Explosiones Expiradas
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

                // 5. Actualización de Inteligencia Artificial de Enemigos
                activeEnemies.forEach { enemy ->
                    if (enemy.isAlive) {
                        if (enemy.bombCooldownMs > 0L) {
                            enemy.bombCooldownMs = (enemy.bombCooldownMs - deltaMs).coerceAtLeast(0L)
                        }

                        AIController.updateEnemyAI(
                            enemy = enemy,
                            playerX = playerX,
                            playerY = playerY,
                            placedBombs = placedBombs,
                            activeExplosions = activeTilesList,
                            gridWidth = gridWidth,
                            gridHeight = gridHeight,
                            isTileBlockedFunc = { c, r -> isTileBlocked(c, r) },
                            onPlaceBomb = { col, row ->
                                val alreadyHas = placedBombs.any { it.gridX == col && it.gridY == row }
                                if (!alreadyHas) {
                                    placedBombs.add(
                                        GlowBomb(
                                            id = "ai_bomb_${System.currentTimeMillis()}_${enemy.id}",
                                            ownerId = enemy.id,
                                            gridX = col,
                                            gridY = row,
                                            range = enemy.explosionRange,
                                            timerMs = 2500L
                                        )
                                    )
                                }
                            }
                        )

                        val nextEx = enemy.x + enemy.moveDirX * enemy.speed * delta
                        val nextEy = enemy.y + enemy.moveDirY * enemy.speed * delta
                        if (!isTileBlocked((nextEx + 0.4f * enemy.moveDirX).toInt(), (enemy.y + 0.4f).toInt())) {
                            enemy.x = nextEx.coerceIn(1.0f, (gridWidth - 2).toFloat())
                        }
                        if (!isTileBlocked((enemy.x + 0.4f).toInt(), (nextEy + 0.4f * enemy.moveDirY).toInt())) {
                            enemy.y = nextEy.coerceIn(1.0f, (gridHeight - 2).toFloat())
                        }

                        if (activeTilesList.isNotEmpty() && CollisionSystem.isPlayerHitByBlast(enemy.x, enemy.y, activeTilesList)) {
                            enemy.lives -= 1
                            if (enemy.lives <= 0) {
                                enemy.isAlive = false
                                Toast.makeText(context, "💥 ¡${enemy.name} eliminado!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // 6. Actualización del Jefe Cybernético
                val boss = cyberBoss.value
                if (boss.isAlive) {
                    BossController.updateBoss(
                        boss = boss,
                        playerX = playerX,
                        playerY = playerY,
                        delta = delta,
                        timeMillis = timeMillis,
                        gridWidth = gridWidth,
                        gridHeight = gridHeight,
                        isTileBlockedFunc = { c, r -> isTileBlocked(c, r) },
                        onPlaceBomb = { col, row, range ->
                            val alreadyHas = placedBombs.any { it.gridX == col && it.gridY == row }
                            if (!alreadyHas) {
                                placedBombs.add(
                                    GlowBomb(
                                        id = "boss_bomb_${System.currentTimeMillis()}",
                                        ownerId = boss.id,
                                        gridX = col,
                                        gridY = row,
                                        range = range,
                                        timerMs = 2000L
                                    )
                                )
                            }
                        }
                    )

                    if (activeTilesList.isNotEmpty() && now - lastBossDamageTime > 500L) {
                        if (CollisionSystem.isPlayerHitByBlast(boss.x, boss.y, activeTilesList)) {
                            if (boss.shieldActive) {
                                Toast.makeText(context, "🛡️ ¡El Escudo del Jefe absorbió el daño!", Toast.LENGTH_SHORT).show()
                            } else {
                                boss.hp = (boss.hp - 15).coerceAtLeast(0)
                                lastBossDamageTime = now
                                GlowSoundManager.playGameAction(context)
                                GlowHapticManager.vibrateImpact(context)

                                if (boss.hp <= 0) {
                                    boss.isAlive = false
                                    showVictoryRewardDialog = true
                                    GlowSoundManager.playVictory(context)
                                }
                            }
                        }
                    }
                }

                // 7. Verificación de Daño al Jugador
                if (playerLives > 0 && activeTilesList.isNotEmpty() && now - lastDamageTime > 1200L) {
                    if (CollisionSystem.isPlayerHitByBlast(playerX, playerY, activeTilesList)) {
                        if (hasShield) {
                            hasShield = false
                            lastDamageTime = now
                            GlowSoundManager.playGameAction(context)
                            GlowHapticManager.vibrateImpact(context)
                            Toast.makeText(context, "🛡️ ¡El Escudo absorbió la explosión!", Toast.LENGTH_SHORT).show()
                        } else {
                            playerLives = (playerLives - 1).coerceAtLeast(0)
                            lastDamageTime = now
                            GlowSoundManager.playVictory(context)
                            GlowHapticManager.vibrateError(context)
                        }
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
                            activePowers = activePowers,
                            enemies = activeEnemies,
                            boss = if (cyberBoss.value.isAlive) cyberBoss.value else null,
                            timeMillis = timeMillis
                        )
                    }
                }
            }

            // CONTROLES TÁCTILES VIRTUALES
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

            // BARRA DE VIDA DESTACADA DEL JEFE CYBER (SUPERIOR CENTRAL)
            val boss = cyberBoss.value
            if (boss.isAlive) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .width(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xDD0B071E))
                        .border(1.5.dp, if (boss.phase == BossPhase.PHASE_3_CYBER_NOVA) Color(0xFFFF007F) else ElectricCyan, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = boss.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = when (boss.phase) {
                                BossPhase.PHASE_1 -> "FASE 1"
                                BossPhase.PHASE_2 -> "FASE 2 ⚡"
                                BossPhase.PHASE_3_CYBER_NOVA -> "FASE 3 CYBER NOVA 💥"
                            },
                            color = if (boss.phase == BossPhase.PHASE_3_CYBER_NOVA) Color(0xFFFF007F) else NeonLime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF20123A))
                    ) {
                        val barColor = when (boss.phase) {
                            BossPhase.PHASE_1 -> ElectricCyan
                            BossPhase.PHASE_2 -> Color(0xFFFFD24C)
                            BossPhase.PHASE_3_CYBER_NOVA -> Color(0xFFFF007F)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(boss.hp.toFloat() / boss.maxHp.toFloat())
                                .background(barColor)
                        )
                    }
                }
            }

            // CABECERA CUBERPUINK FLOTANTE (SUPERIOR)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                GlowSecondaryButton(
                    text = "← SALIR",
                    onClick = onBack,
                    borderColor = ElectricCyan,
                    textColor = ElectricCyan,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp
                )

                // CRONÓMETRO Y VIDAS CENTRAL
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xDD0C0824))
                        .border(1.5.dp, ElectricCyan, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    val hearts = "❤️".repeat(playerLives).ifBlank { "💀 ELIMINADO" }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "02:45",
                            color = ElectricCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = hearts,
                            color = NeonLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // RADAR TÁCTICO / MINIMAPA EN TIEMPO REAL
                GlowTacticalMiniMap(
                    playerX = playerX,
                    playerY = playerY,
                    enemies = activeEnemies,
                    boss = if (cyberBoss.value.isAlive) cyberBoss.value else null,
                    placedBombs = placedBombs,
                    destroyedCrates = destroyedCrates.toSet(),
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    timeMillis = timeMillis
                )
            }

            // PANEL INFERIOR FLOTANTE DE INVENTARIO DE PODERES (GLASSMORPHISM)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xEE120B30))
                    .border(1.5.dp, Brush.horizontalGradient(listOf(ElectricCyan, Color(0xFFFF007F))), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡ Rango: $explosionRange", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text("💣 Capacidad: ${placedBombs.size}/$maxBombs", color = ElectricCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text("👟 Velocidad: ${"%.1f".format(playerSpeed)}", color = NeonLime, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text("🛡️ Escudo: ${if (hasShield) "ACTIVO" else "OFF"}", color = if (hasShield) NeonLime else Color.Gray, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            // DIÁLOGO DE VICTORIA Y RECOMPENSA DE RECOMPENSA AL DERROTAR AL JEFE
            if (showVictoryRewardDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .width(280.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF140D2A))
                            .border(2.dp, NeonLime, RoundedCornerShape(22.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆 ¡JEFE DERROTADO!", color = NeonLime, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("👑 RECOMPENSA ESPECIAL DE RECOMPENSA", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("🪙 +500 GlowCoins", color = NeonLime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("💎 +100 Gemas", color = ElectricCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = {
                                showVictoryRewardDialog = false
                                onMatchOver(true, 1500)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reclamar y Continuar", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // OVERLAY DE GAME OVER
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
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    playerLives = 3
                                    maxBombs = 1
                                    explosionRange = 2
                                    playerSpeed = 3.6f
                                    hasShield = false
                                    playerX = 1f
                                    playerY = 1f
                                    placedBombs.clear()
                                    activeExplosions.clear()
                                    activePowers.clear()
                                    cyberBoss.value = BossEntity(hp = 100, maxHp = 100, x = 7.0f, y = 5.0f)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reintentar", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    onMatchOver(false, 350)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Ver Resultados", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
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
