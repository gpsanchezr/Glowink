package com.example.glowink.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.viewmodel.BackgroundStyle
import com.example.glowink.ui.viewmodel.Direction
import com.example.glowink.ui.viewmodel.ItemType
import com.example.glowink.ui.viewmodel.SnakeViewModel
import com.example.glowink.ui.viewmodel.WorldType
import kotlin.math.abs
import kotlin.math.sin

/**
 * Pantalla de Gameplay con Canvas 2D, Cámara Fluida, Entorno Procedural (Bosque Neón & Abismo Glow) y D-Pad (GDD - FASES 3 A 8).
 * Incluye renderizado de árboles, hongos, corales bioluminiscentes, ruinas marinas, medusas flotantes y burbujas.
 */
@Composable
fun SnakeGameScreen(
    viewModel: SnakeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // 1. Interpolación suave de cámara (lerp factor 0.2f)
    val headPos = uiState.snakeSegments.firstOrNull()?.position ?: uiState.cameraPosition
    val cameraX = remember { mutableFloatStateOf(headPos.x) }
    val cameraY = remember { mutableFloatStateOf(headPos.y) }

    LaunchedEffect(headPos) {
        cameraX.floatValue += (headPos.x - cameraX.floatValue) * 0.2f
        cameraY.floatValue += (headPos.y - cameraY.floatValue) * 0.2f
    }

    val primaryColor = Color(uiState.snakeColor.hexColor)
    val glowColor = Color(uiState.snakeColor.glowHex)

    // Modulación del parpadeo y animaciones de criaturas
    val timeMillis = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(uiState.isInvulnerable, uiState.creatures.isNotEmpty()) {
        while (uiState.gameState == com.example.glowink.ui.viewmodel.GameState.PLAYING) {
            timeMillis.longValue = System.currentTimeMillis()
            kotlinx.coroutines.delay(80L)
        }
    }

    val isBlinkPhase = uiState.isInvulnerable && ((timeMillis.longValue / 150) % 2 == 0L)
    val blinkAlphaMultiplier = if (isBlinkPhase) 0.22f else 1.0f

    // Gestos Swipe
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (uiState.currentLevel.worldType == WorldType.ABISMO_GLOW) Color(0xFF030A1C) else Color(0xFF070414)
            )
    ) {
        // LAYER 1: CANVAS DEL MUNDO MASIVO
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount.x
                            dragOffsetY += dragAmount.y
                        },
                        onDragEnd = {
                            val minSwipeDistance = 35f
                            if (abs(dragOffsetX) > abs(dragOffsetY)) {
                                if (dragOffsetX > minSwipeDistance) {
                                    viewModel.updateDirection(Direction.RIGHT)
                                } else if (dragOffsetX < -minSwipeDistance) {
                                    viewModel.updateDirection(Direction.LEFT)
                                }
                            } else {
                                if (dragOffsetY > minSwipeDistance) {
                                    viewModel.updateDirection(Direction.DOWN)
                                } else if (dragOffsetY < -minSwipeDistance) {
                                    viewModel.updateDirection(Direction.UP)
                                }
                            }
                        }
                    )
                }
        ) {
            val screenWidth = constraints.maxWidth.toFloat()
            val screenHeight = constraints.maxHeight.toFloat()

            // Translocación del Canvas
            val translateX = screenWidth / 2f - cameraX.floatValue
            val translateY = screenHeight / 2f - cameraY.floatValue

            Canvas(modifier = Modifier.fillMaxSize()) {
                withTransform({
                    translate(translateX, translateY)
                }) {
                    val worldWidth = uiState.currentLevel.worldWidth
                    val worldHeight = uiState.currentLevel.worldHeight

                    // A) TEXTURA Y SUELO DEL MUNDO
                    if (uiState.currentLevel.worldType == WorldType.ABISMO_GLOW) {
                        drawCircle(
                            color = Color(0xFF001F3F).copy(alpha = 0.35f),
                            radius = 900f,
                            center = Offset(2500f, 2500f)
                        )
                    } else if (uiState.backgroundStyle == BackgroundStyle.SIMPLE) {
                        drawCircle(
                            color = Color(0xFF0D2808).copy(alpha = 0.25f),
                            radius = 600f,
                            center = Offset(1200f, 1200f)
                        )
                    }

                    // B) RETÍCULA NEÓN (GRID REFORZADA 20% ALPHA)
                    if (uiState.backgroundStyle == BackgroundStyle.GRID) {
                        val cellSize = 100f
                        val gridColor = if (uiState.currentLevel.worldType == WorldType.ABISMO_GLOW) {
                            ElectricCyan.copy(alpha = 0.20f)
                        } else {
                            primaryColor.copy(alpha = 0.20f)
                        }

                        var x = 0f
                        while (x <= worldWidth) {
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, worldHeight),
                                strokeWidth = 2f
                            )
                            x += cellSize
                        }

                        var y = 0f
                        while (y <= worldHeight) {
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(worldWidth, y),
                                strokeWidth = 2f
                            )
                            y += cellSize
                        }
                    }

                    // C) BORDES PERIMETRALES DEL MUNDO
                    val borderColor = if (uiState.currentLevel.worldType == WorldType.ABISMO_GLOW) ElectricCyan else primaryColor
                    drawRect(
                        color = borderColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(worldWidth, worldHeight),
                        style = Stroke(width = 8f)
                    )

                    drawRect(
                        color = glowColor.copy(alpha = 0.35f),
                        topLeft = Offset(-6f, -6f),
                        size = Size(worldWidth + 12f, worldHeight + 12f),
                        style = Stroke(width = 16f)
                    )

                    // D) RENDERIZADO PROCEDURAL DE OBSTÁCULOS VIBRANTES (BOSQUE Y ABISMO GLOW)
                    uiState.obstacles.forEach { obstacle ->
                        val pos = Offset(obstacle.position.x, obstacle.position.y)
                        val radius = obstacle.width / 2f

                        when (obstacle.shapeType) {
                            "TREE" -> {
                                // Árbol Bioluminiscente Verde Neón Vibrante
                                drawCircle(color = NeonLime.copy(alpha = 0.35f), radius = radius * 1.35f, center = pos)
                                drawCircle(color = Color(0xFF0C240A), radius = radius, center = pos)
                                drawCircle(color = NeonLime, radius = radius, center = pos, style = Stroke(width = 6f))
                                drawCircle(color = Color(0xFF1B4D16), radius = radius * 0.65f, center = pos)
                            }
                            "MUSHROOM" -> {
                                // Hongo Neón Magenta + Tallo Cyan
                                val capRadius = radius * 0.9f
                                drawRect(color = ElectricCyan, topLeft = Offset(pos.x - 7f, pos.y), size = Size(14f, capRadius * 1.1f))
                                drawArc(color = Color(0xFFFF007F).copy(alpha = 0.4f), startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(pos.x - capRadius * 1.2f, pos.y - capRadius * 1.2f), size = Size(capRadius * 2.4f, capRadius * 2.4f), style = Stroke(width = 8f))
                                drawArc(color = Color(0xFFFF007F), startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(pos.x - capRadius, pos.y - capRadius), size = Size(capRadius * 2f, capRadius * 2f))
                                drawCircle(color = Color.White, radius = 4f, center = Offset(pos.x - capRadius * 0.4f, pos.y - capRadius * 0.4f))
                                drawCircle(color = Color.White, radius = 4f, center = Offset(pos.x + capRadius * 0.4f, pos.y - capRadius * 0.4f))
                                drawCircle(color = Color.White, radius = 4.5f, center = Offset(pos.x, pos.y - capRadius * 0.7f))
                            }
                            "CORAL" -> {
                                drawCircle(color = Color(0xFF003D66), radius = radius, center = pos)
                                drawCircle(color = ElectricCyan.copy(alpha = 0.35f), radius = radius * 1.35f, center = pos, style = Stroke(width = 8f))
                                drawCircle(color = ElectricCyan, radius = radius, center = pos, style = Stroke(width = 4f))
                                drawCircle(color = Color(0xFF007FFF), radius = radius * 0.6f, center = Offset(pos.x - 8f, pos.y - 8f))
                            }
                            "RUIN" -> {
                                val topLeft = Offset(pos.x - obstacle.width / 2f, pos.y - obstacle.height / 2f)
                                val size = Size(obstacle.width, obstacle.height)
                                drawRoundRect(color = Color(0xFF3B2E0B), topLeft = topLeft, size = size, cornerRadius = CornerRadius(12f, 12f))
                                drawRoundRect(color = Color(0xFFFFD700), topLeft = topLeft, size = size, cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 4f))
                                drawLine(color = Color(0xFFFFD700), start = Offset(topLeft.x, pos.y), end = Offset(topLeft.x + obstacle.width, pos.y), strokeWidth = 3f)
                            }
                        }
                    }

                    // E) CRIATURAS AMBIENTALES (LUCIÉRNAGAS, MEDUSAS Y BURBUJAS)
                    uiState.creatures.forEach { creature ->
                        val pos = Offset(creature.position.x, creature.position.y)
                        val seed = creature.id.hashCode()

                        when (creature.type) {
                            "FIREFLY" -> {
                                val flickerAlpha = 0.35f + 0.65f * abs(sin((timeMillis.longValue + seed) / 180f))
                                drawCircle(color = Color(0xFFFFD24C).copy(alpha = 0.5f * flickerAlpha), radius = 16f, center = pos)
                                drawCircle(color = Color(0xFFFFFFB0).copy(alpha = flickerAlpha), radius = 6f, center = pos)
                            }
                            "JELLYFISH" -> {
                                // Medusa Rosada Flotante
                                val swayX = sin((timeMillis.longValue + seed) / 300f) * 12f
                                val jellyPos = Offset(pos.x + swayX, pos.y)
                                val capRadius = 18f

                                // Sombrero Rosa Magenta
                                drawArc(
                                    color = Color(0xFFFF007F).copy(alpha = 0.8f),
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = true,
                                    topLeft = Offset(jellyPos.x - capRadius, jellyPos.y - capRadius),
                                    size = Size(capRadius * 2f, capRadius * 2f)
                                )

                                // Tentáculos
                                for (t in -2..2) {
                                    val tentacleX = jellyPos.x + (t * 6f)
                                    val tentacleSway = sin((timeMillis.longValue + seed + t * 50) / 200f) * 5f
                                    drawLine(
                                        color = Color(0xFFFF4FD8).copy(alpha = 0.7f),
                                        start = Offset(tentacleX, jellyPos.y),
                                        end = Offset(tentacleX + tentacleSway, jellyPos.y + 22f),
                                        strokeWidth = 2f
                                    )
                                }
                            }
                            "BUBBLE" -> {
                                // Burbujas Submarinas Ascendentes
                                val riseY = (pos.y - ((timeMillis.longValue / 20) + seed) % 2000f + 2000f) % 2000f
                                val bubblePos = Offset(pos.x + sin((riseY + seed) / 100f) * 8f, riseY)
                                drawCircle(
                                    color = Color(0xAA80F0FF),
                                    radius = 7f,
                                    center = bubblePos,
                                    style = Stroke(width = 1.8f)
                                )
                            }
                        }
                    }

                    // F) RENDERIZADO DE OBJETOS Y COMIDA NEÓN
                    uiState.foods.forEach { food ->
                        val pos = Offset(food.position.x, food.position.y)
                        val itemColor = Color(food.type.colorHex)

                        val outerRadius = when (food.type) {
                            ItemType.CRYSTAL -> 28f
                            ItemType.COIN, ItemType.PEARL -> 24f
                            else -> 20f
                        }

                        val innerRadius = when (food.type) {
                            ItemType.CRYSTAL -> 16f
                            ItemType.COIN, ItemType.PEARL -> 13f
                            else -> 10f
                        }

                        drawCircle(color = itemColor.copy(alpha = 0.35f), radius = outerRadius, center = pos)
                        drawCircle(color = itemColor.copy(alpha = 0.65f), radius = outerRadius * 0.7f, center = pos)
                        drawCircle(color = Color.White, radius = innerRadius * 0.5f, center = pos)
                        drawCircle(color = itemColor, radius = innerRadius, center = pos, style = Stroke(width = 3f))
                    }

                    // G) SERPIENTE NEÓN (Segmentos + Cabeza)
                    val segments = uiState.snakeSegments
                    val totalSegments = segments.size.coerceAtLeast(1)

                    for (i in segments.indices.reversed()) {
                        val segment = segments[i]
                        val isHead = (i == 0)
                        val pos = Offset(segment.position.x, segment.position.y)

                        val t = i.toFloat() / totalSegments
                        val baseRadius = if (isHead) 24f else (20f * (1f - t * 0.35f)).coerceAtLeast(10f)

                        drawCircle(
                            color = glowColor.copy(alpha = (if (isHead) 0.45f else 0.22f) * blinkAlphaMultiplier),
                            radius = baseRadius * 1.9f,
                            center = pos
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.65f * blinkAlphaMultiplier),
                            radius = baseRadius * 1.35f,
                            center = pos
                        )
                        drawCircle(
                            color = (if (isHead) Color.White else primaryColor).copy(alpha = blinkAlphaMultiplier),
                            radius = baseRadius,
                            center = pos
                        )

                        if (isHead) {
                            drawSnakeEyes(
                                headPos = pos,
                                direction = uiState.direction,
                                headRadius = baseRadius,
                                alpha = blinkAlphaMultiplier
                            )
                        }
                    }
                }
            }
        }

        // LAYER 2: CONTROLES D-PAD NEÓN
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            NeonDPadControl(
                neonColor = primaryColor,
                onDirectionClick = { dir -> viewModel.updateDirection(dir) }
            )
        }
    }
}

/**
 * Componente D-Pad Futurista en Cruz con Botones Independientes Neón y Feedback Táctil.
 */
@Composable
fun NeonDPadControl(
    neonColor: Color = ElectricCyan,
    onDirectionClick: (Direction) -> Unit
) {
    val buttonSize = 58.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NeonDirectionButton(
            direction = Direction.UP,
            neonColor = neonColor,
            size = buttonSize,
            onClick = { onDirectionClick(Direction.UP) }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeonDirectionButton(
                direction = Direction.LEFT,
                neonColor = neonColor,
                size = buttonSize,
                onClick = { onDirectionClick(Direction.LEFT) }
            )

            NeonDirectionButton(
                direction = Direction.RIGHT,
                neonColor = neonColor,
                size = buttonSize,
                onClick = { onDirectionClick(Direction.RIGHT) }
            )
        }

        NeonDirectionButton(
            direction = Direction.DOWN,
            neonColor = neonColor,
            size = buttonSize,
            onClick = { onDirectionClick(Direction.DOWN) }
        )
    }
}

@Composable
private fun NeonDirectionButton(
    direction: Direction,
    neonColor: Color,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleAnim by animateFloatAsState(targetValue = if (isPressed) 0.88f else 1f, label = "DPadPress")

    Box(
        modifier = Modifier
            .size(size)
            .scale(scaleAnim)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xCC150E30))
            .border(1.8.dp, if (isPressed) Color.White else neonColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val w = this.size.width
            val h = this.size.height
            val color = if (isPressed) Color.White else neonColor

            val path = Path()
            when (direction) {
                Direction.UP -> {
                    path.moveTo(w * 0.5f, 0f)
                    path.lineTo(w, h)
                    path.lineTo(0f, h)
                    path.close()
                }
                Direction.DOWN -> {
                    path.moveTo(0f, 0f)
                    path.lineTo(w, 0f)
                    path.lineTo(w * 0.5f, h)
                    path.close()
                }
                Direction.LEFT -> {
                    path.moveTo(w, 0f)
                    path.lineTo(w, h)
                    path.lineTo(0f, h * 0.5f)
                    path.close()
                }
                Direction.RIGHT -> {
                    path.moveTo(0f, 0f)
                    path.lineTo(w, h * 0.5f)
                    path.lineTo(0f, h)
                    path.close()
                }
            }
            drawPath(path = path, color = color)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSnakeEyes(
    headPos: Offset,
    direction: Direction,
    headRadius: Float,
    alpha: Float = 1f
) {
    val eyeOffsetForward = headRadius * 0.45f
    val eyeOffsetSide = headRadius * 0.4f
    val eyeRadius = 3.5f

    val (leftEye, rightEye) = when (direction) {
        Direction.RIGHT -> Pair(
            Offset(headPos.x + eyeOffsetForward, headPos.y - eyeOffsetSide),
            Offset(headPos.x + eyeOffsetForward, headPos.y + eyeOffsetSide)
        )
        Direction.LEFT -> Pair(
            Offset(headPos.x - eyeOffsetForward, headPos.y - eyeOffsetSide),
            Offset(headPos.x - eyeOffsetForward, headPos.y + eyeOffsetSide)
        )
        Direction.UP -> Pair(
            Offset(headPos.x - eyeOffsetSide, headPos.y - eyeOffsetForward),
            Offset(headPos.x + eyeOffsetSide, headPos.y - eyeOffsetForward)
        )
        Direction.DOWN -> Pair(
            Offset(headPos.x - eyeOffsetSide, headPos.y + eyeOffsetForward),
            Offset(headPos.x + eyeOffsetSide, headPos.y + eyeOffsetForward)
        )
    }

    drawCircle(color = Color.Black.copy(alpha = alpha), radius = eyeRadius * 1.5f, center = leftEye)
    drawCircle(color = Color(0xFF39FF14).copy(alpha = alpha), radius = eyeRadius, center = leftEye)

    drawCircle(color = Color.Black.copy(alpha = alpha), radius = eyeRadius * 1.5f, center = rightEye)
    drawCircle(color = Color(0xFF39FF14).copy(alpha = alpha), radius = eyeRadius, center = rightEye)
}
