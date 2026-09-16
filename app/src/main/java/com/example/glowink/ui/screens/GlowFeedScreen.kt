package com.example.glowink.ui.screens
import coil.compose.AsyncImage
import androidx.compose.animation.core.Animatable
import com.example.glowink.data.GlowStory
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import com.example.glowink.data.User
import com.example.glowink.ui.viewmodel.ChatsListUiState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.GlassContainer
import com.example.glowink.ui.theme.GlowinkTheme
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.theme.ObsidianBackground
import com.example.glowink.ui.theme.OnSurfaceMuted
import com.example.glowink.ui.theme.UltravioletPurple
import com.example.glowink.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class ReactionParticle(
    val id: Long,
    val emoji: String,
    val offsetX: Float,
    val offsetYAnim: Animatable<Float, *>,
    val alphaAnim: Animatable<Float, *>
)

/**
 * Pantalla de Visualización de Estados ("GlowFeedScreen").
 *
 * Características:
 * 1. Reproducción de historias a pantalla completa.
 * 2. Barras de progreso segmentadas en la parte superior.
 * 3. Botones de reacciones rápidas basadas en emojis (🔥, 💖, 😂, ⚡) que estallan/flotan en pantalla al ser tocados.
 */
@Composable
fun GlowFeedScreen(
    viewModel: ChatViewModel? = if (LocalInspectionMode.current) null else ChatViewModel(),
    onCloseFeed: () -> Unit = {}
) {
    // Si estamos en modo Preview de Android Studio (LocalInspectionMode.current == true) o no hay ViewModel,
    // usamos un estado simulado para evitar que el ViewModel intente inicializar Firebase (lo cual lanza
    // IllegalStateException: Default FirebaseApp is not initialized in this process).
    val uiState = if (viewModel != null) {
        val state by viewModel.chatsListUiState.collectAsState()
        state
    } else {
        ChatsListUiState(
            currentUser = User(username = "GlowUser"),
            stories = listOf(
                GlowStory(
                    id = "preview_1",
                    userId = "preview_user",
                    mediaUrl = "",
                    tipoFiltro = "Disco Vibe"
                )
            )
        )
    }
    val stories = uiState.stories
    var currentStoryIndex by remember { mutableStateOf(0) }

    // Partículas animadas de reacciones flotantes
    val activeParticles = remember { mutableStateListOf<ReactionParticle>() }
    val coroutineScope = rememberCoroutineScope()

    // Simulación de avance de la barra de progreso segmentada
    var progress by remember { mutableStateOf(0f) }
    // rememberUpdatedState evita un bug sutil: sin esto, si "stories" pasa de vacío a tener
    // datos reales de Firestore MIENTRAS la barra ya está corriendo, el efecto de abajo
    // seguiría viendo la lista vacía que capturó al arrancar (Compose no reinicia el efecto
    // solo porque cambió "stories", ya que no es su key) y cerraría el feed de inmediato en
    // cuanto la barra llegara al final, aunque sí hubiera historias para mostrar.
    val latestStories = rememberUpdatedState(stories)
    LaunchedEffect(currentStoryIndex) {
        progress = 0f
        while (progress < 1f) {
            delay(50)
            progress += 0.02f
        }
        val freshStories = latestStories.value
        if (freshStories.isNotEmpty() && currentStoryIndex < freshStories.size - 1) {
            currentStoryIndex++
        } else {
            onCloseFeed()
        }
    }

    fun triggerEmojiBurst(emoji: String) {
        val particleId = System.currentTimeMillis() + Random.nextLong(1000)
        val randomX = Random.nextFloat() * 300f - 150f
        val offsetY = Animatable(0f)
        val alpha = Animatable(1f)

        val particle = ReactionParticle(particleId, emoji, randomX, offsetY, alpha)
        activeParticles.add(particle)

        // Animación de subida y desaparición gradual
        coroutineScope.launch {
            launch {
                offsetY.animateTo(-400f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
            }
            launch {
                alpha.animateTo(0f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
            }
            activeParticles.remove(particle)
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF160B30),
                            ObsidianBackground,
                            Color(0xFF100724)
                        )
                    )
                )
        ) {

            // --- Contenido Multimedia de la Historia ---
            StoryContentDisplay(
                story = stories.getOrNull(currentStoryIndex),
                activeParticles = activeParticles
            )

            // --- Header Superior con Barras de Progreso Segmentadas ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {

                // 1. Barras de Progreso Segmentadas
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val totalSegments = if (stories.isEmpty()) 3 else stories.size
                    for (i in 0 until totalSegments) {
                        val segmentProgress = when {
                            i < currentStoryIndex -> 1f
                            i == currentStoryIndex -> progress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = segmentProgress,
                            color = NeonLime,
                            trackColor = Color(0x66FFFFFF),
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Información del Creador de la Historia
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, ElectricCyan, CircleShape)
                                .background(Color(0xFF221A3B))
                        ) {
                            com.example.glowink.ui.avatar.GlowAvatar(
                                config = uiState.currentUser?.avatarConfig ?: com.example.glowink.data.Avatar3DConfig(),
                                animate = false,
                                modifier = Modifier.fillMaxSize().padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = uiState.currentUser?.username ?: "GlowUser",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Disco Vibe • Hace 2h",
                                fontSize = 11.sp,
                                color = ElectricCyan
                            )
                        }
                    }

                    Text(
                        text = "✕",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.clickable { onCloseFeed() }
                    )
                }
            }

            // --- Panel Inferior: Reacciones rápidas ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                ReactionButton("🔥") { triggerEmojiBurst("🔥") }
                ReactionButton("💖") { triggerEmojiBurst("💖") }
                ReactionButton("😂") { triggerEmojiBurst("😂") }
                ReactionButton("⚡") { triggerEmojiBurst("⚡") }
            }
        }
    }
}

/**
 * Muestra el contenido central del Estado y renderiza las partículas animadas de reacción que estallan.
 */
@Composable
private fun StoryContentDisplay(
    story: GlowStory?,
    activeParticles: List<ReactionParticle>
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(3.dp, UltravioletPurple, RoundedCornerShape(28.dp))
                    .background(Color(0xFF231640))
            ) {
                if (!story?.mediaUrl.isNullOrBlank() &&
                    story?.mediaUrl != "story_media" &&
                    story?.mediaUrl != "story_media_sin_subir"
                ) {
                    AsyncImage(
                        model = story.mediaUrl,
                        contentDescription = "Historia de Glowink",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp))
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✨", fontSize = 70.sp)
                        Text(
                            "Historia sin imagen",
                            color = OnSurfaceMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (story != null) "✨ GlowStory • ${story.tipoFiltro}" else "Aún no hay GlowStories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Renderizado de Partículas de Emojis que Estallan y Flotan
        activeParticles.forEach { particle ->
            Text(
                text = particle.emoji,
                fontSize = 42.sp,
                modifier = Modifier
                    .offset { IntOffset(particle.offsetX.toInt(), particle.offsetYAnim.value.toInt()) }
                    .alpha(particle.alphaAnim.value)
            )
        }
    }
}

@Composable
private fun ReactionButton(
    emoji: String,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0x331E1735))
            .border(1.dp, Color(0x6600F0FF), CircleShape)
            .clickable { onClick() }
    ) {
        Text(text = emoji, fontSize = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun GlowFeedScreenPreview() {
    GlowinkTheme {
        GlowFeedScreen()
    }
}
