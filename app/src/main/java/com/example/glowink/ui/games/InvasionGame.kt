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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
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

private data class Entity(val x: Float, val y: Float, val type: Int)
private data class Bullet(val x: Float, val y: Float)

@Composable
fun InvasionGameScreen(onExit: () -> Unit, onGameOver: (Int) -> Unit) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var playerX by remember { mutableFloatStateOf(0.5f) }
    var enemies = remember { mutableStateListOf<Entity>() }
    var bullets = remember { mutableStateListOf<Bullet>() }
    var isGameOver by remember { mutableStateOf(false) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAlpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.8f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "alpha")

    LaunchedEffect(Unit) {
        while (isActive && !isGameOver) {
            withFrameMillis { time ->
                if (lastFrameTime == 0L) lastFrameTime = time
                val delta = (time - lastFrameTime) / 1000f
                lastFrameTime = time

                // Update Bullets
                val itB = bullets.iterator()
                while (itB.hasNext()) {
                    val b = itB.next()
                    val newY = b.y - delta * 0.8f
                    if (newY < 0) itB.remove()
                    // Collision check
                    val itE = enemies.iterator()
                    var hit = false
                    while (itE.hasNext()) {
                        val e = itE.next()
                        if (kotlin.math.abs(b.x - e.x) < 0.05f && kotlin.math.abs(b.y - e.y) < 0.05f) {
                            itE.remove()
                            hit = true
                            score += 10
                            GlowSoundManager.playGameAction(context)
                            GlowHapticManager.vibrateImpact(context)
                            break
                        }
                    }
                    if (hit) itB.remove()
                }

                // Update Enemies
                if (enemies.size < 5 + score / 50 && Random.nextFloat() < 0.02f) {
                    enemies.add(Entity(Random.nextFloat(), 0f, 0))
                }
                val itE = enemies.iterator()
                while (itE.hasNext()) {
                    val e = itE.next()
                    val newY = e.y + delta * (0.1f + score / 1000f)
                    if (newY > 1f) {
                        isGameOver = true
                        onGameOver(score)
                        GlowSoundManager.playVictory(context)
                        GlowHapticManager.vibrateError(context)
                    }
                }
            }
        }
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).pointerInput(Unit) {
            detectDragGestures { _, dragAmount -> playerX = (playerX + dragAmount.x / size.width).coerceIn(0f, 1f) }
        }.clickable { bullets.add(Bullet(playerX, 0.9f)); GlowSoundManager.playGameAction(context) }) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Starfield
                repeat(20) { i -> drawCircle(Color.White.copy(alpha = starAlpha), radius = 2f, center = Offset((i * 137L % 100) / 100f * w, (i * 243L % 100) / 100f * h)) }

                // Player
                drawRect(ElectricCyan, Offset(playerX * w - 20.dp.toPx(), 0.9f * h), Size(40.dp.toPx(), 20.dp.toPx()))

                // Bullets
                bullets.forEach { b -> drawCircle(NeonLime, radius = 4.dp.toPx(), center = Offset(b.x * w, b.y * h)) }

                // Enemies
                enemies.forEach { e -> drawRect(Color.Red, Offset(e.x * w - 15.dp.toPx(), e.y * h), Size(30.dp.toPx(), 30.dp.toPx())) }
            }

            Text("SCORE: $score", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

            if (isGameOver) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GAME OVER", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Text("Score: $score", color = Color.White, fontSize = 20.sp)
                        Button(onClick = { enemies.clear(); bullets.clear(); score = 0; isGameOver = false; lastFrameTime = 0L }, colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)) { Text("REPLAY", color = Color.Black) }
                    }
                }
            }
        }
    }
}
