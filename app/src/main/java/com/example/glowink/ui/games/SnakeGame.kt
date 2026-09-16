package com.example.glowink.ui.games

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.*
import com.example.glowink.util.GlowSoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

private const val COLS = 13
private const val ROWS = 19

private enum class Dir(val dx: Int, val dy: Int) { UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0) }
private data class Cell(val x: Int, val y: Int)

data class SnakeWorld(
    val id: Int,
    val name: String,
    val icon: String,
    val bossName: String,
    val bossIcon: String,
    val accentColor: Color,
    val gemType: String,
    val bgGradient: List<Color>
)

private val SNAKE_WORLDS = listOf(
    SnakeWorld(1, "Jardines Neón", "🌱", "Rey Serpiente 👑", "🐍", NeonLime, "Gema Verde 💎", listOf(Color(0xFF0F2B07), Color(0xFF0A071A))),
    SnakeWorld(2, "Ciudad Cyberpunk", "🌃", "Cyber Titan 🤖", "🤖", ElectricCyan, "Gema Azul 💎", listOf(Color(0xFF002F4B), Color(0xFF0A071A))),
    SnakeWorld(3, "Volcanic Core", "🌋", "Señor del Volcán 🔥", "🔥", Color(0xFFFF4444), "Gema Roja 💎", listOf(Color(0xFF4A0E0E), Color(0xFF0A071A))),
    SnakeWorld(4, "Cosmic Zone", "🌌", "Guardián Cósmico 👾", "👾", UltravioletPurpleLight, "Gema Morada 💎", listOf(Color(0xFF28133D), Color(0xFF0A071A))),
    SnakeWorld(5, "Kingdom of Glow", "👑", "Rey Neon 💀", "💀", Color(0xFFFFD24C), "Gema Dorada 💎", listOf(Color(0xFF3B2E0B), Color(0xFF0A071A)))
)

private data class SnakeState(
    val worldId: Int = 1,
    val levelId: Int = 1,
    val snake: List<Cell> = listOf(Cell(COLS / 2, ROWS / 2)),
    val dir: Dir = Dir.RIGHT,
    val food: Cell = Cell(COLS / 2 + 3, ROWS / 2),
    val obstacles: List<Cell> = emptyList(),
    val isPrism: Boolean = false,
    val score: Int = 0,
    val itemsEaten: Int = 0,
    val targetItems: Int = 5,
    val bossHp: Int = 10,
    val started: Boolean = false,
    val gameOver: Boolean = false,
    val victory: Boolean = false
)

private fun generateObstacles(worldId: Int, levelId: Int): List<Cell> {
    val obstacles = mutableListOf<Cell>()
    val count = (levelId - 1) * 2
    for (i in 0 until count) {
        val ox = (i * 3 + 2) % (COLS - 2) + 1
        val oy = (i * 4 + 3) % (ROWS - 4) + 2
        obstacles.add(Cell(ox, oy))
    }
    return obstacles
}

private fun randomEmptyCell(occupied: List<Cell>, obstacles: List<Cell>): Cell {
    while (true) {
        val c = Cell(Random.nextInt(1, COLS - 1), Random.nextInt(1, ROWS - 1))
        if (occupied.none { it.x == c.x && it.y == c.y } && obstacles.none { it.x == c.x && it.y == c.y }) {
            return c
        }
    }
}

private fun advanceSnake(state: SnakeState, requestedDir: Dir): SnakeState {
    if (!state.started || state.gameOver || state.victory) return state

    val newDir = if ((requestedDir == Dir.UP && state.dir == Dir.DOWN) ||
        (requestedDir == Dir.DOWN && state.dir == Dir.UP) ||
        (requestedDir == Dir.LEFT && state.dir == Dir.RIGHT) ||
        (requestedDir == Dir.RIGHT && state.dir == Dir.LEFT)
    ) state.dir else requestedDir

    val head = state.snake.first()
    val newHead = Cell(head.x + newDir.dx, head.y + newDir.dy)

    val hitsWall = newHead.x < 0 || newHead.x >= COLS || newHead.y < 0 || newHead.y >= ROWS
    val hitsSelf = state.snake.any { it.x == newHead.x && it.y == newHead.y }
    val hitsObstacle = state.obstacles.any { it.x == newHead.x && it.y == newHead.y }

    if (hitsWall || hitsSelf || hitsObstacle) {
        return state.copy(gameOver = true, dir = newDir)
    }

    val ateFood = newHead.x == state.food.x && newHead.y == state.food.y
    val newSnakeBody = if (ateFood) listOf(newHead) + state.snake else listOf(newHead) + state.snake.dropLast(1)

    if (ateFood) {
        val newEaten = state.itemsEaten + 1
        val gainedScore = if (state.isPrism) 30 else 10
        val isBossLevel = state.levelId == 5
        val newBossHp = if (isBossLevel) (state.bossHp - 1).coerceAtLeast(0) else 0

        val levelCompleted = if (isBossLevel) newBossHp <= 0 else newEaten >= state.targetItems

        return if (levelCompleted) {
            state.copy(
                snake = newSnakeBody,
                score = state.score + gainedScore + 50,
                itemsEaten = newEaten,
                bossHp = 0,
                victory = true
            )
        } else {
            state.copy(
                snake = newSnakeBody,
                dir = newDir,
                food = randomEmptyCell(newSnakeBody, state.obstacles),
                isPrism = Random.nextInt(100) < 15,
                score = state.score + gainedScore,
                itemsEaten = newEaten,
                bossHp = newBossHp
            )
        }
    } else {
        return state.copy(snake = newSnakeBody, dir = newDir)
    }
}

/**
 * Pantalla principal del minijuego "Culebra Glow" con Mapa de Mundos, Niveles y Jefes.
 * Corregida con persistencia de nivel desbloqueado (rememberSaveable), botones formateados y Canvas rico de Bosque Neón.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeGameScreen(
    onExit: () -> Unit,
    onGameOver: (score: Int) -> Unit = {}
) {
    val context = LocalContext.current

    var currentWorldId by rememberSaveable { mutableIntStateOf(1) }
    var currentLevelId by rememberSaveable { mutableIntStateOf(1) }
    var unlockedWorldId by rememberSaveable { mutableIntStateOf(1) }
    var unlockedLevelId by rememberSaveable { mutableIntStateOf(1) }

    var isPlaying by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf(SnakeState()) }
    var pendingDir by remember { mutableStateOf(Dir.RIGHT) }
    var reported by remember { mutableStateOf(false) }

    val timeMillis = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            timeMillis.longValue = System.currentTimeMillis()
            delay(80L)
        }
    }

    val currentWorld = SNAKE_WORLDS.find { it.id == currentWorldId } ?: SNAKE_WORLDS.first()

    fun startLevel(worldId: Int, levelId: Int) {
        currentWorldId = worldId
        currentLevelId = levelId
        val obstacles = generateObstacles(worldId, levelId)
        val isBoss = levelId == 5
        state = SnakeState(
            worldId = worldId,
            levelId = levelId,
            obstacles = obstacles,
            targetItems = if (isBoss) 8 else 4 + levelId,
            bossHp = if (isBoss) 8 else 0,
            started = true
        )
        pendingDir = Dir.RIGHT
        reported = false
        isPlaying = true
    }

    // Loop principal del juego cuando se está jugando
    LaunchedEffect(isPlaying, state.started, state.gameOver, state.victory) {
        if (!isPlaying || !state.started || state.gameOver || state.victory) return@LaunchedEffect
        val tickMs = (190L - (currentLevelId * 14L)).coerceAtLeast(100L)
        var lastScore = state.score

        while (isActive) {
            delay(tickMs)
            state = advanceSnake(state, pendingDir)
            if (state.score > lastScore) {
                GlowSoundManager.playGameAction(context)
                lastScore = state.score
            }
            if (state.gameOver) {
                GlowSoundManager.playVictory(context)
            }
            if (state.victory) {
                GlowSoundManager.playVictory(context)
                // Desbloqueo progresivo guardado
                if (currentLevelId < 5) {
                    unlockedLevelId = maxOf(unlockedLevelId, currentLevelId + 1)
                } else {
                    unlockedWorldId = maxOf(unlockedWorldId, currentWorldId + 1)
                    unlockedLevelId = 1
                }
            }
        }
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(currentWorld.bgGradient))
        ) {
            if (!isPlaying) {
                // VISTA DE SELECTOR DE MUNDOS Y NIVELES
                WorldSelectorScreen(
                    currentWorld = currentWorld,
                    selectedWorldId = currentWorldId,
                    unlockedWorldId = unlockedWorldId,
                    unlockedLevelId = unlockedLevelId,
                    onSelectWorld = { currentWorldId = it },
                    onStartLevel = { levelId -> startLevel(currentWorldId, levelId) },
                    onExit = onExit
                )
            } else {
                // VISTA DE TABLERO DE JUEGO (GAMEPLAY CON CANVAS RICO)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header de la partida
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "← Volver",
                            color = ElectricCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isPlaying = false }
                                .padding(8.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${currentWorld.icon} ${currentWorld.name}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (currentLevelId == 5) "👹 JEFE: ${currentWorld.bossName}" else "Nivel $currentLevelId / 5",
                                color = currentWorld.accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "★ ${state.score}",
                            color = NeonLime,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Indicador de Objetivo o HP de Jefe
                    if (currentLevelId == 5) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("👹 ${currentWorld.bossName}", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("HP: ${state.bossHp}/8", color = Color.White, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (state.bossHp / 8f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = Color.Red,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Manzanas Neón: ${state.itemsEaten} / ${state.targetItems}",
                                color = ElectricCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tablero de Juego (Canvas Bioluminiscente de Bosque Neón)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .aspectRatio(COLS / ROWS.toFloat())
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF070414))
                            .border(1.5.dp, currentWorld.accentColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                            .pointerInput(Unit) {
                                var dx = 0f
                                var dy = 0f
                                detectDragGestures(
                                    onDragStart = { dx = 0f; dy = 0f },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dx += dragAmount.x
                                        dy += dragAmount.y
                                    },
                                    onDragEnd = {
                                        pendingDir = if (abs(dx) > abs(dy)) {
                                            if (dx > 0) Dir.RIGHT else Dir.LEFT
                                        } else {
                                            if (dy > 0) Dir.DOWN else Dir.UP
                                        }
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cw = size.width / COLS
                            val ch = size.height / ROWS

                            // 1. Rejilla Neón de Fondo (20% Alpha)
                            for (i in 0..COLS) {
                                drawLine(currentWorld.accentColor.copy(alpha = 0.12f), Offset(i * cw, 0f), Offset(i * cw, size.height), strokeWidth = 1.5f)
                            }
                            for (j in 0..ROWS) {
                                drawLine(currentWorld.accentColor.copy(alpha = 0.12f), Offset(0f, j * ch), Offset(size.width, j * ch), strokeWidth = 1.5f)
                            }

                            // 2. RENDERIZADO DE ÁRBOLES Y HONGOS EN EL BOSQUE NEÓN
                            if (currentWorldId == 1) {
                                // Dibujar árboles bioluminiscentes en celdas decorativas fijas
                                val treeCells = listOf(Cell(2, 3), Cell(10, 4), Cell(1, 12), Cell(11, 14), Cell(4, 16))
                                treeCells.forEach { tree ->
                                    val center = Offset(tree.x * cw + cw / 2, tree.y * ch + ch / 2)
                                    val radius = cw * 0.45f
                                    drawCircle(color = Color(0xFF0C240A), radius = radius, center = center)
                                    drawCircle(color = NeonLime, radius = radius, center = center, style = Stroke(width = 3.5f))
                                    drawCircle(color = NeonLime.copy(alpha = 0.35f), radius = radius * 1.3f, center = center, style = Stroke(width = 6f))
                                }

                                // Dibujar hongos neón magenta en celdas decorativas
                                val mushroomCells = listOf(Cell(8, 2), Cell(3, 8), Cell(9, 10), Cell(2, 17))
                                mushroomCells.forEach { mush ->
                                    val center = Offset(mush.x * cw + cw / 2, mush.y * ch + ch / 2)
                                    val capRadius = cw * 0.4f
                                    drawRect(color = ElectricCyan, topLeft = Offset(center.x - 3f, center.y), size = Size(6f, capRadius))
                                    drawArc(color = Color(0xFFFF007F), startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(center.x - capRadius, center.y - capRadius), size = Size(capRadius * 2f, capRadius * 2f))
                                    drawCircle(color = Color.White, radius = 2.5f, center = Offset(center.x - capRadius * 0.4f, center.y - capRadius * 0.4f))
                                    drawCircle(color = Color.White, radius = 2.5f, center = Offset(center.x + capRadius * 0.4f, center.y - capRadius * 0.4f))
                                }

                                // Dibuja Luciérnagas Titilantes de Fondo
                                val fireflyCells = listOf(Cell(1, 1), Cell(6, 3), Cell(11, 7), Cell(3, 11), Cell(8, 15))
                                fireflyCells.forEach { ff ->
                                    val center = Offset(ff.x * cw + cw / 2, ff.y * ch + ch / 2)
                                    val seed = ff.hashCode()
                                    val flickerAlpha = 0.35f + 0.65f * abs(sin((timeMillis.longValue + seed) / 180f))
                                    drawCircle(color = Color(0xFFFFD24C).copy(alpha = 0.4f * flickerAlpha), radius = 10f, center = center)
                                    drawCircle(color = Color(0xFFFFFFB0).copy(alpha = flickerAlpha), radius = 4f, center = center)
                                }
                            }

                            // 3. Dibuja Obstáculos del Nivel
                            state.obstacles.forEach { obs ->
                                val pos = Offset(obs.x * cw + cw * 0.1f, obs.y * ch + ch * 0.1f)
                                drawRoundRect(
                                    color = Color(0xFFFF4444).copy(alpha = 0.8f),
                                    topLeft = pos,
                                    size = Size(cw * 0.8f, ch * 0.8f),
                                    cornerRadius = CornerRadius(cw * 0.25f)
                                )
                                drawRoundRect(
                                    color = Color(0xFFFF4444),
                                    topLeft = pos,
                                    size = Size(cw * 0.8f, ch * 0.8f),
                                    cornerRadius = CornerRadius(cw * 0.25f),
                                    style = Stroke(width = 2.5f)
                                )
                            }

                            // 4. Dibuja Comida / Manzana Neón Bioluminiscente
                            val foodColor = if (state.isPrism) Color(0xFFFFD24C) else currentWorld.accentColor
                            val foodCenter = Offset(state.food.x * cw + cw / 2, state.food.y * ch + ch / 2)
                            drawCircle(
                                color = foodColor.copy(alpha = 0.35f),
                                radius = minOf(cw, ch) * 0.65f,
                                center = foodCenter
                            )
                            drawCircle(
                                color = foodColor,
                                radius = minOf(cw, ch) * 0.38f,
                                center = foodCenter
                            )
                            drawCircle(
                                color = Color.White,
                                radius = minOf(cw, ch) * 0.18f,
                                center = foodCenter
                            )

                            // 5. Dibuja Serpiente Neón (Segmentos + Cabeza con Ojos)
                            state.snake.forEachIndexed { index, cell ->
                                val isHead = (index == 0)
                                val center = Offset(cell.x * cw + cw / 2, cell.y * ch + ch / 2)
                                val radius = minOf(cw, ch) * 0.42f

                                val segmentColor = if (isHead) Color.White else currentWorld.accentColor

                                // Halo bioluminiscente
                                drawCircle(
                                    color = currentWorld.accentColor.copy(alpha = if (isHead) 0.5f else 0.25f),
                                    radius = radius * 1.6f,
                                    center = center
                                )
                                drawCircle(
                                    color = currentWorld.accentColor,
                                    radius = radius * 1.2f,
                                    center = center
                                )
                                drawCircle(
                                    color = segmentColor,
                                    radius = radius,
                                    center = center
                                )

                                // Ojos en la cabeza
                                if (isHead) {
                                    val eyeOffset = radius * 0.4f
                                    drawCircle(color = Color.Black, radius = 4f, center = Offset(center.x - eyeOffset, center.y - eyeOffset))
                                    drawCircle(color = NeonLime, radius = 2.5f, center = Offset(center.x - eyeOffset, center.y - eyeOffset))
                                    drawCircle(color = Color.Black, radius = 4f, center = Offset(center.x + eyeOffset, center.y - eyeOffset))
                                    drawCircle(color = NeonLime, radius = 2.5f, center = Offset(center.x + eyeOffset, center.y - eyeOffset))
                                }
                            }
                        }

                        // Superposición de Derrota (Game Over)
                        if (state.gameOver) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.75f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💀 ¡PERDISTE!", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                    Text("Puntaje: ${state.score}", color = Color.White, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { startLevel(currentWorldId, currentLevelId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Reintentar Nivel", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Superposición de Victoria (Level Complete / Boss Defeated)
                        if (state.victory) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = if (currentLevelId == 5) "👑 ¡JEFE DERROTADO!" else "⭐ ¡NIVEL COMPLETADO!",
                                        color = NeonLime,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val coinsGained = if (currentLevelId == 5) 100 else 10 + (currentLevelId * 5)
                                    Text("Recompensa: +$coinsGained GlowCoins", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                                    if (currentLevelId == 5) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("💎 ¡CONSEGUISTE ${currentWorld.gemType}!", color = Color(0xFFFFD24C), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Button(
                                        onClick = {
                                            onGameOver(state.score)
                                            val nextLevel = if (currentLevelId < 5) currentLevelId + 1 else 1
                                            val nextWorld = if (currentLevelId == 5 && currentWorldId < 5) currentWorldId + 1 else currentWorldId
                                            unlockedLevelId = maxOf(unlockedLevelId, nextLevel)
                                            unlockedWorldId = maxOf(unlockedWorldId, nextWorld)
                                            startLevel(nextWorld, nextLevel)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(
                                            text = if (currentLevelId < 5) "Siguiente Nivel >" else "Continuar al Mundo",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // DPad de Control
                    SnakeDPad(onDirection = { pendingDir = it })
                }
            }
        }
    }
}

/**
 * Pantalla de Selección de Mundos y Niveles para Culebra Glow.
 */
@Composable
private fun WorldSelectorScreen(
    currentWorld: SnakeWorld,
    selectedWorldId: Int,
    unlockedWorldId: Int,
    unlockedLevelId: Int,
    onSelectWorld: (Int) -> Unit,
    onStartLevel: (Int) -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Volver",
                color = ElectricCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onExit() }
            )

            Text(
                text = "CULEBRA GLOW",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text("🌱", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Banner del Mundo Actual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.linearGradient(currentWorld.bgGradient))
                .border(1.5.dp, currentWorld.accentColor, RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MUNDO ${currentWorld.id}",
                        color = currentWorld.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = currentWorld.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "👹 Jefe: ${currentWorld.bossName}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }

                Text(currentWorld.icon, fontSize = 54.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fila Horizontal de Mundos
        Text("🌌 Selecciona un Mundo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SNAKE_WORLDS) { world ->
                val isUnlocked = world.id <= unlockedWorldId
                val isSelected = world.id == selectedWorldId

                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(85.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) world.accentColor.copy(alpha = 0.3f) else Color(0x221E1735)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) world.accentColor else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(enabled = isUnlocked) { onSelectWorld(world.id) }
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mundo ${world.id}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(if (isUnlocked) world.icon else "🔒", fontSize = 14.sp)
                        }
                        Text(
                            text = world.name,
                            color = if (isUnlocked) Color.White else OnSurfaceMuted,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid de Niveles del Mundo Seleccionado
        Text("🎯 Niveles de ${currentWorld.name}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            (1..5).forEach { levelId ->
                val isUnlocked = selectedWorldId < unlockedWorldId || (selectedWorldId == unlockedWorldId && levelId <= unlockedLevelId)
                val isBoss = levelId == 5

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isUnlocked) {
                                if (isBoss) Color(0x33FF007F) else Color(0x221E1735)
                            } else {
                                Color(0x11FFFFFF)
                            }
                        )
                        .border(
                            1.dp,
                            if (isUnlocked) (if (isBoss) Color(0xFFFF007F) else currentWorld.accentColor) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable(enabled = isUnlocked) { onStartLevel(levelId) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBoss) "👹" else "Nivel $levelId",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isBoss) "JEFE: ${currentWorld.bossName}" else "Come manzanas y evita obstáculos",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isUnlocked) {
                            Button(
                                onClick = { onStartLevel(levelId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBoss) Color(0xFFFF007F) else currentWorld.accentColor
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp).defaultMinSize(minWidth = 85.dp)
                            ) {
                                Text(
                                    text = "JUGAR",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        } else {
                            Text("🔒", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnakeDPad(onDirection: (Dir) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        SnakePadButton("▲") { onDirection(Dir.UP) }
        Row(horizontalArrangement = Arrangement.spacedBy(46.dp)) {
            SnakePadButton("◀") { onDirection(Dir.LEFT) }
            SnakePadButton("▶") { onDirection(Dir.RIGHT) }
        }
        SnakePadButton("▼") { onDirection(Dir.DOWN) }
    }
}

@Composable
private fun SnakePadButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xCC150E30))
            .border(1.5.dp, ElectricCyan, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = ElectricCyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}
