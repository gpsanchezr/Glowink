package com.example.glowink.ui.games

import com.example.glowink.ui.components.GameActionButton

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.theme.ObsidianBackground
import com.example.glowink.ui.theme.OnSurfaceMuted
import com.example.glowink.ui.theme.UltravioletPurple
import kotlinx.coroutines.delay

/**
 * "Cyber Quiz": trivia original de cultura gamer / tecnología / cyberpunk, escrita a mano
 * para Glowink — ninguna pregunta se copió de un banco de trivia existente. 10 preguntas de
 * opción múltiple, 12 segundos cada una; contestar rápido no da puntos extra (a propósito,
 * para no presionar de más), pero sí hay una racha de aciertos consecutivos.
 */

private data class QuizQuestion(val question: String, val options: List<String>, val correctIndex: Int)

private val quizBank = listOf(
    QuizQuestion("¿Qué significa la sigla \"RGB\" en pantallas y periféricos gamer?", listOf("Red, Green, Blue", "Rapid Game Boost", "Render Graphics Buffer", "Real Gamer Badge"), 0),
    QuizQuestion("¿Cuál de estos NO es un motor de videojuegos?", listOf("Unity", "Unreal Engine", "Godot", "Photoshop"), 3),
    QuizQuestion("En jerga gamer, ¿qué es hacer \"grindear\"?", listOf("Abandonar una partida", "Repetir una tarea para ganar experiencia o recursos", "Perder a propósito", "Chatear durante la partida"), 1),
    QuizQuestion("¿Qué unidad se usa para medir cuántas veces se actualiza una pantalla por segundo?", listOf("FPS de audio", "Hercios (Hz)", "Bits por segundo", "Lúmenes"), 1),
    QuizQuestion("¿Qué significa \"NPC\" en un videojuego?", listOf("New Player Character", "Non-Playable Character", "Network Player Connection", "Next Puzzle Challenge"), 1),
    QuizQuestion("¿Cuál de estos lenguajes se usa para el desarrollo nativo de apps Android?", listOf("Swift", "Kotlin", "Ruby", "PHP"), 1),
    QuizQuestion("¿Qué significa la sigla \"AR\" cuando hablamos de cámaras con filtros?", listOf("Audio Render", "Augmented Reality (Realidad Aumentada)", "Auto Refresh", "Adaptive Resolution"), 1),
    QuizQuestion("¿Qué es un \"speedrun\"?", listOf("Un torneo de baile", "Completar un juego lo más rápido posible", "Un tipo de conexión a internet", "Un modo de bajo consumo de batería"), 1),
    QuizQuestion("¿Cuál de estas NO es una consola de videojuegos?", listOf("PlayStation", "Xbox", "GeForce", "Nintendo Switch"), 2),
    QuizQuestion("En Firebase, ¿qué servicio se usa para guardar datos en tiempo real como en Glowink?", listOf("Firestore", "Photoshop Cloud", "AdMob", "Google Docs"), 0)
)

private enum class QuizPhase { INTRO, QUESTION, ANSWERED, FINISHED }

@Composable
fun QuizGameScreen(
    onExit: () -> Unit,
    onGameOver: (score: Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var phase by remember { mutableStateOf(QuizPhase.INTRO) }
    var questionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf(-1) }
    var timeLeft by remember { mutableStateOf(12) }
    var reported by remember { mutableStateOf(false) }

    val questions = remember { quizBank.shuffled() }
    val current = questions.getOrNull(questionIndex)

    fun goToNext() {
        selectedOption = -1
        timeLeft = 12
        if (questionIndex >= questions.lastIndex) {
            phase = QuizPhase.FINISHED
        } else {
            questionIndex += 1
            phase = QuizPhase.QUESTION
        }
    }

    fun answer(index: Int) {
        if (phase != QuizPhase.QUESTION) return
        com.example.glowink.util.GlowSoundManager.playGameAction(context)
        selectedOption = index
        phase = QuizPhase.ANSWERED
        if (index == current?.correctIndex) {
            streak += 1
            score += 10 + (streak - 1).coerceAtMost(5) * 2
        } else {
            streak = 0
        }
    }

    LaunchedEffect(phase, questionIndex) {
        if (phase == QuizPhase.QUESTION) {
            timeLeft = 12
            while (timeLeft > 0 && phase == QuizPhase.QUESTION) {
                delay(1000)
                timeLeft -= 1
            }
            if (phase == QuizPhase.QUESTION) {
                answer(-1) // se acabó el tiempo: cuenta como fallo, sin seleccionar ninguna
            }
        }
    }

    LaunchedEffect(phase) {
        if (phase == QuizPhase.ANSWERED) {
            delay(1400)
            goToNext()
        }
    }

    if (phase == QuizPhase.FINISHED && !reported) {
        reported = true
        onGameOver(score)
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(ObsidianBackground, Color(0xFF160A2E))))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("← Salir", color = ElectricCyan, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onExit))
                    Text("CYBER QUIZ 🧠", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("★ $score", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (phase == QuizPhase.QUESTION || phase == QuizPhase.ANSWERED) {
                    Spacer(modifier = Modifier.height(18.dp))

                    LinearProgressIndicator(
                        progress = (questionIndex + 1) / questions.size.toFloat(),
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                        color = UltravioletPurple,
                        trackColor = Color(0x22FFFFFF)
                    )
                    Text(
                        "Pregunta ${questionIndex + 1} de ${questions.size} · racha $streak 🔥",
                        color = OnSurfaceMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            "⏱ $timeLeft s",
                            color = if (timeLeft <= 4) Color(0xFFFF6B6B) else ElectricCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = current?.question ?: "",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    current?.options?.forEachIndexed { index, option ->
                        val isCorrect = index == current.correctIndex
                        val isSelected = index == selectedOption
                        val bg = when {
                            phase == QuizPhase.ANSWERED && isCorrect -> NeonLime.copy(alpha = 0.28f)
                            phase == QuizPhase.ANSWERED && isSelected && !isCorrect -> Color(0xFFFF6B6B).copy(alpha = 0.28f)
                            else -> Color(0x14FFFFFF)
                        }
                        val border = when {
                            phase == QuizPhase.ANSWERED && isCorrect -> NeonLime
                            phase == QuizPhase.ANSWERED && isSelected && !isCorrect -> Color(0xFFFF6B6B)
                            else -> Color(0x2AFFFFFF)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(bg)
                                .border(1.5.dp, border, RoundedCornerShape(14.dp))
                                .clickable(enabled = phase == QuizPhase.QUESTION) { answer(index) }
                                .padding(14.dp)
                        ) {
                            Text(option, color = Color.White, fontSize = 14.5.sp)
                        }
                    }
                }

                if (phase == QuizPhase.INTRO) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🧠", fontSize = 48.sp)
                            Text("Cyber Quiz", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, modifier = Modifier.padding(top = 10.dp))
                            Text(
                                "${questions.size} preguntas de cultura gamer y tecnología. 12 segundos por pregunta.",
                                color = OnSurfaceMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            GameActionButton(text = "¡Empezar!") { phase = QuizPhase.QUESTION }
                        }
                    }
                }

                if (phase == QuizPhase.FINISHED) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏆", fontSize = 48.sp)
                            Text("¡Quiz terminado!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            Text("Puntaje final: $score", color = NeonLime, fontSize = 17.sp, modifier = Modifier.padding(top = 6.dp))
                            Spacer(modifier = Modifier.height(18.dp))
                            GameActionButton(text = "Jugar de nuevo") {
                                phase = QuizPhase.INTRO
                                questionIndex = 0
                                score = 0
                                streak = 0
                                selectedOption = -1
                                reported = false
                            }
                        }
                    }
                }
            }
        }
    }
}
