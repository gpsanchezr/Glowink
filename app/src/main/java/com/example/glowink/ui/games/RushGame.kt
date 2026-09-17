package com.example.glowink.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun RushGameScreen(onExit: () -> Unit, onGameOver: (Int) -> Unit) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var playerLane by remember { mutableIntStateOf(1) } // 0, 1, 2
    var obstacles = remember { mutableStateListOf<Offset>() }
    var isGameOver by remember { mutableStateOf(false) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive && !isGameOver) {
            withFrameMillis { time ->
                if (lastFrameTime == 0L) lastFrameTime = time
                val delta = (time - lastFrameTime) / 1000f
                lastFrameTime = time

                // Update Obstacles
                if (obstacles.isEmpty() || obstacles.last().y > 0.3f) {
                    obstacles.add(Offset(Random.nextInt(3).toFloat(), -0.1f))
                }
                
                val it = obstacles.iterator()
                while (it.hasNext()) {
                    val o = it.next()
                    val newY = o.y + delta * (0.5f + score / 500f)
                    if (newY > 1.1f) {
                        it.remove()
                        score += 5
                    } else {
                        // Collision check
                        if (newY > 0.8f && newY < 0.95f && o.x.toInt() == playerLane) {
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).pointerInput(Unit) {
            detectDragGestures { _, dragAmount ->
                if (dragAmount.x > 50) playerLane = (playerLane + 1).coerceAtMost(2)
                else if (dragAmount.x < -50) playerLane = (playerLane - 1).coerceAtLeast(0)
            }
        }) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val laneW = w / 3

                // Lanes
                drawLine(Color.White.copy(0.2f), Offset(laneW, 0f), Offset(laneW, h), 2f)
                drawLine(Color.White.copy(0.2f), Offset(laneW * 2, 0f), Offset(laneW * 2, h), 2f)

                // Player
                drawRect(ElectricCyan, Offset(playerLane * laneW + 10.dp.toPx(), 0.85f * h), Size(laneW - 20.dp.toPx(), 40.dp.toPx()))

                // Obstacles
                obstacles.forEach { o ->
                    drawRect(Color.Red, Offset(o.x * laneW + 20.dp.toPx(), o.y * h), Size(laneW - 40.dp.toPx(), 30.dp.toPx()))
                }
            }

            Text("SCORE: $score", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

            if (isGameOver) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CRASHED!", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Text("Score: $score", color = Color.White, fontSize = 20.sp)
                        Button(onClick = { obstacles.clear(); score = 0; isGameOver = false; lastFrameTime = 0L }, colors = ButtonDefaults.buttonColors(containerColor = UltravioletPurple)) { Text("RUSH AGAIN", color = Color.White) }
                        TextButton(onClick = onExit) { Text("EXIT", color = Color.White) }
                    }
                }
            }
        }
    }
}
