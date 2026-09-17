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

@Composable
fun BombGameScreen(onExit: () -> Unit, onGameOver: (Int) -> Unit) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var bombPosition by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var timeLeft by remember { mutableFloatStateOf(10f) }
    var isGameOver by remember { mutableStateOf(false) }

    LaunchedEffect(isGameOver) {
        while (isActive && !isGameOver) {
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

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFF0F0A21))) {
            Canvas(modifier = Modifier.fillMaxSize().clickable { 
                if (!isGameOver) {
                    score += 10
                    timeLeft = (timeLeft + 1f).coerceAtMost(10f)
                    bombPosition = Offset(Random.nextFloat(), Random.nextFloat())
                    GlowSoundManager.playGameAction(context)
                    GlowHapticManager.vibrateImpact(context)
                }
            }) {
                drawCircle(Color.Red, radius = (20 + timeLeft * 5).dp.toPx(), center = Offset(bombPosition.x * size.width, bombPosition.y * size.height))
                drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(bombPosition.x * size.width, bombPosition.y * size.height))
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text("SCORE: $score", color = Color.White, fontWeight = FontWeight.Bold)
                Text("TIME: ${"%.1f".format(timeLeft)}", color = if (timeLeft < 3) Color.Red else Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }

            if (isGameOver) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BOMB EXPLODED!", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Text("Final Score: $score", color = Color.White, fontSize = 20.sp)
                        Button(onClick = { score = 0; timeLeft = 10f; isGameOver = false }, colors = ButtonDefaults.buttonColors(containerColor = NeonLime)) { Text("DEFUSE AGAIN", color = Color.Black) }
                        TextButton(onClick = onExit) { Text("EXIT", color = Color.White) }
                    }
                }
            }
        }
    }
}
