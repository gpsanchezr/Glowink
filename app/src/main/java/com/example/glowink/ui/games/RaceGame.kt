package com.example.glowink.ui.games

import com.example.glowink.ui.components.GameActionButton

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.theme.ObsidianBackground
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * "Carrera Glow": carrera de fichas por un circuito ovalado propio, inspirada en la idea
 * genérica de "juego de mesa donde fichas recorren una pista y se capturan entre sí" (el
 * mismo principio detrás de juegos centenarios de dominio público como el Pachisi indio) —
 * NO copia el tablero en forma de cruz ni el arte de ningún juego comercial: el circuito,
 * los colores y la presentación son propios de Glowink. Reglas: se necesita un 6 para salir
 * de la base, capturas al caer exacto sobre la ficha de un rival (salvo en casillas
 * "seguras" ⭐), sacar 6 da turno extra, y gana quien complete la vuelta primero.
 */

private const val LOOP_LENGTH = 24

private val PLAYER_COLORS = listOf(Color(0xFF39FF14), Color(0xFF00F0FF), Color(0xFFFF4FD8), Color(0xFFFFD24C))
private val PLAYER_NAMES = listOf("Verde", "Cian", "Rosa", "Dorado")

private data class RaceState(
    val numPlayers: Int = 2,
    val positions: List<Int> = listOf(-1, -1, -1, -1), // -1 = en base, 0..LOOP_LENGTH = en pista/meta
    val currentPlayer: Int = 0,
    val lastRoll: Int = 0,
    val rolling: Boolean = false,
    val message: String = "Turno de ${PLAYER_NAMES[0]}: tira el dado",
    val winner: Int? = null
)

private fun startCellFor(player: Int, numPlayers: Int): Int = player * (LOOP_LENGTH / numPlayers)

private fun isSafeCell(absoluteCell: Int, numPlayers: Int): Boolean {
    for (p in 0 until numPlayers) {
        if (startCellFor(p, numPlayers) == absoluteCell) return true
    }
    return false
}

private fun applyRoll(state: RaceState, roll: Int): RaceState {
    val player = state.currentPlayer
    val pos = state.positions[player]
    val numPlayers = state.numPlayers
    val newPositions = state.positions.toMutableList()
    var message: String
    var extraTurn = false

    if (pos == -1) {
        if (roll == 6) {
            newPositions[player] = 0
            message = "¡${PLAYER_NAMES[player]} sale de la base! Turno extra por sacar 6"
            extraTurn = true
        } else {
            message = "${PLAYER_NAMES[player]} necesita un 6 para salir"
        }
    } else {
        val newRelative = pos + roll
        if (newRelative >= LOOP_LENGTH) {
            newPositions[player] = LOOP_LENGTH
            message = "¡${PLAYER_NAMES[player]} llegó a la meta! 🏁"
        } else {
            newPositions[player] = newRelative
            val myAbsolute = (startCellFor(player, numPlayers) + newRelative) % LOOP_LENGTH
            var captured = false
            if (!isSafeCell(myAbsolute, numPlayers)) {
                for (other in 0 until numPlayers) {
                    if (other == player) continue
                    val otherPos = newPositions[other]
                    if (otherPos in 0 until LOOP_LENGTH) {
                        val otherAbsolute = (startCellFor(other, numPlayers) + otherPos) % LOOP_LENGTH
                        if (otherAbsolute == myAbsolute) {
                            newPositions[other] = -1
                            captured = true
                        }
                    }
                }
            }
            message = if (captured) "¡${PLAYER_NAMES[player]} capturó una ficha! 💥" else "${PLAYER_NAMES[player]} avanza $roll casillas"
            if (roll == 6) extraTurn = true
        }
    }

    val winner = if (newPositions[player] == LOOP_LENGTH) player else null
    val nextPlayer = if (extraTurn || winner != null) player else (player + 1) % numPlayers

    return state.copy(
        positions = newPositions,
        currentPlayer = nextPlayer,
        lastRoll = roll,
        message = if (winner != null) "🏆 ¡${PLAYER_NAMES[player]} gana la carrera!" else message,
        winner = winner
    )
}

@Composable
fun RaceGameScreen(
    numPlayers: Int,
    onExit: () -> Unit,
    onGameOver: (winnerIsPlayerZero: Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var state by remember { mutableStateOf(RaceState(numPlayers = numPlayers, positions = List(4) { -1 })) }
    var reported by remember { mutableStateOf(false) }

    if (state.winner != null && !reported) {
        reported = true
        onGameOver(state.winner == 0)
        com.example.glowink.util.GlowSoundManager.playVictory(context)
    }

    LaunchedEffect(state.rolling) {
        if (state.rolling) {
            com.example.glowink.util.GlowSoundManager.playGameAction(context)
            delay(650)
            val roll = Random.nextInt(1, 7)
            state = applyRoll(state.copy(rolling = false), roll)
        }
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(ObsidianBackground, Color(0xFF12082A))))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("← Salir", color = ElectricCyan, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onExit))
                Text("CARRERA GLOW", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Spacer(modifier = Modifier.size(1.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .aspectRatio(1f)
            ) {
                RaceTrackCanvas(state)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                for (p in 0 until state.numPlayers) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                if (p == state.currentPlayer) PLAYER_COLORS[p].copy(alpha = 0.25f) else Color(0x14FFFFFF),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(PLAYER_NAMES[p], color = PLAYER_COLORS[p], fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (state.winner == null) {
                DiceButton(rolling = state.rolling, lastRoll = state.lastRoll) {
                    if (!state.rolling) state = state.copy(rolling = true)
                }
            } else {
                GameActionButton(text = "Nueva Carrera") {
                    state = RaceState(numPlayers = numPlayers, positions = List(4) { -1 })
                    reported = false
                }
            }
        }
    }
}

@Composable
private fun DiceButton(rolling: Boolean, lastRoll: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(Brush.linearGradient(listOf(Color(0xFF9D4EDD), Color(0xFF00F0FF))), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(if (rolling) "🎲" else lastRoll.takeIf { it > 0 }?.toString() ?: "🎲", color = Color.Black, fontSize = 26.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun RaceTrackCanvas(state: RaceState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radiusX = size.width * 0.42f
        val radiusY = size.height * 0.42f
        val cellR = size.width * 0.028f

        fun cellCenter(absoluteCell: Int): Offset {
            val angle = -Math.PI.toFloat() / 2f + (2f * Math.PI.toFloat() * absoluteCell / LOOP_LENGTH)
            return Offset(cx + radiusX * cos(angle), cy + radiusY * sin(angle))
        }

        // Pista
        for (i in 0 until LOOP_LENGTH) {
            val p = cellCenter(i)
            val safe = isSafeCell(i, state.numPlayers)
            drawCircle(
                color = if (safe) Color(0xFFFFD24C).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.14f),
                radius = if (safe) cellR * 1.25f else cellR,
                center = p
            )
        }

        // Bases (fuera del círculo, una por jugador)
        for (p in 0 until state.numPlayers) {
            val baseAngle = -Math.PI.toFloat() / 2f + (2f * Math.PI.toFloat() * startCellFor(p, state.numPlayers) / LOOP_LENGTH)
            val baseCenter = Offset(cx + (radiusX * 1.32f) * cos(baseAngle), cy + (radiusY * 1.32f) * sin(baseAngle))
            drawCircle(color = PLAYER_COLORS[p].copy(alpha = 0.18f), radius = cellR * 1.8f, center = baseCenter)
            drawCircle(color = PLAYER_COLORS[p], radius = cellR * 0.85f, center = baseCenter, style = androidx.compose.ui.graphics.drawscope.Stroke(width = cellR * 0.4f))
            if (state.positions[p] == -1) {
                drawCircle(color = PLAYER_COLORS[p], radius = cellR * 0.9f, center = baseCenter)
            }
        }

        // Fichas en pista
        for (p in 0 until state.numPlayers) {
            val pos = state.positions[p]
            if (pos in 0 until LOOP_LENGTH) {
                val absolute = (startCellFor(p, state.numPlayers) + pos) % LOOP_LENGTH
                val center = cellCenter(absolute)
                val jitter = Offset((p % 2) * cellR * 0.5f - cellR * 0.25f, (p / 2) * cellR * 0.5f - cellR * 0.25f)
                drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = cellR * 1.05f, center = center + jitter)
                drawCircle(color = PLAYER_COLORS[p], radius = cellR * 0.95f, center = center + jitter)
            } else if (pos == LOOP_LENGTH) {
                // Ficha en meta: se dibuja en el centro
                val homeCenter = Offset(cx + (p - state.numPlayers / 2f) * cellR * 2f, cy)
                drawCircle(color = PLAYER_COLORS[p], radius = cellR, center = homeCenter)
            }
        }
    }
}
