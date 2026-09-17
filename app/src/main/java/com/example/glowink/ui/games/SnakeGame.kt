package com.example.glowink.ui.games

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.components.GlowPrimaryButton
import com.example.glowink.ui.components.GlowSecondaryButton
import com.example.glowink.ui.theme.*
import com.example.glowink.util.GlowHapticManager
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

/**
 * Curva de Dificultad Dinámica que regula velocidad, densidad de obstáculos, objetivos y HP de jefes.
 */
data class DifficultyCurve(
    val baseTickMs: Long,
    val speedMultiplier: Float,
    val obstacleCount: Int,
    val targetItems: Int,
    val bossHp: Int,
    val difficultyLabel: String
) {
    companion object {
        fun forWorldAndLevel(worldId: Int, levelId: Int): DifficultyCurve {
            val isBoss = levelId == 5
            val rawMs = 180 - (worldId * 16) - (levelId * 10)
            val tickMs = rawMs.coerceIn(50, 180).toLong()
            val speedMult = 180f / tickMs.toFloat()

            val obsCount = if (isBoss) (worldId * 2 + 3) else ((levelId - 1) * 2 + (worldId - 1) * 2)
            val targets = if (isBoss) 8 + worldId * 2 else 4 + levelId + worldId
            val bossHealth = if (isBoss) 6 + worldId * 3 else 0

            val label = when {
                worldId >= 4 || (worldId == 3 && levelId >= 4) -> "EXTREMA ⚡"
                worldId >= 3 || (worldId == 2 && levelId >= 4) -> "ALTA 🔥"
                worldId >= 2 || levelId >= 3 -> "MEDIA ⚡"
                else -> "NORMAL 🌱"
            }

            return DifficultyCurve(
                baseTickMs = tickMs,
                speedMultiplier = speedMult,
                obstacleCount = obsCount,
                targetItems = targets,
                bossHp = bossHealth,
                difficultyLabel = label
            )
        }
    }
}

/**
 * Tema de Color Neón Procedural y Rejilla por Nivel.
 */
data class SnakeLevelTheme(
    val snakeHeadColor: Color,
    val snakeBodyColor: Color,
    val accentColor: Color,
    val foodColor: Color,
    val obstacleColor: Color,
    val gridLineColor: Color,
    val bgGradient: List<Color>,
    val gridStyleName: String
)

fun getLevelTheme(worldId: Int, levelId: Int): SnakeLevelTheme {
    return if (levelId == 5) {
        // NIVEL JEFE: Rojo Fuego Neón / Alerta Volcánica 🌋
        SnakeLevelTheme(
            snakeHeadColor = Color(0xFFFFD700), // Oro Imperial
            snakeBodyColor = Color(0xFFFF2222), // Rojo Fuego Neón
            accentColor = Color(0xFFFF2222),
            foodColor = Color(0xFFFF6600), // Naranja Fuego
            obstacleColor = Color(0xFFFF1100),
            gridLineColor = Color(0xFFFF2222).copy(alpha = 0.25f),
            bgGradient = listOf(Color(0xFF3A0000), Color(0xFF1A000A), Color(0xFF070414)),
            gridStyleName = "Alerta Volcánica 🌋"
        )
    } else {
        when (levelId) {
            1 -> SnakeLevelTheme(
                snakeHeadColor = Color.White,
                snakeBodyColor = Color(0xFF39FF14), // Verde Neón Bioluminiscente
                accentColor = Color(0xFF39FF14),
                foodColor = Color(0xFF7FFF00),
                obstacleColor = Color(0xFF1E824C),
                gridLineColor = Color(0xFF39FF14).copy(alpha = 0.15f),
                bgGradient = listOf(Color(0xFF0A290C), Color(0xFF070414)),
                gridStyleName = "Jardín Esmeralda 🌱"
            )
            2 -> SnakeLevelTheme(
                snakeHeadColor = Color.White,
                snakeBodyColor = Color(0xFF00E5FF), // Cian Eléctrico Cyber
                accentColor = Color(0xFF00E5FF),
                foodColor = Color(0xFF00FFFF),
                obstacleColor = Color(0xFF007799),
                gridLineColor = Color(0xFF00E5FF).copy(alpha = 0.15f),
                bgGradient = listOf(Color(0xFF00223E), Color(0xFF070414)),
                gridStyleName = "Matriz Cyberpunk 🌃"
            )
            3 -> SnakeLevelTheme(
                snakeHeadColor = Color.White,
                snakeBodyColor = Color(0xFFFF007F), // Fucsia Neón Vibrante
                accentColor = Color(0xFFFF007F),
                foodColor = Color(0xFFFF4081),
                obstacleColor = Color(0xFF99004C),
                gridLineColor = Color(0xFFFF007F).copy(alpha = 0.15f),
                bgGradient = listOf(Color(0xFF3A002A), Color(0xFF070414)),
                gridStyleName = "Fibras Magenta 🌺"
            )
            else -> SnakeLevelTheme( // Level 4
                snakeHeadColor = Color.White,
                snakeBodyColor = Color(0xFFB026FF), // Violeta/Morado Cósmico
                accentColor = Color(0xFFB026FF),
                foodColor = Color(0xFFE040FB),
                obstacleColor = Color(0xFF550099),
                gridLineColor = Color(0xFFB026FF).copy(alpha = 0.15f),
                bgGradient = listOf(Color(0xFF23003A), Color(0xFF070414)),
                gridStyleName = "Malla Cósmica 🌌"
            )
        }
    }
}

data class SnakeWorld(
    val id: Int,
    val name: String,
    val icon: String,
    val bossName: String,
    val bossIcon: String,
    val defaultAccent: Color,
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
    val curve = DifficultyCurve.forWorldAndLevel(worldId, levelId)
    val count = curve.obstacleCount

    for (i in 0 until count) {
        val ox = when (levelId) {
            1 -> (i * 3 + 2) % (COLS - 2) + 1
            2 -> (i * 2 + 1) % (COLS - 2) + 1
            3 -> (i * 4 + 3) % (COLS - 2) + 1
            4 -> (i * 3 + 1) % (COLS - 2) + 1
            else -> (i * 2 + 2) % (COLS - 2) + 1 // Jefe
        }
        val oy = when (levelId) {
            1 -> (i * 4 + 3) % (ROWS - 4) + 2
            2 -> (i * 3 + 2) % (ROWS - 4) + 2
            3 -> (i * 2 + 4) % (ROWS - 4) + 2
            4 -> (i * 4 + 1) % (ROWS - 4) + 2
            else -> (i * 3 + 3) % (ROWS - 4) + 2 // Jefe
        }
        val c = Cell(ox, oy)
        if (c.x != COLS / 2 && c.y != ROWS / 2) {
            obstacles.add(c)
        }
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
 * Pantalla principal de "Culebra Glow" con Selección de Mundos, Niveles, Variedad Neón Procedural
 * y Aislamiento total de bucles de juego para liberación de memoria al salir.
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
    var lastFrameTime by remember { mutableLongStateOf(0L) }

    val timeMillis = remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Limpieza al desmontar el componente (Prevención de fugas de memoria y mezcla de estados)
    DisposableEffect(Unit) {
        onDispose {
            isPlaying = false
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            timeMillis.longValue = System.currentTimeMillis()
            delay(80L)
        }
    }

    val currentWorld = SNAKE_WORLDS.find { it.id == currentWorldId } ?: SNAKE_WORLDS.first()
    val levelTheme = getLevelTheme(currentWorldId, currentLevelId)
    val difficulty = DifficultyCurve.forWorldAndLevel(currentWorldId, currentLevelId)

    fun startLevel(worldId: Int, levelId: Int) {
        currentWorldId = worldId
        currentLevelId = levelId
        val diff = DifficultyCurve.forWorldAndLevel(worldId, levelId)
        val obstacles = generateObstacles(worldId, levelId)
        val isBoss = levelId == 5
        state = SnakeState(
            worldId = worldId,
            levelId = levelId,
            obstacles = obstacles,
            targetItems = diff.targetItems,
            bossHp = if (isBoss) diff.bossHp else 0,
            started = true
        )
        pendingDir = Dir.RIGHT
        reported = false
        isPlaying = true
    }

    fun exitToMenu() {
        isPlaying = false
        onExit()
    }

    // Loop principal del juego con tick rate constante regulado por dificultad
    LaunchedEffect(isPlaying, state.started, state.gameOver, state.victory, currentWorldId, currentLevelId) {
        if (!isPlaying || !state.started || state.gameOver || state.victory) return@LaunchedEffect

        var lastScore = state.score

        while (isActive && isPlaying && !state.gameOver && !state.victory) {
            val tickMs = difficulty.baseTickMs.coerceIn(50L, 180L)
            delay(tickMs)

            if (!isPlaying || state.gameOver || state.victory) break

            state = advanceSnake(state, pendingDir)

            if (state.score > lastScore) {
                GlowSoundManager.playGameAction(context)
                GlowHapticManager.vibrateImpact(context)
                lastScore = state.score
            }

            if (state.gameOver) {
                GlowSoundManager.playVictory(context)
                GlowHapticManager.vibrateError(context)
            }

            if (state.victory) {
                GlowSoundManager.playVictory(context)
                GlowHapticManager.vibrateSuccess(context)
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
                .background(Brush.verticalGradient(levelTheme.bgGradient))
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
                    onExit = { exitToMenu() }
                )
            } else {
                // VISTA DE TABLERO DE JUEGO (GAMEPLAY CON CANVAS PROCEDURAL)
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
                            color = levelTheme.accentColor,
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
                                text = if (currentLevelId == 5) "👹 JEFE: ${currentWorld.bossName}" else "Nivel $currentLevelId - ${levelTheme.gridStyleName}",
                                color = levelTheme.accentColor,
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badges de Dificultad y Velocidad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "Dificultad: ${difficulty.difficultyLabel}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Velocidad: ${"%.1f".format(difficulty.speedMultiplier)}x",
                            color = ElectricCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Indicador de Objetivo o HP de Jefe
                    if (currentLevelId == 5) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("👹 ${currentWorld.bossName}", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("HP: ${state.bossHp}/${difficulty.bossHp}", color = Color.White, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { if (difficulty.bossHp > 0) (state.bossHp.toFloat() / difficulty.bossHp.toFloat()).coerceIn(0f, 1f) else 0f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = Color.Red,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Gemas Eaten: ${state.itemsEaten} / ${state.targetItems}",
                                color = levelTheme.accentColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tablero de Juego (Canvas Ailado Bioluminiscente)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .aspectRatio(COLS / ROWS.toFloat())
                            .clip(RoundedCornerShape(16.dp))
                            .clipToBounds()
                            .background(Color(0xFF070414))
                            .border(1.5.dp, levelTheme.accentColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                            .pointerInput(Unit) {
                                var accumulatedDx = 0f
                                var accumulatedDy = 0f
                                detectDragGestures(
                                    onDragStart = {
                                        accumulatedDx = 0f
                                        accumulatedDy = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        accumulatedDx += dragAmount.x
                                        accumulatedDy += dragAmount.y

                                        if (abs(accumulatedDx) > 24f || abs(accumulatedDy) > 24f) {
                                            if (abs(accumulatedDx) > abs(accumulatedDy)) {
                                                val newDir = if (accumulatedDx > 0) Dir.RIGHT else Dir.LEFT
                                                if ((newDir == Dir.LEFT && state.dir != Dir.RIGHT) || (newDir == Dir.RIGHT && state.dir != Dir.LEFT)) {
                                                    pendingDir = newDir
                                                    accumulatedDx = 0f
                                                    accumulatedDy = 0f
                                                }
                                            } else {
                                                val newDir = if (accumulatedDy > 0) Dir.DOWN else Dir.UP
                                                if ((newDir == Dir.UP && state.dir != Dir.DOWN) || (newDir == Dir.DOWN && state.dir != Dir.UP)) {
                                                    pendingDir = newDir
                                                    accumulatedDx = 0f
                                                    accumulatedDy = 0f
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                            val cw = size.width / COLS
                            val ch = size.height / ROWS

                            // 1. Rejilla Neón Procedural del Nivel
                            val gridAlpha = if (currentLevelId == 5) 0.28f else 0.14f
                            for (i in 0..COLS) {
                                drawLine(
                                    color = levelTheme.gridLineColor.copy(alpha = gridAlpha),
                                    start = Offset(i * cw, 0f),
                                    end = Offset(i * cw, size.height),
                                    strokeWidth = 1.5f
                                )
                            }
                            for (j in 0..ROWS) {
                                drawLine(
                                    color = levelTheme.gridLineColor.copy(alpha = gridAlpha),
                                    start = Offset(0f, j * ch),
                                    end = Offset(size.width, j * ch),
                                    strokeWidth = 1.5f
                                )
                            }

                            // 2. Elementos Decorativos Dinámicos de Fondo por Nivel
                            when (currentLevelId) {
                                1 -> {
                                    // Bosque Neón Bioluminiscente
                                    val treeCells = listOf(Cell(2, 3), Cell(10, 4), Cell(1, 12), Cell(11, 14))
                                    treeCells.forEach { tree ->
                                        val center = Offset(tree.x * cw + cw / 2, tree.y * ch + ch / 2)
                                        val radius = cw * 0.42f
                                        drawCircle(color = Color(0xFF0C240A), radius = radius, center = center)
                                        drawCircle(color = levelTheme.accentColor, radius = radius, center = center, style = Stroke(width = 3f))
                                    }
                                }
                                2 -> {
                                    // Nodos de Energía Cyber
                                    val nodeCells = listOf(Cell(3, 4), Cell(9, 3), Cell(2, 14), Cell(10, 15))
                                    nodeCells.forEach { node ->
                                        val center = Offset(node.x * cw + cw / 2, node.y * ch + ch / 2)
                                        val boxSize = Size(cw * 0.7f, ch * 0.7f)
                                        drawRect(color = Color(0xFF00334E), topLeft = Offset(center.x - boxSize.width / 2, center.y - boxSize.height / 2), size = boxSize)
                                        drawRect(color = ElectricCyan, topLeft = Offset(center.x - boxSize.width / 2, center.y - boxSize.height / 2), size = boxSize, style = Stroke(width = 2.5f))
                                    }
                                }
                                3 -> {
                                    // Fibras Magenta Flotantes
                                    val fiberCells = listOf(Cell(4, 2), Cell(8, 5), Cell(2, 10), Cell(9, 16))
                                    fiberCells.forEach { fiber ->
                                        val seed = fiber.hashCode()
                                        val pulse = abs(sin((timeMillis.longValue + seed) / 250f))
                                        val center = Offset(fiber.x * cw + cw / 2, fiber.y * ch + ch / 2)
                                        drawCircle(color = levelTheme.accentColor.copy(alpha = 0.3f * pulse), radius = cw * 0.5f, center = center)
                                    }
                                }
                                4 -> {
                                    // Nebulosa Cósmica Titilante
                                    val starCells = listOf(Cell(1, 2), Cell(11, 4), Cell(3, 11), Cell(10, 16))
                                    starCells.forEach { star ->
                                        val seed = star.hashCode()
                                        val flickerAlpha = 0.3f + 0.7f * abs(sin((timeMillis.longValue + seed) / 180f))
                                        val center = Offset(star.x * cw + cw / 2, star.y * ch + ch / 2)
                                        drawCircle(color = levelTheme.accentColor.copy(alpha = flickerAlpha), radius = 5f, center = center)
                                    }
                                }
                                5 -> {
                                    // Nivel Jefe: Grietas de Lava y Alerta Volcánica
                                    val lavaCells = listOf(Cell(1, 1), Cell(11, 1), Cell(1, 17), Cell(11, 17), Cell(6, 9))
                                    lavaCells.forEach { lava ->
                                        val center = Offset(lava.x * cw + cw / 2, lava.y * ch + ch / 2)
                                        drawCircle(color = Color(0xFFFF2222).copy(alpha = 0.45f), radius = cw * 0.65f, center = center)
                                        drawCircle(color = Color(0xFFFFD700), radius = cw * 0.25f, center = center)
                                    }
                                }
                            }

                            // 3. Obstáculos del Nivel con Color Procedural
                            state.obstacles.forEach { obs ->
                                val pos = Offset(obs.x * cw + cw * 0.1f, obs.y * ch + ch * 0.1f)
                                val boxSize = Size(cw * 0.8f, ch * 0.8f)
                                drawRoundRect(
                                    color = levelTheme.obstacleColor.copy(alpha = 0.85f),
                                    topLeft = pos,
                                    size = boxSize,
                                    cornerRadius = CornerRadius(cw * 0.25f)
                                )
                                drawRoundRect(
                                    color = levelTheme.accentColor,
                                    topLeft = pos,
                                    size = boxSize,
                                    cornerRadius = CornerRadius(cw * 0.25f),
                                    style = Stroke(width = 2.5f)
                                )
                            }

                            // 4. Comida / Gema Neón Bioluminiscente
                            val foodColor = if (state.isPrism) Color(0xFFFFD24C) else levelTheme.foodColor
                            val foodCenter = Offset(state.food.x * cw + cw / 2, state.food.y * ch + ch / 2)
                            drawCircle(
                                color = foodColor.copy(alpha = 0.4f),
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

                            // 5. Serpiente Neón (Segmentos + Cabeza con Ojos y Color Procedural)
                            state.snake.forEachIndexed { index, cell ->
                                val isHead = (index == 0)
                                val center = Offset(cell.x * cw + cw / 2, cell.y * ch + ch / 2)
                                val radius = minOf(cw, ch) * 0.42f

                                val segmentColor = if (isHead) levelTheme.snakeHeadColor else levelTheme.snakeBodyColor

                                drawCircle(
                                    color = levelTheme.accentColor.copy(alpha = if (isHead) 0.55f else 0.25f),
                                    radius = radius * 1.6f,
                                    center = center
                                )
                                drawCircle(
                                    color = levelTheme.accentColor,
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
                                    .background(Color.Black.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💀 ¡PERDISTE!", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 24.sp)
                                    Text("Puntaje: ${state.score}", color = Color.White, fontSize = 16.sp)
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

                        // Superposición de Victoria
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
 * Pantalla de Selección de Mundos y Niveles para Culebra Glow con Curva de Dificultad.
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
                .height(135.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.linearGradient(currentWorld.bgGradient))
                .border(1.5.dp, currentWorld.defaultAccent, RoundedCornerShape(22.dp))
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
                        color = currentWorld.defaultAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = currentWorld.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "👹 Jefe: ${currentWorld.bossName}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(currentWorld.icon, fontSize = 52.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fila Horizontal de Selección de Mundos
        Text("🌌 Selecciona un Mundo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SNAKE_WORLDS) { world ->
                val isUnlocked = world.id <= unlockedWorldId
                val isSelected = world.id == selectedWorldId
                val worldDiff = DifficultyCurve.forWorldAndLevel(world.id, 1)

                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) world.defaultAccent.copy(alpha = 0.3f) else Color(0x221E1735)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) world.defaultAccent else Color.White.copy(alpha = 0.2f),
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
                        Text(
                            text = worldDiff.difficultyLabel,
                            color = world.defaultAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid de Niveles con Curva de Dificultad Dinámica
        Text("🎯 Niveles de ${currentWorld.name}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            (1..5).forEach { levelId ->
                val isUnlocked = selectedWorldId < unlockedWorldId || (selectedWorldId == unlockedWorldId && levelId <= unlockedLevelId)
                val isBoss = levelId == 5
                val curve = DifficultyCurve.forWorldAndLevel(selectedWorldId, levelId)
                val levelTheme = getLevelTheme(selectedWorldId, levelId)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
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
                            if (isUnlocked) levelTheme.accentColor else Color.White.copy(alpha = 0.1f),
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
                            Column {
                                Text(
                                    text = if (isBoss) "JEFE: ${currentWorld.bossName}" else levelTheme.gridStyleName,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Velocidad: ${"%.1f".format(curve.speedMultiplier)}x | Obstáculos: ${curve.obstacleCount}",
                                    color = levelTheme.accentColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (isUnlocked) {
                            GlowPrimaryButton(
                                text = "JUGAR",
                                onClick = { onStartLevel(levelId) },
                                containerColor = levelTheme.accentColor,
                                contentColor = Color.Black,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp
                            )
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
