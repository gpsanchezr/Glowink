package com.example.glowink.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun RunGameScreen(onExit: () -> Unit, onGameOver: (Int) -> Unit) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var playerY by remember { mutableFloatStateOf(0.8f) }
    var playerVelocity by remember { mutableFloatStateOf(0f) }
    var obstacles = remember { mutableStateListOf<Offset>() }
    var isGameOver by remember { mutableStateOf(false) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive && !isGameOver) {
            withFrameMillis { time ->
                if (lastFrameTime == 0L) lastFrameTime = time
                val delta = (time - lastFrameTime) / 1000f
                lastFrameTime = time

                // Physics
                playerVelocity += delta * 2.5f // Gravity
                playerY = (playerY + playerVelocity * delta).coerceIn(0.2f, 0.8f)
                if (playerY >= 0.8f) playerVelocity = 0f

                // Update Obstacles
                if (obstacles.isEmpty() || obstacles.last().x < 0.6f) {
                    obstacles.add(Offset(1.1f, 0.8f))
                }
                
                val it = obstacles.iterator()
                while (it.hasNext()) {
                    val o = it.next()
                    val newX = o.x - delta * (0.6f + score / 1000f)
                    if (newX < -0.1f) {
                        it.remove()
                        score += 10
                    } else {
                        // Collision check
                        if (kotlin.math.abs(newX - 0.2f) < 0.05f && playerY > 0.7f) {
                            isGameOver = true
                            onGameOver(score)
                            GlowSoundManager.playVictory(context)
                            GlowHapticManager.vibrateError(context)
                        }
                    }
                }
            }
        }
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFF070514)).clickable { 
            if (playerY >= 0.79f) {
                playerVelocity = -1.2f 
                GlowSoundManager.playGameAction(context)
            }
        }) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Ground
                drawLine(NeonLime, Offset(0f, 0.82f * h), Offset(w, 0.82f * h), 4f)

                // Player
                drawRect(ElectricCyan, Offset(0.2f * w - 15.dp.toPx(), playerY * h - 30.dp.toPx()), Size(30.dp.toPx(), 30.dp.toPx()))

                // Obstacles
                obstacles.forEach { o ->
                    drawRect(Color.Red, Offset(o.x * w - 10.dp.toPx(), o.y * h - 25.dp.toPx()), Size(20.dp.toPx(), 25.dp.toPx()))
                }
            }

            Text("SCORE: $score", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

            if (isGameOver) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RUN ENDED", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Text("Score: $score", color = Color.White, fontSize = 20.sp)
                        Button(onClick = { obstacles.clear(); score = 0; playerY = 0.8f; isGameOver = false; lastFrameTime = 0L }, colors = ButtonDefaults.buttonColors(containerColor = NeonLime)) { Text("RUN AGAIN", color = Color.Black) }
                        TextButton(onClick = onExit) { Text("EXIT", color = Color.White) }
                    }
                }
            }
        }
    }
}
