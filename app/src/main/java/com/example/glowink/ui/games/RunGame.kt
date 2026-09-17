package com.example.glowink.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun RunGameScreen(onExit: () -> Unit, onGameOver: (Int) -> Unit) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var gameResetCounter by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var playerY by remember { mutableFloatStateOf(0.8f) }
    var playerVelocity by remember { mutableFloatStateOf(0f) }
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

                // Physics
                playerVelocity += delta * 2.8f
                playerY = (playerY + playerVelocity * delta).coerceIn(0.2f, 0.8f)
                if (playerY >= 0.8f) playerVelocity = 0f

                // Update Obstacles
                if (obstacles.isEmpty() || obstacles.last().x < 0.55f) {
                    obstacles.add(Offset(1.1f, 0.8f))
                }

                val it = obstacles.iterator()
                while (it.hasNext()) {
                    val o = it.next()
                    val newX = o.x - delta * (0.65f + score / 800f)
                    if (newX < -0.1f) {
                        it.remove()
                        score += 10
                    } else {
                        // Collision check
                        if (kotlin.math.abs(newX - 0.2f) < 0.05f && playerY > 0.72f) {
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
        playerY = 0.8f
        playerVelocity = 0f
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
                .background(Color(0xFF070514))
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
                Text("GLOW RUNNER 🏃", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("SCORE: $score", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .clickable {
                        if (!isGameOver && playerY >= 0.79f) {
                            playerVelocity = -1.35f
                            GlowSoundManager.playGameAction(context)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                    val w = size.width
                    val h = size.height

                    drawLine(NeonLime, Offset(0f, 0.82f * h), Offset(w, 0.82f * h), 4f)

                    drawRect(
                        ElectricCyan,
                        Offset(0.2f * w - 15.dp.toPx(), playerY * h - 30.dp.toPx()),
                        Size(30.dp.toPx(), 30.dp.toPx())
                    )

                    obstacles.forEach { o ->
                        drawRect(
                            Color.Red,
                            Offset(o.x * w - 10.dp.toPx(), o.y * h - 25.dp.toPx()),
                            Size(20.dp.toPx(), 25.dp.toPx())
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
                            Text("FIN DE LA CARRERA", color = Color.Red, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Score: $score", color = Color.White, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { restartGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonLime)
                            ) {
                                Text("CORRER DE NUEVO", color = Color.Black, fontWeight = FontWeight.Bold)
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
