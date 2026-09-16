package com.example.glowink.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.random.Random

// ============================================================================
// 1. ENUMS (CULEBRA GLOW - FASES 1 A 8)
// ============================================================================

enum class Direction(val dx: Float, val dy: Float) {
    UP(0f, -1f),
    DOWN(0f, 1f),
    LEFT(-1f, 0f),
    RIGHT(1f, 0f)
}

enum class GameState {
    WELCOME,
    CUSTOMIZATION,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class SnakeColor(val hexColor: Long, val glowHex: Long) {
    CYAN(0xFF00F0FF, 0x8000F0FF),
    MAGENTA(0xFFFF007F, 0x80FF007F),
    GREEN(0xFF39FF14, 0x8039FF14)
}

enum class BackgroundStyle {
    SIMPLE,
    GRID
}

enum class WorldType(val title: String, val description: String) {
    BOSQUE_NEON("BOSQUE NEÓN", "Bosque nocturno bioluminiscente"),
    ABISMO_GLOW("ABISMO GLOW", "Océano profundo y fantástico"),
    NEON_CITY("NEON CITY", "Metrópolis cyberpunk synthwave"),
    COSMIC_SNAKE("COSMIC SNAKE", "Espacio profundo y nebulosas"),
    CRYSTAL_TEMPLE("CRYSTAL TEMPLE", "Templo místico de cristales"),
    DRAGON_REALM("DRAGON REALM", "Reino de montañas y dragones")
}

enum class ItemType(val points: Int, val xp: Int, val coins: Int, val growthAmount: Int, val colorHex: Long) {
    FRUIT(10, 5, 1, 1, 0xFF39FF14),       // Verde Neón (+1 segmento)
    BERRY(15, 8, 2, 1, 0xFFFF007F),       // Rosa Neón (+1 segmento)
    MUSHROOM(20, 10, 3, 2, 0xFF9D4EDD),    // Púrpura (+2 segmentos)
    PEARL(25, 12, 5, 2, 0xFF00F0FF),       // Cyan Neón (+2 segmentos)
    BATTERY(30, 15, 5, 2, 0xFFFFD24C),     // Amarillo Neón (+2 segmentos)
    COIN(50, 20, 10, 1, 0xFFFFD700),      // Dorado (+1 segmento)
    CRYSTAL(100, 50, 25, 5, 0xFFFF00FF),   // Magenta Mágico (+5 segmentos)
    SPEED_BOOST(15, 10, 2, 1, 0xFF007FFF),  // Azul Cyber (+1 segmento)
    MAGNET(20, 15, 3, 1, 0xFFFF5555),     // Rojo Neón (+1 segmento)
    SHIELD(25, 20, 5, 1, 0xFF00FFCC)      // Menta Neón (+1 segmento)
}

// ============================================================================
// 2. DATA CLASSES & MODELOS DE ESTADO (FASES 1 A 8)
// ============================================================================

data class Position(
    val x: Float = 0f,
    val y: Float = 0f
)

data class SnakeSegment(
    val position: Position,
    val angle: Float = 0f
)

data class Food(
    val id: String,
    val position: Position,
    val type: ItemType = ItemType.FRUIT
)

data class Obstacle(
    val id: String,
    val position: Position,
    val width: Float = 100f,
    val height: Float = 100f,
    val shapeType: String = "RECT" // TREE, MUSHROOM, CORAL, RUIN, RECT
)

data class Creature(
    val id: String,
    val position: Position,
    val type: String = "FIREFLY", // FIREFLY, JELLYFISH, BUBBLE
    val isScared: Boolean = false
)

data class WorldLevel(
    val worldType: WorldType = WorldType.BOSQUE_NEON,
    val subLevel: Int = 1,
    val name: String = "1-1 Bosque Clásico",
    val objectiveText: String = "COME 10 FRUTAS",
    val worldWidth: Float = 5000f,
    val worldHeight: Float = 5000f
)

data class PlayerStats(
    val points: Int = 0,
    val xpLevel: Int = 7,
    val xpProgress: Float = 0.7f,
    val coins: Int = 3450,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val highScore: Int = 5100
)

data class GameUiState(
    val gameState: GameState = GameState.WELCOME,
    val snakeColor: SnakeColor = SnakeColor.CYAN,
    val backgroundStyle: BackgroundStyle = BackgroundStyle.GRID,
    val direction: Direction = Direction.RIGHT,
    val requestedDirection: Direction = Direction.RIGHT,
    val snakeSegments: List<SnakeSegment> = listOf(
        SnakeSegment(Position(2500f, 2500f), 0f),
        SnakeSegment(Position(2460f, 2500f), 0f),
        SnakeSegment(Position(2420f, 2500f), 0f)
    ),
    val foods: List<Food> = emptyList(),
    val obstacles: List<Obstacle> = emptyList(),
    val creatures: List<Creature> = emptyList(),
    val cameraPosition: Position = Position(2500f, 2500f),
    val currentLevel: WorldLevel = WorldLevel(),
    val unlockedLevels: Set<String> = setOf(
        "BOSQUE_NEON_1", "BOSQUE_NEON_2", "BOSQUE_NEON_3", "BOSQUE_NEON_4", "BOSQUE_NEON_5",
        "ABISMO_GLOW_1", "ABISMO_GLOW_2", "ABISMO_GLOW_3", "ABISMO_GLOW_4", "ABISMO_GLOW_5"
    ),
    val stats: PlayerStats = PlayerStats(),
    val speedMs: Long = INITIAL_SPEED_MS,
    val itemsEaten: Int = 0,
    val pendingGrowthSegments: Int = 0,
    val isInvulnerable: Boolean = false
) {
    companion object {
        const val INITIAL_SPEED_MS = 250L
        const val MIN_SPEED_MS = 80L
        const val STEP_DISTANCE = 40f
        const val TARGET_FOOD_COUNT = 25
        const val EAT_COLLISION_RADIUS = 36f
        const val BODY_COLLISION_RADIUS = 28f
        const val INVULNERABILITY_DURATION_MS = 2500L
    }
}

// ============================================================================
// 3. SNAKE VIEWMODEL CON ENTORNO MUNDO 2 ABISMO GLOW (FASE 8)
// ============================================================================

/**
 * ViewModel principal para Culebra Glow (GDD - FASE 8: MUNDO 2 - ABISMO GLOW).
 * Administra la generación procedural de arrecifes de coral, ruinas marinas, medusas y burbujas.
 */
class SnakeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var invulnerabilityJob: Job? = null

    init {
        val initialSegments = _uiState.value.snakeSegments
        val defaultLevel = WorldLevel(worldType = WorldType.BOSQUE_NEON, subLevel = 1, name = "1-1 Bosque Clásico")
        val (obs, creat) = generateLevelEnvironment(defaultLevel, initialSegments)
        val initialFoods = generateInitialItems(initialSegments)

        _uiState.update {
            it.copy(
                currentLevel = defaultLevel,
                obstacles = obs,
                creatures = creat,
                foods = initialFoods
            )
        }
    }

    // --- TRANSICIONES DE PANTALLA Y SELECCIÓN DE NIVELES ---

    fun goToWelcome() {
        stopGameLoop()
        _uiState.update { it.copy(gameState = GameState.WELCOME) }
    }

    fun goToCustomization() {
        stopGameLoop()
        _uiState.update { it.copy(gameState = GameState.CUSTOMIZATION) }
    }

    fun selectColor(color: SnakeColor) {
        _uiState.update { it.copy(snakeColor = color) }
    }

    fun selectBackgroundStyle(style: BackgroundStyle) {
        _uiState.update { it.copy(backgroundStyle = style) }
    }

    fun selectWorldLevel(worldType: WorldType, subLevel: Int = 1) {
        val levelName = when (worldType) {
            WorldType.BOSQUE_NEON -> "1-$subLevel Bosque Clásico"
            WorldType.ABISMO_GLOW -> "2-$subLevel Arrecife Bioluminiscente"
            WorldType.NEON_CITY -> "3-$subLevel Neon City"
            WorldType.COSMIC_SNAKE -> "4-$subLevel Cosmic Void"
            WorldType.CRYSTAL_TEMPLE -> "5-$subLevel Crystal Shrine"
            WorldType.DRAGON_REALM -> "6-$subLevel Dragon Lair"
        }

        val objectiveText = when (worldType) {
            WorldType.ABISMO_GLOW -> "CONSIGUE 10 PERLAS Y TESOROS 🔮"
            WorldType.NEON_CITY -> "RECOGE 15 BATERÍAS CYBER 🔋"
            else -> "COME 10 FRUTAS NEÓN 🍇"
        }

        val level = WorldLevel(
            worldType = worldType,
            subLevel = subLevel,
            name = levelName,
            objectiveText = objectiveText
        )

        val initialSegments = _uiState.value.snakeSegments
        val (obs, creat) = generateLevelEnvironment(level, initialSegments)
        val foods = generateInitialItems(initialSegments)

        _uiState.update {
            it.copy(
                currentLevel = level,
                obstacles = obs,
                creatures = creat,
                foods = foods
            )
        }
    }

    fun unlockNextLevel() {
        _uiState.update { state ->
            val level = state.currentLevel
            val nextSubLevel = level.subLevel + 1
            val nextLevelKey = "${level.worldType.name}_$nextSubLevel"

            val updatedUnlocked = state.unlockedLevels + nextLevelKey + "${level.worldType.name}_${level.subLevel}"
            val nextLevel = level.copy(
                subLevel = nextSubLevel,
                name = "${level.worldType.title} - Nivel $nextSubLevel"
            )

            val (newObs, newCreat) = generateLevelEnvironment(nextLevel, state.snakeSegments)

            state.copy(
                unlockedLevels = updatedUnlocked,
                currentLevel = nextLevel,
                obstacles = newObs,
                creatures = newCreat
            )
        }
    }

    fun startGame() {
        stopGameLoop()
        invulnerabilityJob?.cancel()

        val initialSegments = listOf(
            SnakeSegment(Position(2500f, 2500f), 0f),
            SnakeSegment(Position(2460f, 2500f), 0f),
            SnakeSegment(Position(2420f, 2500f), 0f)
        )

        val currentLevel = _uiState.value.currentLevel
        val (obstacles, creatures) = generateLevelEnvironment(currentLevel, initialSegments)
        val initialFoods = generateInitialItems(initialSegments)

        _uiState.update { state ->
            state.copy(
                gameState = GameState.PLAYING,
                direction = Direction.RIGHT,
                requestedDirection = Direction.RIGHT,
                snakeSegments = initialSegments,
                obstacles = obstacles,
                creatures = creatures,
                foods = initialFoods,
                cameraPosition = Position(2500f, 2500f),
                speedMs = GameUiState.INITIAL_SPEED_MS,
                itemsEaten = 0,
                pendingGrowthSegments = 0,
                isInvulnerable = false,
                stats = state.stats.copy(points = 0, lives = 3)
            )
        }
        startGameLoop()
    }

    fun updateDirection(newDirection: Direction) {
        val currentDir = _uiState.value.direction
        if (!isOppositeDirection(newDirection, currentDir)) {
            _uiState.update { it.copy(requestedDirection = newDirection) }
        }
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING) {
            stopGameLoop()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            startGameLoop()
        }
    }

    fun restartGame() {
        startGame()
    }

    fun triggerGameOver() {
        stopGameLoop()
        invulnerabilityJob?.cancel()
        _uiState.update { state ->
            val newHighScore = maxOf(state.stats.highScore, state.stats.points)
            state.copy(
                gameState = GameState.GAME_OVER,
                stats = state.stats.copy(highScore = newHighScore)
            )
        }
    }

    // --- TAREA 1: GENERACIÓN PROCEDURAL BOSQUE NEÓN Y ABISMO GLOW (FASE 7 Y 8) ---

    private fun generateLevelEnvironment(
        level: WorldLevel,
        initialSegments: List<SnakeSegment>
    ): Pair<List<Obstacle>, List<Creature>> {
        val obstacles = mutableListOf<Obstacle>()
        val creatures = mutableListOf<Creature>()

        val spawnSafeDistance = 220f
        val startHead = initialSegments.first().position

        when (level.worldType) {
            WorldType.BOSQUE_NEON -> {
                val totalTrees = 45
                val totalMushrooms = 30

                for (i in 0 until totalTrees) {
                    val pos = generateNonCollidingPos(level, startHead, spawnSafeDistance, obstacles)
                    val size = Random.nextFloat() * 60f + 120f
                    obstacles.add(Obstacle("tree_$i", pos, width = size, height = size, shapeType = "TREE"))
                }

                for (i in 0 until totalMushrooms) {
                    val pos = generateNonCollidingPos(level, startHead, spawnSafeDistance, obstacles)
                    val size = Random.nextFloat() * 40f + 70f
                    obstacles.add(Obstacle("mushroom_$i", pos, width = size, height = size, shapeType = "MUSHROOM"))
                }

                for (i in 0..50) {
                    val rx = Random.nextFloat() * (level.worldWidth - 300f) + 150f
                    val ry = Random.nextFloat() * (level.worldHeight - 300f) + 150f
                    creatures.add(Creature("firefly_$i", Position(rx, ry), type = "FIREFLY"))
                }
            }

            WorldType.ABISMO_GLOW -> {
                // Abismo Glow (Océano): Corales, Ruinas, Medusas y Burbujas (TAREA 1 - FASE 8)
                val totalCorals = 25
                val totalRuins = 10

                for (i in 0 until totalCorals) {
                    val pos = generateNonCollidingPos(level, startHead, spawnSafeDistance, obstacles)
                    val size = Random.nextFloat() * 60f + 90f
                    obstacles.add(Obstacle("coral_$i", pos, width = size, height = size, shapeType = "CORAL"))
                }

                for (i in 0 until totalRuins) {
                    val pos = generateNonCollidingPos(level, startHead, spawnSafeDistance, obstacles)
                    val w = Random.nextFloat() * 60f + 100f
                    val h = Random.nextFloat() * 40f + 70f
                    obstacles.add(Obstacle("ruin_$i", pos, width = w, height = h, shapeType = "RUIN"))
                }

                // Medusas Rosadas Flotantes (20)
                for (i in 0..20) {
                    val rx = Random.nextFloat() * (level.worldWidth - 300f) + 150f
                    val ry = Random.nextFloat() * (level.worldHeight - 300f) + 150f
                    creatures.add(Creature("jellyfish_$i", Position(rx, ry), type = "JELLYFISH"))
                }

                // Burbujas Submarinas Ascendentes (40)
                for (i in 0..40) {
                    val rx = Random.nextFloat() * (level.worldWidth - 300f) + 150f
                    val ry = Random.nextFloat() * (level.worldHeight - 300f) + 150f
                    creatures.add(Creature("bubble_$i", Position(rx, ry), type = "BUBBLE"))
                }
            }

            else -> {
                for (i in 0..20) {
                    val pos = generateNonCollidingPos(level, startHead, spawnSafeDistance, obstacles)
                    obstacles.add(Obstacle("generic_$i", pos, width = 100f, height = 100f, shapeType = "RECT"))
                }
            }
        }

        return Pair(obstacles, creatures)
    }

    private fun generateNonCollidingPos(
        level: WorldLevel,
        startHead: Position,
        safeRadius: Float,
        existingObstacles: List<Obstacle>
    ): Position {
        var attempt = 0
        while (attempt < 100) {
            val rx = Random.nextFloat() * (level.worldWidth - 400f) + 200f
            val ry = Random.nextFloat() * (level.worldHeight - 400f) + 200f

            val distToStart = hypot(rx - startHead.x, ry - startHead.y)
            val overlapsObstacle = existingObstacles.any { obs ->
                hypot(rx - obs.position.x, ry - obs.position.y) < (obs.width + 80f)
            }

            if (distToStart > safeRadius && !overlapsObstacle) {
                return Position(rx, ry)
            }
            attempt++
        }
        return Position(Random.nextFloat() * 3800f + 600f, Random.nextFloat() * 3800f + 600f)
    }

    // --- GAME LOOP Y MOVIMIENTO EN CORRUTINA ---

    private fun startGameLoop() {
        stopGameLoop()
        gameLoopJob = viewModelScope.launch {
            while (isActive && _uiState.value.gameState == GameState.PLAYING) {
                val delayMs = _uiState.value.speedMs
                delay(delayMs)
                performMovementTick()
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun performMovementTick() {
        _uiState.update { state ->
            if (state.gameState != GameState.PLAYING) return@update state

            val validDir = if (isOppositeDirection(state.requestedDirection, state.direction)) {
                state.direction
            } else {
                state.requestedDirection
            }

            val head = state.snakeSegments.first()
            val newHeadPos = Position(
                x = head.position.x + validDir.dx * GameUiState.STEP_DISTANCE,
                y = head.position.y + validDir.dy * GameUiState.STEP_DISTANCE
            )

            if (!state.isInvulnerable && checkFatalCollisions(newHeadPos, state)) {
                processDamageTaken(state)
                return@update state.copy(
                    stats = state.stats.copy(lives = (state.stats.lives - 1).coerceAtLeast(0))
                )
            }

            val newHeadSegment = SnakeSegment(position = newHeadPos, angle = calculateAngle(validDir))

            var eatenFood: Food? = null
            for (food in state.foods) {
                val distance = hypot(newHeadPos.x - food.position.x, newHeadPos.y - food.position.y)
                if (distance < GameUiState.EAT_COLLISION_RADIUS) {
                    eatenFood = food
                    break
                }
            }

            val pendingGrowth = state.pendingGrowthSegments + (eatenFood?.type?.growthAmount ?: 0)
            val shouldGrow = pendingGrowth > 0
            val updatedPendingGrowth = if (shouldGrow) pendingGrowth - 1 else 0

            val newSegments = mutableListOf(newHeadSegment)
            val endIdx = if (shouldGrow) state.snakeSegments.size else state.snakeSegments.size - 1
            for (i in 0 until endIdx) {
                val seg = state.snakeSegments[i]
                newSegments.add(SnakeSegment(position = seg.position, angle = seg.angle))
            }

            var newItemsEaten = state.itemsEaten
            var newPoints = state.stats.points
            var newCoins = state.stats.coins
            var newSpeedMs = state.speedMs
            val remainingFoods = state.foods.toMutableList()

            if (eatenFood != null) {
                remainingFoods.remove(eatenFood)
                newItemsEaten += 1
                newPoints += eatenFood.type.points
                newCoins += eatenFood.type.coins

                val speedReduction = (newItemsEaten / 5) * 15L
                val calculatedSpeed = GameUiState.INITIAL_SPEED_MS - speedReduction
                newSpeedMs = maxOf(GameUiState.MIN_SPEED_MS, calculatedSpeed)
            }

            val replenishedFoods = spawnItemsIfNeeded(remainingFoods, newSegments, state.currentLevel)

            state.copy(
                direction = validDir,
                snakeSegments = newSegments,
                cameraPosition = newHeadPos,
                foods = replenishedFoods,
                itemsEaten = newItemsEaten,
                pendingGrowthSegments = updatedPendingGrowth,
                speedMs = newSpeedMs,
                stats = state.stats.copy(points = newPoints, coins = newCoins)
            )
        }
    }

    private fun checkFatalCollisions(newHeadPos: Position, state: GameUiState): Boolean {
        val level = state.currentLevel

        if (newHeadPos.x < 0f || newHeadPos.x > level.worldWidth ||
            newHeadPos.y < 0f || newHeadPos.y > level.worldHeight) {
            return true
        }

        val bodySegmentsToTest = state.snakeSegments.drop(3)
        for (segment in bodySegmentsToTest) {
            val distToBody = hypot(newHeadPos.x - segment.position.x, newHeadPos.y - segment.position.y)
            if (distToBody < GameUiState.BODY_COLLISION_RADIUS) {
                return true
            }
        }

        for (obstacle in state.obstacles) {
            val distToObstacle = hypot(newHeadPos.x - obstacle.position.x, newHeadPos.y - obstacle.position.y)
            val collisionThreshold = (obstacle.width / 2f) + 16f
            if (distToObstacle < collisionThreshold) {
                return true
            }
        }

        return false
    }

    private fun processDamageTaken(currentState: GameUiState) {
        val remainingLives = currentState.stats.lives - 1

        if (remainingLives <= 0) {
            triggerGameOver()
        } else {
            _uiState.update { it.copy(isInvulnerable = true) }
            invulnerabilityJob?.cancel()
            invulnerabilityJob = viewModelScope.launch {
                delay(GameUiState.INVULNERABILITY_DURATION_MS)
                _uiState.update { it.copy(isInvulnerable = false) }
            }
        }
    }

    private fun generateInitialItems(segments: List<SnakeSegment>): List<Food> {
        val list = mutableListOf<Food>()
        val level = WorldLevel()
        return spawnItemsIfNeeded(list, segments, level)
    }

    private fun spawnItemsIfNeeded(
        currentFoods: List<Food>,
        segments: List<SnakeSegment>,
        level: WorldLevel
    ): List<Food> {
        val result = currentFoods.toMutableList()
        val targetCount = GameUiState.TARGET_FOOD_COUNT

        while (result.size < targetCount) {
            val randomPos = generateSafeRandomPosition(segments, result, level)
            val randomItemType = selectRandomItemType()
            val foodId = "item_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
            result.add(Food(id = foodId, position = randomPos, type = randomItemType))
        }

        return result
    }

    private fun generateSafeRandomPosition(
        segments: List<SnakeSegment>,
        existingFoods: List<Food>,
        level: WorldLevel
    ): Position {
        val minX = 100f
        val maxX = level.worldWidth - 100f
        val minY = 100f
        val maxY = level.worldHeight - 100f
        val minSafeDistance = 70f

        var attempt = 0
        while (attempt < 100) {
            val rx = Random.nextFloat() * (maxX - minX) + minX
            val ry = Random.nextFloat() * (maxY - minY) + minY
            val candidate = Position(rx, ry)

            val overlapsSegment = segments.any { seg ->
                hypot(candidate.x - seg.position.x, candidate.y - seg.position.y) < minSafeDistance
            }

            val overlapsFood = existingFoods.any { food ->
                hypot(candidate.x - food.position.x, candidate.y - food.position.y) < minSafeDistance
            }

            if (!overlapsSegment && !overlapsFood) {
                return candidate
            }
            attempt++
        }

        return Position(Random.nextFloat() * (maxX - minX) + minX, Random.nextFloat() * (maxY - minY) + minY)
    }

    private fun selectRandomItemType(): ItemType {
        val chance = Random.nextInt(100)
        return when {
            chance < 60 -> ItemType.FRUIT
            chance < 75 -> ItemType.BERRY
            chance < 85 -> ItemType.MUSHROOM
            chance < 93 -> ItemType.PEARL
            chance < 97 -> ItemType.BATTERY
            chance < 99 -> ItemType.COIN
            else -> ItemType.CRYSTAL
        }
    }

    private fun isOppositeDirection(dirA: Direction, dirB: Direction): Boolean {
        return (dirA == Direction.UP && dirB == Direction.DOWN) ||
                (dirA == Direction.DOWN && dirB == Direction.UP) ||
                (dirA == Direction.LEFT && dirB == Direction.RIGHT) ||
                (dirA == Direction.RIGHT && dirB == Direction.LEFT)
    }

    private fun calculateAngle(direction: Direction): Float {
        return when (direction) {
            Direction.RIGHT -> 0f
            Direction.DOWN -> 90f
            Direction.LEFT -> 180f
            Direction.UP -> 270f
        }
    }

    override fun onCleared() {
        stopGameLoop()
        invulnerabilityJob?.cancel()
        super.onCleared()
    }
}
