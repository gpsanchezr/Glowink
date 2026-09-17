package com.example.glowink.ui.games

import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
    var playerLane by remember { mutableIntStateOf(1) } // 0, 1, 2
    val obstacles = remember { mutableStateListOf<Offset>() }
    var isGameOver by remember { mutableStateOf(false) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }

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

                // Update Obstacles
                if (obstacles.isEmpty() || obstacles.last().y > 0.35f) {
                    obstacles.add(Offset(Random.nextInt(3).toFloat(), -0.1f))
                }

                val it = obstacles.iterator()
                while (it.hasNext()) {
                    val o = it.next()
                    val newY = o.y + delta * (0.55f + score / 500f)
                    if (newY > 1.1f) {
                        it.remove()
                        score += 5
                    } else {
                        // Collision check
                        if (newY > 0.78f && newY < 0.92f && o.x.toInt() == playerLane) {
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
            // Top Bar
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
                Text("CYBER RUSH", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("SCORE: $score", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            if (dragAmount.x > 40) playerLane = (playerLane + 1).coerceAtMost(2)
                            else if (dragAmount.x < -40) playerLane = (playerLane - 1).coerceAtLeast(0)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                    val w = size.width
                    val h = size.height
                    val laneW = w / 3

                    // Lanes
                    drawLine(Color.White.copy(0.2f), Offset(laneW, 0f), Offset(laneW, h), 2f)
                    drawLine(Color.White.copy(0.2f), Offset(laneW * 2, 0f), Offset(laneW * 2, h), 2f)

                    // Player
                    drawRect(
                        ElectricCyan,
                        Offset(playerLane * laneW + 10.dp.toPx(), 0.85f * h),
                        Size(laneW - 20.dp.toPx(), 40.dp.toPx())
                    )

                    // Obstacles
                    obstacles.forEach { o ->
                        drawRect(
                            Color.Red,
                            Offset(o.x * laneW + 20.dp.toPx(), o.y * h),
                            Size(laneW - 40.dp.toPx(), 30.dp.toPx())
                        )
                    }
                }

                if (isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("¡CHOQUE DETECTADO!", color = Color.Red, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Score: $score", color = Color.White, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { restartGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = UltravioletPurple)
                            ) {
                                Text("REINTENTAR", color = Color.White, fontWeight = FontWeight.Bold)
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
