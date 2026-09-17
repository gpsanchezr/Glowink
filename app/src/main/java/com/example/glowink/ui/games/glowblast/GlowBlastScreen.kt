package com.example.glowink.ui.games.glowblast

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.components.GlowCard
import com.example.glowink.ui.components.GlowPrimaryButton
import com.example.glowink.ui.components.GlowSecondaryButton
import com.example.glowink.ui.games.glowblast.model.GameMode
import com.example.glowink.ui.games.glowblast.model.GlowAvatarConfig
import com.example.glowink.ui.games.glowblast.model.MapTheme
import com.example.glowink.ui.games.glowblast.online.MultiplayerViewModel
import com.example.glowink.ui.games.glowblast.renderer.GlowCharacterCanvas
import com.example.glowink.ui.games.glowblast.renderer.GlowCharacterPresets
import com.example.glowink.ui.theme.*

/**
 * Estado de navegación interna dentro de Glow Blast.
 */
enum class GlowBlastStep { COVER, SETUP, MULTIPLAYER, CHARACTER, GAME, RESULT, SHOP }

/**
 * Pantalla Principal del Minijuego GLOW BLAST con gestión de pasos (Cover, Mode Setup, Multiplayer Lobby, Character Select, Game Arena, Results y Shop).
 */
@Composable
fun GlowBlastScreen(
    onBack: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onMultiplayerClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onShopClick: () -> Unit = {}
) {
    var currentStep by rememberSaveable { mutableStateOf(GlowBlastStep.COVER) }
    var selectedMode by rememberSaveable { mutableStateOf(GameMode.CLASICO) }
    var selectedMap by rememberSaveable { mutableStateOf(MapTheme.CIUDAD_NEON) }
    var selectedCharacter by remember { mutableStateOf(GlowCharacterPresets.NEO) }

    var lastMatchVictory by rememberSaveable { mutableStateOf(true) }
    var lastMatchScore by rememberSaveable { mutableIntStateOf(1250) }

    when (currentStep) {
        GlowBlastStep.COVER -> {
            GlowBlastCoverScreen(
                onBack = onBack,
                onPlayClick = { currentStep = GlowBlastStep.SETUP },
                onMultiplayerClick = { currentStep = GlowBlastStep.MULTIPLAYER },
                onSettingsClick = onSettingsClick,
                onShopClick = { currentStep = GlowBlastStep.SHOP }
            )
        }
        GlowBlastStep.SETUP -> {
            GlowBlastModeScreen(
                onBack = { currentStep = GlowBlastStep.COVER },
                onContinue = { mode, map ->
                    selectedMode = mode
                    selectedMap = map
                    currentStep = GlowBlastStep.MULTIPLAYER
                }
            )
        }
        GlowBlastStep.MULTIPLAYER -> {
            GlowBlastMultiplayerScreen(
                selectedMode = selectedMode,
                selectedMap = selectedMap,
                onBack = { currentStep = GlowBlastStep.SETUP },
                onContinue = { currentStep = GlowBlastStep.CHARACTER }
            )
        }
        GlowBlastStep.CHARACTER -> {
            GlowBlastCharacterScreen(
                selectedCharacter = selectedCharacter,
                onSelectCharacter = { selectedCharacter = it },
                onBack = { currentStep = GlowBlastStep.MULTIPLAYER },
                onStartMatch = { currentStep = GlowBlastStep.GAME }
            )
        }
        GlowBlastStep.GAME -> {
            GlowBlastGameScreen(
                mapTheme = selectedMap,
                selectedCharacter = selectedCharacter,
                onBack = {
                    lastMatchVictory = false
                    lastMatchScore = 450
                    currentStep = GlowBlastStep.RESULT
                },
                onMatchOver = { isVictory, finalScore ->
                    lastMatchVictory = isVictory
                    lastMatchScore = finalScore
                    currentStep = GlowBlastStep.RESULT
                }
            )
        }
        GlowBlastStep.RESULT -> {
            GlowBlastResultScreen(
                isVictory = lastMatchVictory,
                score = lastMatchScore,
                coinsEarned = if (lastMatchVictory) 350 else 100,
                xpEarned = if (lastMatchVictory) 120 else 40,
                selectedCharacter = selectedCharacter,
                onPlayAgain = { currentStep = GlowBlastStep.GAME },
                onMenu = { currentStep = GlowBlastStep.COVER },
                onContinue = onBack
            )
        }
        GlowBlastStep.SHOP -> {
            GlowBlastShopScreen(
                currentConfig = selectedCharacter,
                onAvatarUpdated = { updatedConfig ->
                    selectedCharacter = updatedConfig
                },
                onBack = { currentStep = GlowBlastStep.COVER }
            )
        }
    }
}

/**
 * FASE 2: Portada Cyberpunk para Glow Blast renderizada 100% en Canvas 2D con primitivas vectoriales.
 */
@Composable
fun GlowBlastCoverScreen(
    onBack: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onMultiplayerClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onShopClick: () -> Unit = {}
) {
    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                val w = size.width
                val h = size.height

                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF03010C), Color(0xFF0F0A28), Color(0xFF1E0C3E), Color(0xFF38084B))
                    )
                )

                repeat(40) { i ->
                    val starX = (i * 137L % 100) / 100f * w
                    val starY = (i * 243L % 50) / 100f * h
                    val starAlpha = 0.3f + (i % 5) * 0.15f
                    val starColor = if (i % 2 == 0) ElectricCyan else Color(0xFFFF007F)
                    drawCircle(
                        color = starColor.copy(alpha = starAlpha),
                        radius = (1.5f + (i % 3)),
                        center = Offset(starX, starY)
                    )
                }

                val horizonY = h * 0.52f
                val buildings = listOf(
                    Triple(0.00f, 0.22f, 0.28f),
                    Triple(0.12f, 0.18f, 0.34f),
                    Triple(0.24f, 0.20f, 0.22f),
                    Triple(0.38f, 0.25f, 0.40f),
                    Triple(0.58f, 0.18f, 0.30f),
                    Triple(0.72f, 0.22f, 0.26f),
                    Triple(0.88f, 0.20f, 0.32f)
                )

                buildings.forEach { (relX, relW, relH) ->
                    val bX = relX * w
                    val bW = relW * w
                    val bH = relH * h
                    val bY = horizonY - bH

                    drawRect(color = Color(0xFF0A0518), topLeft = Offset(bX, bY), size = Size(bW, bH))
                    drawRect(color = Color(0x3300F0FF), topLeft = Offset(bX, bY), size = Size(bW, bH), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))

                    val cols = (bW / 12f).toInt().coerceAtLeast(2)
                    val rows = (bH / 16f).toInt().coerceAtLeast(3)
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            if ((r + c + (relX * 10).toInt()) % 3 == 0) {
                                val winX = bX + c * (bW / cols) + 3f
                                val winY = bY + r * (bH / rows) + 3f
                                val winColor = if ((r + c) % 2 == 0) ElectricCyan else Color(0xFFFF007F)
                                drawRect(color = winColor.copy(alpha = 0.75f), topLeft = Offset(winX, winY), size = Size(bW / cols - 5f, bH / rows - 5f))
                            }
                        }
                    }

                    drawLine(color = ElectricCyan, start = Offset(bX + bW / 2f, bY), end = Offset(bX + bW / 2f, bY - 20f), strokeWidth = 2f)
                    drawCircle(color = Color.Red, radius = 3f, center = Offset(bX + bW / 2f, bY - 20f))
                }

                drawRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF15002A), Color(0xFF070414))),
                    topLeft = Offset(0f, horizonY),
                    size = Size(w, h - horizonY)
                )

                drawLine(color = Color(0xFFFF007F), start = Offset(0f, horizonY), end = Offset(w, horizonY), strokeWidth = 3f)

                val vanishingPoint = Offset(w * 0.5f, horizonY)
                for (p in -6..16) {
                    val targetX = (p / 10f) * w
                    drawLine(color = ElectricCyan.copy(alpha = 0.35f), start = vanishingPoint, end = Offset(targetX, h), strokeWidth = 1.5f)
                }

                var gridY = horizonY + 12f
                var step = 10f
                while (gridY < h) {
                    drawLine(color = Color(0xFFFF007F).copy(alpha = 0.35f), start = Offset(0f, gridY), end = Offset(w, gridY), strokeWidth = 1.5f)
                    step *= 1.25f
                    gridY += step
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    GlowSecondaryButton(
                        text = "← SALIR",
                        onClick = onBack,
                        borderColor = ElectricCyan,
                        textColor = ElectricCyan,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 12.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xAA100B2A)).border(1.dp, NeonLime, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("🪙 1,500", color = NeonLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xAA100B2A)).border(1.dp, ElectricCyan, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("💎 45", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    GlowCard(
                        shape = RoundedCornerShape(26.dp),
                        borderColor = ElectricCyan,
                        borderWidth = 2.dp,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GLOW BLAST", fontSize = 36.sp, fontWeight = FontWeight.Black, color = ElectricCyan, textAlign = TextAlign.Center, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("¡COLOCA • EXPLOTA • GANA!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonLime, textAlign = TextAlign.Center, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    GlowPrimaryButton(
                        text = "▶  JUGAR",
                        onClick = onPlayClick,
                        containerColor = Color(0xFFFF007F),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 14.dp),
                        fontSize = 18.sp,
                        modifier = Modifier.defaultMinSize(minWidth = 220.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlowSecondaryButton(text = "👥 MULTI", onClick = onMultiplayerClick, modifier = Modifier.weight(1f), borderColor = ElectricCyan, textColor = Color.White, fontSize = 11.5.sp, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp))
                    GlowSecondaryButton(text = "⚙ AJUSTES", onClick = onSettingsClick, modifier = Modifier.weight(1f), borderColor = ElectricCyan, textColor = Color.White, fontSize = 11.5.sp, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp))
                    GlowSecondaryButton(text = "🛍 TIENDA", onClick = onShopClick, modifier = Modifier.weight(1f), borderColor = NeonLime, textColor = Color.White, fontSize = 11.5.sp, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp))
                }
            }
        }
    }
}

/**
 * FASE 3: Pantalla de Configuración de Modo de Juego y Escenario/Mapa Neón.
 */
@Composable
fun GlowBlastModeScreen(
    onBack: () -> Unit = {},
    onContinue: (GameMode, MapTheme) -> Unit = { _, _ -> }
) {
    var selectedMode by rememberSaveable { mutableStateOf(GameMode.CLASICO) }
    var selectedMap by rememberSaveable { mutableStateOf(MapTheme.CIUDAD_NEON) }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            com.example.glowink.ui.theme.GlowinkMainBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        GlowSecondaryButton(
                            text = "←",
                            onClick = onBack,
                            borderColor = ElectricCyan,
                            textColor = ElectricCyan,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "SELECCIONA MODO DE JUEGO",
                            color = ElectricCyan,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val modes = listOf(
                            Triple(GameMode.CLASICO, "⚡ CLÁSICO", "Elimina a todos tus rivales en combate individual"),
                            Triple(GameMode.EQUIPOS, "👥 EQUIPOS", "Cooperación 2 vs 2 para dominar el campo"),
                            Triple(GameMode.TODOS_CONTRA_TODOS, "⚔ BATALLA", "Caos libre de 4 jugadores simultáneos"),
                            Triple(GameMode.TORNEO, "🏆 TORNEO", "Sobrevive a rondas con recompensa de puntos extra")
                        )

                        modes.forEach { (mode, title, desc) ->
                            val isSelected = mode == selectedMode
                            val cardBg = if (isSelected) Color(0x4400F0FF) else Color(0x221E1735)
                            val cardBorder = if (isSelected) ElectricCyan else Color.White.copy(alpha = 0.15f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(cardBg)
                                    .border(if (isSelected) 2.dp else 1.dp, cardBorder, RoundedCornerShape(18.dp))
                                    .clickable { selectedMode = mode }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = title, color = if (isSelected) Color.White else OnSurfaceMuted, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = desc, color = Color.White.copy(alpha = 0.7f), fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(ElectricCyan),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("✓", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("SELECCIONA ESCENARIO", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    val maps = listOf(
                        Triple(MapTheme.CIUDAD_NEON, "🌆", listOf(Color(0xFF003366), Color(0xFF330066))),
                        Triple(MapTheme.BOSQUE_GLOW, "🌱", listOf(Color(0xFF0A3A10), Color(0xFF005522))),
                        Triple(MapTheme.LABORATORIO, "🧪", listOf(Color(0xFF004455), Color(0xFF001133))),
                        Triple(MapTheme.ISLA_CELESTIAL, "🌌", listOf(Color(0xFF4A0055), Color(0xFF1B0033))),
                        Triple(MapTheme.RUINAS_CYBER, "🌋", listOf(Color(0xFF4A1000), Color(0xFF2B0000)))
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(maps) { (theme, icon, gradientColors) ->
                            val isSelected = theme == selectedMap
                            val borderCol = if (isSelected) Color(0xFFFF007F) else Color.White.copy(alpha = 0.2f)

                            Box(
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(105.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Brush.linearGradient(gradientColors))
                                    .border(if (isSelected) 2.5.dp else 1.dp, borderCol, RoundedCornerShape(18.dp))
                                    .clickable { selectedMap = theme }
                                    .padding(12.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(icon, fontSize = 24.sp)
                                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x88000000)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text(theme.difficultyLabel, color = NeonLime, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Column {
                                        Text(text = theme.displayName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                GlowPrimaryButton(
                    text = "CONTINUAR ▶",
                    onClick = { onContinue(selectedMode, selectedMap) },
                    containerColor = Color(0xFFFF007F),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * FASE 15: Pantalla Visual y Multijugador Firebase para Sala Multijugador (Lobby).
 */
@Composable
fun GlowBlastMultiplayerScreen(
    selectedMode: GameMode = GameMode.CLASICO,
    selectedMap: MapTheme = MapTheme.CIUDAD_NEON,
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val context = LocalContext.current
    var playerCount by rememberSaveable { mutableIntStateOf(4) }
    var onlineRoomCodeInput by remember { mutableStateOf("") }

    val viewModel = remember { MultiplayerViewModel() }
    val roomState by viewModel.roomState.collectAsState()
    val currentRoomId by viewModel.currentRoomId.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    val displayPlayers = if (roomState != null && roomState?.players?.isNotEmpty() == true) {
        roomState!!.players.map { p ->
            Triple(if (p.isHost) "${p.name} (Líder)" else p.name, if (p.isHost) "👑" else "🎮", p.isReady)
        }
    } else {
        listOf(
            Triple("Tú (Líder)", "👑", true),
            Triple("NeoFire", "🎮", true),
            Triple("LunaStar", "🎮", true),
            Triple("Zeta", "🎮", true)
        )
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            com.example.glowink.ui.theme.GlowinkMainBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlowSecondaryButton(
                            text = "←",
                            onClick = onBack,
                            borderColor = ElectricCyan,
                            textColor = ElectricCyan,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MULTIJUGADOR EN LÍNEA",
                                color = ElectricCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (currentRoomId != null) "SALA EN LÍNEA: #$currentRoomId" else "SALAS ONLINE CON CLOUD FIRESTORE",
                                color = if (currentRoomId != null) NeonLime else Color.White.copy(alpha = 0.7f),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // INPUT Y ACCIONES MULTIJUGADOR FIREBASE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = onlineRoomCodeInput,
                            onValueChange = { onlineRoomCodeInput = it },
                            placeholder = { Text("Código de Sala", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        GlowSecondaryButton(
                            text = "UNIRSE",
                            onClick = { viewModel.joinRoom(onlineRoomCodeInput) },
                            borderColor = ElectricCyan,
                            textColor = ElectricCyan,
                            fontSize = 11.5.sp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        )

                        GlowSecondaryButton(
                            text = "CREAR 🔥",
                            onClick = { viewModel.createRoom(selectedMap, selectedMode, playerCount) },
                            borderColor = NeonLime,
                            textColor = NeonLime,
                            fontSize = 11.5.sp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CANTIDAD DE JUGADORES",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..4).forEach { num ->
                                val isSelected = num == playerCount
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ElectricCyan else Color(0x221E1735))
                                        .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) ElectricCyan else Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .clickable { playerCount = num },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$num",
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    GlowCard(
                        shape = RoundedCornerShape(22.dp),
                        borderColor = ElectricCyan,
                        borderWidth = 1.5.dp,
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SALA DE ESPERA (LOBBY EN LÍNEA)",
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            (0 until playerCount).forEach { index ->
                                val playerInfo = displayPlayers.getOrNull(index) ?: Triple("Esperando jugador...", "⏳", false)
                                val isLeader = index == 0

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isLeader) Color(0x3300F0FF) else Color(0x11FFFFFF))
                                        .border(1.dp, if (isLeader) ElectricCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(playerInfo.second, fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = playerInfo.first,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (playerInfo.third) NeonLime.copy(alpha = 0.2f) else Color(0x22FFFFFF))
                                                .border(1.dp, if (playerInfo.third) NeonLime else Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (playerInfo.third) "✓ LISTO" else "⏳ ESPERANDO",
                                                color = if (playerInfo.third) NeonLime else Color.Gray,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF003366), Color(0xFF330066))))
                            .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ESCENARIO: ${selectedMap.displayName}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Modo: ${selectedMode.title} • Dificultad: ${selectedMap.difficultyLabel}",
                                    color = NeonLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text("🌆", fontSize = 32.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                GlowPrimaryButton(
                    text = "SELECCIONAR AVATAR ▶",
                    onClick = {
                        viewModel.startMatch()
                        onContinue()
                    },
                    containerColor = ElectricCyan,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * FASE 5: Pantalla de Selección de Personajes / Avatares Procedurales Neón.
 */
@Composable
fun GlowBlastCharacterScreen(
    selectedCharacter: GlowAvatarConfig = GlowCharacterPresets.NEO,
    onSelectCharacter: (GlowAvatarConfig) -> Unit = {},
    onBack: () -> Unit = {},
    onStartMatch: () -> Unit = {}
) {
    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            com.example.glowink.ui.theme.GlowinkMainBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlowSecondaryButton(
                            text = "←",
                            onClick = onBack,
                            borderColor = ElectricCyan,
                            textColor = ElectricCyan,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ELIGE TU AVATAR",
                            color = ElectricCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    GlowCard(
                        shape = RoundedCornerShape(28.dp),
                        borderColor = ElectricCyan,
                        borderWidth = 2.dp,
                        contentPadding = PaddingValues(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GlowCharacterCanvas(
                                config = selectedCharacter,
                                modifier = Modifier.size(200.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = selectedCharacter.characterId,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "✓ LISTO PARA EL COMBATE",
                            color = NeonLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlowCharacterPresets.ALL_PRESETS.forEach { preset ->
                            val isSelected = preset.characterId == selectedCharacter.characterId
                            val cardBg = if (isSelected) Color(0x4400F0FF) else Color(0x221E1735)
                            val borderCol = if (isSelected) ElectricCyan else Color.White.copy(alpha = 0.15f)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBg)
                                    .border(if (isSelected) 2.dp else 1.dp, borderCol, RoundedCornerShape(16.dp))
                                    .clickable { onSelectCharacter(preset) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    GlowCharacterCanvas(
                                        config = preset,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.characterId,
                                        color = if (isSelected) Color.White else OnSurfaceMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                GlowPrimaryButton(
                    text = "INICIAR PARTIDA ▶",
                    onClick = onStartMatch,
                    containerColor = Color(0xFFFF007F),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    fontSize = 17.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
