package com.example.glowink.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.*
import com.example.glowink.util.GlowHapticManager
import com.example.glowink.util.GlowSoundManager
import kotlinx.coroutines.isActive
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RushGameScreen(onExit: () -> Unit, onGameOver: (Int) -> Unit) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var gameResetCounter by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var playerLane by remember { mutableIntStateOf(1) }
    val obstacles = remember { mutableStateListOf<Offset>() }
    var isGameOver by remember { mutableStateOf(false) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }
    var roadScrollOffset by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        onDispose {
            isPlaying = false
            obstacles.clear()
        }
    }

    LaunchedEffect(isPlaying, gameResetCounter, isGameOver) {
        if (!isPlaying || isGameOver) return@LaunchedEffect
        lastFrameTime = 0L

        while (isActive && isPlaying && !isGameOver) {
            withFrameMillis { time ->
                if (lastFrameTime == 0L) lastFrameTime = time
                val delta = (time - lastFrameTime) / 1000f
                lastFrameTime = time

                val roadSpeed = 0.8f + (score / 400f)
                roadScrollOffset = (roadScrollOffset + delta * roadSpeed) % 1.0f

                if (obstacles.isEmpty() || obstacles.last().y > 0.35f) {
                    obstacles.add(Offset(Random.nextInt(3).toFloat(), -0.15f))
                }

                val it = obstacles.iterator()
                while (it.hasNext()) {
                    val o = it.next()
                    val newY = o.y + delta * (0.55f + score / 500f)
                    if (newY > 1.15f) {
                        it.remove()
                        score += 5
                    } else {
                        if (newY > 0.76f && newY < 0.92f && o.x.toInt() == playerLane) {
                            isGameOver = true
                            onGameOver(score)
                            GlowSoundManager.playVictory(context)
                            GlowHapticManager.vibrateError(context)
                            break
                        }
                    }
                }
            }
        }
    }

    fun restartGame() {
        obstacles.clear()
        score = 0
        playerLane = 1
        roadScrollOffset = 0f
        isGameOver = false
        gameResetCounter++
    }

    fun exitGame() {
        isPlaying = false
        obstacles.clear()
        onExit()
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        .clickable { exitGame() }
                        .padding(8.dp)
                )
                Text("CYBER RUSH 🏎️", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("SCORE: $score", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            if (dragAmount.x > 35) playerLane = (playerLane + 1).coerceAtMost(2)
                            else if (dragAmount.x < -35) playerLane = (playerLane - 1).coerceAtLeast(0)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                    val w = size.width
                    val h = size.height
                    val laneW = w / 3f

                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF070414), Color(0xFF0F0A28), Color(0xFF070414))
                        )
                    )

                    drawLine(ElectricCyan.copy(alpha = 0.8f), Offset(2f, 0f), Offset(2f, h), strokeWidth = 4f)
                    drawLine(ElectricCyan.copy(alpha = 0.8f), Offset(w - 2f, 0f), Offset(w - 2f, h), strokeWidth = 4f)

                    val dashHeight = h * 0.08f
                    val dashGap = h * 0.06f
                    val totalUnit = dashHeight + dashGap
                    val startY = (roadScrollOffset * totalUnit) - totalUnit

                    var currentY = startY
                    while (currentY < h + totalUnit) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(laneW, currentY),
                            end = Offset(laneW, currentY + dashHeight),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(laneW * 2f, currentY),
                            end = Offset(laneW * 2f, currentY + dashHeight),
                            strokeWidth = 3f
                        )
                        currentY += totalUnit
                    }

                    val carX = playerLane * laneW + laneW * 0.15f
                    val carY = 0.82f * h
                    val carW = laneW * 0.70f
                    val carH = h * 0.10f

                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.4f),
                        radius = carW * 0.6f,
                        center = Offset(carX + carW / 2f, carY + carH * 0.8f)
                    )
                    drawRoundRect(
                        color = Color(0xFF140D2A),
                        topLeft = Offset(carX, carY),
                        size = Size(carW, carH),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = ElectricCyan,
                        topLeft = Offset(carX, carY),
                        size = Size(carW, carH),
                        cornerRadius = CornerRadius(12f, 12f),
                        style = Stroke(width = 3f)
                    )
                    drawRoundRect(
                        color = NeonLime,
                        topLeft = Offset(carX + carW * 0.2f, carY + carH * 0.25f),
                        size = Size(carW * 0.6f, carH * 0.35f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    obstacles.forEach { o ->
                        val obsX = o.x * laneW + laneW * 0.18f
                        val obsY = o.y * h
                        val obsW = laneW * 0.64f
                        val obsH = h * 0.09f

                        drawRoundRect(
                            color = Color(0xFF3A0000),
                            topLeft = Offset(obsX, obsY),
                            size = Size(obsW, obsH),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = Color.Red,
                            topLeft = Offset(obsX, obsY),
                            size = Size(obsW, obsH),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 3f)
                        )
                        drawCircle(Color.Red, radius = 4f, center = Offset(obsX + obsW * 0.25f, obsY + obsH * 0.8f))
                        drawCircle(Color.Red, radius = 4f, center = Offset(obsX + obsW * 0.75f, obsY + obsH * 0.8f))
                    }
                }

                if (isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💥 ¡CHOQUE DETECTADO!", color = Color.Red, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Score: $score", color = Color.White, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { restartGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = UltravioletPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("REINTENTAR CARRERA", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { exitGame() }) {
                                Text("SALIR AL MENÚ", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
