package com.example.glowink.ui.games

import com.example.glowink.ui.components.GameActionButton

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.theme.ObsidianBackground
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * "Duelo Relámpago": duelo de reflejos original para dos jugadores compartiendo un mismo
 * teléfono (pasa-y-juega), pensado como una interpretación propia y sin derechos de autor
 * de un "duelo de pistolitas" — el mecanismo real es un simple test de tiempo de reacción,
 * un formato de juego de fiesta genérico que no pertenece a nadie. Gana el primero en
 * anotar 3 rondas; tocar antes de la señal "¡YA!" pierde la ronda al instante.
 */

private enum class DuelPhase { INTRO, ARMED, GO, ROUND_RESULT, MATCH_OVER }

private data class DuelState(
    val phase: DuelPhase = DuelPhase.INTRO,
    val scoreTop: Int = 0,
    val scoreBottom: Int = 0,
    val lastRoundWinner: Int = 0, // 1 = arriba, 2 = abajo
    val falseStartBy: Int = 0,
    val round: Int = 1
)

@Composable
fun DuelGameScreen(
    onExit: () -> Unit,
    onMatchOver: (topWon: Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var state by remember { mutableStateOf(DuelState()) }
    var armedAtMillis by remember { mutableStateOf(0L) }
    var goAtMillis by remember { mutableStateOf(0L) }
    var reported by remember { mutableStateOf(false) }

    fun startRound() {
        state = state.copy(phase = DuelPhase.ARMED)
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            state = state.copy(phase = DuelPhase.INTRO)
        }
    }

    LaunchedEffect(state.phase, state.round) {
        if (state.phase == DuelPhase.ARMED) {
            armedAtMillis = System.currentTimeMillis()
            delay(Random.nextLong(1200, 3600))
            // Si nadie hizo trampa mientras esperábamos, mostrar la señal
            if (state.phase == DuelPhase.ARMED) {
                goAtMillis = System.currentTimeMillis()
                state = state.copy(phase = DuelPhase.GO)
                com.example.glowink.util.GlowSoundManager.playGameAction(context)
            }
        }
    }

    fun onZoneTapped(zone: Int) {
        when (state.phase) {
            DuelPhase.ARMED -> {
                // Salida en falso: pierde el que tocó antes de tiempo
                val winner = if (zone == 1) 2 else 1
                state = state.copy(phase = DuelPhase.ROUND_RESULT, lastRoundWinner = winner, falseStartBy = zone)
                com.example.glowink.util.GlowSoundManager.playGameAction(context)
            }
            DuelPhase.GO -> {
                state = state.copy(phase = DuelPhase.ROUND_RESULT, lastRoundWinner = zone, falseStartBy = 0)
                com.example.glowink.util.GlowSoundManager.playVictory(context)
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("← Salir", color = ElectricCyan, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onExit))
                    Text("DUELO RELÁMPAGO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text("R${state.round}", color = NeonLime, fontSize = 15.sp)
                }

                // Zona jugador de ARRIBA (rotada para que se lea desde el lado opuesto del teléfono)
                DuelZone(
                    score = state.scoreTop,
                    label = "JUGADOR 1",
                    background = Color(0xFF241241),
                    highlight = state.phase == DuelPhase.GO,
                    message = duelZoneMessage(state, isTop = true),
                    rotated = true,
                    modifier = Modifier.weight(1f).fillMaxWidth().clickable { onZoneTapped(1) }
                )

                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0x33FFFFFF)))

                // Zona jugador de ABAJO
                DuelZone(
                    score = state.scoreBottom,
                    label = "JUGADOR 2",
                    background = Color(0xFF0B1F3A),
                    highlight = state.phase == DuelPhase.GO,
                    message = duelZoneMessage(state, isTop = false),
                    rotated = false,
                    modifier = Modifier.weight(1f).fillMaxWidth().clickable { onZoneTapped(2) }
                )
            }

            if (state.phase == DuelPhase.INTRO) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.78f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("Duelo Relámpago ⚡", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Un teléfono, dos jugadores. Cuando aparezca \"¡YA!\" toca tu zona lo más rápido posible. ¡Ojo! tocar antes de tiempo pierde la ronda. Gana quien llegue primero a 3.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        GameActionButton(text = "¡Empezar Duelo!") { startRound() }
                    }
                }
            }

            if (state.phase == DuelPhase.MATCH_OVER) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (state.scoreTop > state.scoreBottom) "¡Gana Jugador 1! 🏆" else "¡Gana Jugador 2! 🏆",
                            color = NeonLime, fontWeight = FontWeight.Black, fontSize = 22.sp
                        )
                        Text("${state.scoreTop} — ${state.scoreBottom}", color = Color.White, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        GameActionButton(text = "Revancha") {
                            state = DuelState()
                            reported = false
                        }
                    }
                }
            }
        }
    }
}

private fun duelZoneMessage(state: DuelState, isTop: Boolean): String = when (state.phase) {
    DuelPhase.ARMED -> "Espera…"
    DuelPhase.GO -> "¡YA! 👆"
    DuelPhase.ROUND_RESULT -> {
        val winnerIsThis = (isTop && state.lastRoundWinner == 1) || (!isTop && state.lastRoundWinner == 2)
        when {
            state.falseStartBy != 0 && ((isTop && state.falseStartBy == 1) || (!isTop && state.falseStartBy == 2)) -> "¡Salida en falso!"
            winnerIsThis -> "¡Punto! 🎯"
            else -> "Muy lento…"
        }
    }
    else -> ""
}

@Composable
private fun DuelZone(
    score: Int,
    label: String,
    background: Color,
    highlight: Boolean,
    message: String,
    rotated: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (highlight) Brush.radialGradient(listOf(NeonLime.copy(alpha = 0.5f), background)) else Brush.radialGradient(listOf(background, background))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(rotationZ = if (rotated) 180f else 0f)
        ) {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("★".repeat(score) + "☆".repeat((3 - score).coerceAtLeast(0)), color = NeonLime, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(message, color = if (highlight) Color.Black else Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        }
    }
}
