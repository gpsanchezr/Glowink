package com.example.glowink.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.components.GameActionButton
import com.example.glowink.ui.theme.*
import com.example.glowink.util.GlowSoundManager
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class DuelPhase { INTRO, ARMED, GO, ROUND_RESULT, MATCH_OVER }

private data class DuelState(
    val phase: DuelPhase = DuelPhase.INTRO,
    val scoreTop: Int = 0,
    val scoreBottom: Int = 0,
    val lastRoundWinner: Int = 0,
    val falseStartBy: Int = 0,
    val round: Int = 1
)

@Composable
fun DuelGameScreen(
    onExit: () -> Unit,
    onMatchOver: (topWon: Boolean) -> Unit
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(DuelState()) }
    var armedAtMillis by remember { mutableLongStateOf(0L) }
    var goAtMillis by remember { mutableLongStateOf(0L) }
    var reported by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            state = state.copy(phase = DuelPhase.INTRO)
        }
    }

    fun startRound() {
        state = state.copy(phase = DuelPhase.ARMED)
    }

    LaunchedEffect(state.phase, state.round) {
        if (state.phase == DuelPhase.ARMED) {
            armedAtMillis = System.currentTimeMillis()
            delay(Random.nextLong(1200, 3600))
            if (state.phase == DuelPhase.ARMED) {
                goAtMillis = System.currentTimeMillis()
                state = state.copy(phase = DuelPhase.GO)
                GlowSoundManager.playGameAction(context)
            }
        }
    }

    fun onZoneTapped(zone: Int) {
        when (state.phase) {
            DuelPhase.ARMED -> {
                val winner = if (zone == 1) 2 else 1
                state = state.copy(phase = DuelPhase.ROUND_RESULT, lastRoundWinner = winner, falseStartBy = zone)
                GlowSoundManager.playGameAction(context)
            }
            DuelPhase.GO -> {
                state = state.copy(phase = DuelPhase.ROUND_RESULT, lastRoundWinner = zone, falseStartBy = 0)
                GlowSoundManager.playVictory(context)
            }
            else -> Unit
        }
    }

    LaunchedEffect(state.phase) {
        if (state.phase == DuelPhase.ROUND_RESULT) {
            val newTop = state.scoreTop + if (state.lastRoundWinner == 1) 1 else 0
            val newBottom = state.scoreBottom + if (state.lastRoundWinner == 2) 1 else 0
            delay(1400)
            state = if (newTop >= 3 || newBottom >= 3) {
                state.copy(phase = DuelPhase.MATCH_OVER, scoreTop = newTop, scoreBottom = newBottom)
            } else {
                state.copy(phase = DuelPhase.ARMED, scoreTop = newTop, scoreBottom = newBottom, round = state.round + 1)
            }
        }
    }

    if (state.phase == DuelPhase.MATCH_OVER && !reported) {
        reported = true
        onMatchOver(state.scoreTop > state.scoreBottom)
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
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("← Volver", color = ElectricCyan, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onExit() })
                Text("DUELO RELÁMPAGO ⚡", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Ronda ${state.round}", color = NeonLime, fontWeight = FontWeight.Bold)
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(if (state.lastRoundWinner == 1) NeonLime.copy(0.2f) else Color(0xFF140D2A))
                            .clickable(enabled = state.phase == DuelPhase.ARMED || state.phase == DuelPhase.GO) { onZoneTapped(1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JUGADOR 1: ${state.scoreTop}", color = ElectricCyan, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color(0xFF070414)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (state.phase) {
                            DuelPhase.INTRO -> GameActionButton(text = "¡EMPEZAR DUELO!", onClick = { startRound() })
                            DuelPhase.ARMED -> Text("¡PREPARADOS...!", color = Color(0xFFFFD24C), fontSize = 20.sp, fontWeight = FontWeight.Black)
                            DuelPhase.GO -> Text("⚡ ¡DISPARA YA! ⚡", color = NeonLime, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            DuelPhase.ROUND_RESULT -> Text("¡Punto para Jugador ${state.lastRoundWinner}!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            DuelPhase.MATCH_OVER -> GameActionButton(text = "¡NUEVA PARTIDA!", onClick = { state = DuelState(); reported = false })
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(if (state.lastRoundWinner == 2) NeonLime.copy(0.2f) else Color(0xFF140D2A))
                            .clickable(enabled = state.phase == DuelPhase.ARMED || state.phase == DuelPhase.GO) { onZoneTapped(2) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JUGADOR 2: ${state.scoreBottom}", color = NeonLime, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
