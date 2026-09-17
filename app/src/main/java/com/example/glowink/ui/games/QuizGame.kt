package com.example.glowink.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.components.GameActionButton
import com.example.glowink.ui.theme.*
import com.example.glowink.util.GlowSoundManager
import kotlinx.coroutines.delay

private enum class QuizPhase { INTRO, QUESTION, ANSWERED, FINISHED }

private data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

private val quizBank = listOf(
    QuizQuestion("¿Cuál es el lenguaje principal para desarrollo Android nativo?", listOf("Kotlin", "Java", "Python", "Swift"), 0),
    QuizQuestion("¿Qué componente maneja la interfaz reactiva en Compose?", listOf("View", "Composable", "Activity", "Intent"), 1),
    QuizQuestion("¿Qué base de datos de Firebase ofrece sincronización en tiempo real?", listOf("Realtime DB", "Firestore", "SQLite", "Room"), 1),
    QuizQuestion("¿Cuál es el color primario de Neón en Glowink?", listOf("Cian Eléctrico", "Verde Neón", "Fucsia", "Dorado"), 0)
)

@Composable
fun QuizGameScreen(
    onExit: () -> Unit,
    onGameOver: (score: Int) -> Unit
) {
    val context = LocalContext.current
    var phase by remember { mutableStateOf(QuizPhase.INTRO) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableIntStateOf(-1) }
    var timeLeft by remember { mutableIntStateOf(12) }
    var reported by remember { mutableStateOf(false) }

    val questions = remember { quizBank.shuffled() }
    val current = questions.getOrNull(questionIndex)

    DisposableEffect(Unit) {
        onDispose {
            phase = QuizPhase.INTRO
        }
    }

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
        GlowSoundManager.playGameAction(context)
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
                answer(-1)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("← Volver", color = ElectricCyan, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onExit() })
                Text("CYBER QUIZ 🧠", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("SCORE: $score", color = NeonLime, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (phase) {
                QuizPhase.INTRO -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DEMUESTRA TUS CONOCIMIENTOS", color = ElectricCyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(16.dp))
                            GameActionButton(text = "¡EMPEZAR QUIZ!", onClick = { phase = QuizPhase.QUESTION })
                        }
                    }
                }
                QuizPhase.QUESTION, QuizPhase.ANSWERED -> {
                    current?.let { q ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Pregunta ${questionIndex + 1}/${questions.size}", color = ElectricCyan, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(q.question, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(20.dp))

                            q.options.forEachIndexed { i, opt ->
                                val btnColor = when {
                                    phase == QuizPhase.ANSWERED && i == q.correctIndex -> NeonLime
                                    phase == QuizPhase.ANSWERED && i == selectedOption -> Color.Red
                                    else -> Color(0xFF1E1735)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(btnColor)
                                        .border(1.dp, ElectricCyan, RoundedCornerShape(14.dp))
                                        .clickable(enabled = phase == QuizPhase.QUESTION) { answer(i) }
                                        .padding(16.dp)
                                ) {
                                    Text(opt, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
                QuizPhase.FINISHED -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("¡QUIZ COMPLETADO!", color = NeonLime, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            Text("Puntaje Final: $score", color = Color.White, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            GameActionButton(text = "JUGAR DE NUEVO", onClick = { questionIndex = 0; score = 0; phase = QuizPhase.QUESTION; reported = false })
                        }
                    }
                }
            }
        }
    }
}
