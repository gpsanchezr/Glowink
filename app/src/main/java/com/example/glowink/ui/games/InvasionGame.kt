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

private data class Entity(val x: Float, val y: Float, val type: Int)
private data class Bullet(val x: Float, val y: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvasionGameScreen(onExit: () -> Unit, onGameOver: (Int) -> Unit) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var gameResetCounter by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var playerX by remember { mutableFloatStateOf(0.5f) }
    val enemies = remember { mutableStateListOf<Entity>() }
    val bullets = remember { mutableStateListOf<Bullet>() }
    var isGameOver by remember { mutableStateOf(false) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "alpha"
    )

    DisposableEffect(Unit) {
        onDispose {
            isPlaying = false
            enemies.clear()
            bullets.clear()
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

                // Update Bullets
                val itB = bullets.iterator()
                while (itB.hasNext()) {
                    val b = itB.next()
                    val newY = b.y - delta * 0.8f
                    if (newY < 0) {
                        itB.remove()
                        continue
                    }
                    // Collision check
                    val itE = enemies.iterator()
                    var hit = false
                    while (itE.hasNext()) {
                        val e = itE.next()
                        if (kotlin.math.abs(b.x - e.x) < 0.06f && kotlin.math.abs(b.y - e.y) < 0.06f) {
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

                // Spawn & Update Enemies
                if (enemies.size < 5 + score / 50 && Random.nextFloat() < 0.03f) {
                    enemies.add(Entity(Random.nextFloat().coerceIn(0.05f, 0.95f), 0f, 0))
                }
                val itE = enemies.iterator()
                while (itE.hasNext()) {
                    val e = itE.next()
                    val newY = e.y + delta * (0.12f + score / 800f)
                    if (newY > 0.92f) {
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

    fun restartGame() {
        enemies.clear()
        bullets.clear()
        score = 0
        playerX = 0.5f
        isGameOver = false
        gameResetCounter++
    }

    fun exitGame() {
        isPlaying = false
        enemies.clear()
        bullets.clear()
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
                Text(
                    text = "INVASIÓN NEÓN 👾",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text("SCORE: $score", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            playerX = (playerX + dragAmount.x / size.width).coerceIn(0.05f, 0.95f)
                        }
                    }
                    .clickable {
                        if (!isGameOver) {
                            bullets.add(Bullet(playerX, 0.88f))
                            GlowSoundManager.playGameAction(context)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                    val w = size.width
                    val h = size.height

                    repeat(25) { i ->
                        drawCircle(
                            Color.White.copy(alpha = starAlpha),
                            radius = 2.5f,
                            center = Offset((i * 137L % 100) / 100f * w, (i * 243L % 100) / 100f * h)
                        )
                    }

                    drawRect(
                        ElectricCyan,
                        Offset(playerX * w - 20.dp.toPx(), 0.88f * h),
                        Size(40.dp.toPx(), 20.dp.toPx())
                    )

                    bullets.forEach { b ->
                        drawCircle(NeonLime, radius = 5.dp.toPx(), center = Offset(b.x * w, b.y * h))
                    }

                    enemies.forEach { e ->
                        drawRect(
                            Color.Red,
                            Offset(e.x * w - 15.dp.toPx(), e.y * h),
                            Size(30.dp.toPx(), 30.dp.toPx())
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
                            Text("GAME OVER", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Score: $score", color = Color.White, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { restartGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                            ) {
                                Text("REPLAY", color = Color.Black, fontWeight = FontWeight.Bold)
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
