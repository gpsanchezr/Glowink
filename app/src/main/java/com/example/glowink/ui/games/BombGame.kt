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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.*
import com.example.glowink.util.GlowHapticManager
import com.example.glowink.util.GlowSoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BombGameScreen(onExit: () -> Unit, onGameOver: (Int) -> Unit) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var gameResetCounter by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var bombPosition by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var timeLeft by remember { mutableFloatStateOf(10f) }
    var isGameOver by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            isPlaying = false
        }
    }

    LaunchedEffect(isPlaying, gameResetCounter, isGameOver) {
        if (!isPlaying || isGameOver) return@LaunchedEffect

        while (isActive && isPlaying && !isGameOver) {
            delay(100)
            timeLeft -= 0.1f
            if (timeLeft <= 0) {
                isGameOver = true
                onGameOver(score)
                GlowSoundManager.playVictory(context)
                GlowHapticManager.vibrateError(context)
            }
        }
    }

    fun restartGame() {
        score = 0
        timeLeft = 10f
        bombPosition = Offset(
            Random.nextFloat().coerceIn(0.15f, 0.85f),
            Random.nextFloat().coerceIn(0.15f, 0.85f)
        )
        isGameOver = false
        gameResetCounter++
    }

    fun exitGame() {
        isPlaying = false
        onExit()
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F0A21))
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
                Text("BOMB SQUAD", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("SCORE: $score", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .clickable {
                            if (!isGameOver) {
                                score += 10
                                timeLeft = (timeLeft + 1f).coerceAtMost(10f)
                                bombPosition = Offset(
                                    Random.nextFloat().coerceIn(0.15f, 0.85f),
                                    Random.nextFloat().coerceIn(0.15f, 0.85f)
                                )
                                GlowSoundManager.playGameAction(context)
                                GlowHapticManager.vibrateImpact(context)
                            }
                        }
                ) {
                    drawCircle(
                        Color.Red,
                        radius = (20 + timeLeft * 5).dp.toPx(),
                        center = Offset(bombPosition.x * size.width, bombPosition.y * size.height)
                    )
                    drawCircle(
                        Color.White,
                        radius = 6.dp.toPx(),
                        center = Offset(bombPosition.x * size.width, bombPosition.y * size.height)
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "TIEMPO: ${"%.1f".format(timeLeft.coerceAtLeast(0f))}s",
                        color = if (timeLeft < 3) Color.Red else Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                if (isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("¡BOMBA EXPLOTÓ!", color = Color.Red, fontSize = 30.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Puntaje Final: $score", color = Color.White, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { restartGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonLime)
                            ) {
                                Text("DESACTIVAR DE NUEVO", color = Color.Black, fontWeight = FontWeight.Bold)
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
