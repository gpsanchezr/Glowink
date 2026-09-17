package com.example.glowink.ui.games.glowblast

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.components.GlowCard
import com.example.glowink.ui.components.GlowPrimaryButton
import com.example.glowink.ui.components.GlowSecondaryButton
import com.example.glowink.ui.games.glowblast.model.GlowAvatarConfig
import com.example.glowink.ui.games.glowblast.renderer.GlowCharacterCanvas
import com.example.glowink.ui.games.glowblast.renderer.GlowCharacterPresets
import com.example.glowink.ui.theme.*

/**
 * FASE 14: Pantalla de Resultados de Fin de Partida y Recompensas Cyberpunk para Glow Blast.
 */
@Composable
fun GlowBlastResultScreen(
    isVictory: Boolean = true,
    score: Int = 1250,
    coinsEarned: Int = 350,
    xpEarned: Int = 120,
    selectedCharacter: GlowAvatarConfig = GlowCharacterPresets.NEO,
    onPlayAgain: () -> Unit = {},
    onMenu: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        saveMatchResultsLocally(context, coinsEarned, xpEarned)
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GlowCard(
                        shape = RoundedCornerShape(24.dp),
                        borderColor = if (isVictory) NeonLime else Color(0xFFFF2222),
                        borderWidth = 2.dp,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isVictory) "🏆 ¡VICTORIA CIBERNÉTICA!" else "💀 DERROTA EN COMBATE",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isVictory) NeonLime else Color(0xFFFF2222),
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = if (isVictory) "Dominaste la arena de Glow Blast" else "Mejora tus stats y reinténtalo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33100B2A))
                            .border(1.5.dp, ElectricCyan, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        GlowCharacterCanvas(
                            config = selectedCharacter,
                            modifier = Modifier.size(150.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    GlowCard(
                        shape = RoundedCornerShape(20.dp),
                        borderColor = ElectricCyan,
                        borderWidth = 1.5.dp,
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "TABLA DE POSICIONES DE LA PARTIDA",
                            color = ElectricCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val leaderboard = listOf(
                            Triple("1. Tú (Líder)", "$score pts", true),
                            Triple("2. LunaStar", "850 pts", false),
                            Triple("3. NeoFire", "620 pts", false),
                            Triple("4. Zeta", "310 pts", false)
                        )

                        leaderboard.forEach { (name, pts, isUser) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isUser) Color(0x3300F0FF) else Color(0x11FFFFFF))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, color = if (isUser) Color.White else OnSurfaceMuted, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                Text(pts, color = NeonLime, fontWeight = FontWeight.Black, fontSize = 12.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF003366), Color(0xFF330066))))
                            .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🪙 +$coinsEarned", color = NeonLime, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text("GlowCoins", color = Color.White.copy(alpha = 0.7f), fontSize = 10.5.sp)
                            }

                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.2f)))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⭐ +$xpEarned XP", color = ElectricCyan, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text("Experiencia", color = Color.White.copy(alpha = 0.7f), fontSize = 10.5.sp)
                            }

                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.2f)))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎁 +200", color = Color(0xFFFFD24C), fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text("Bonus Misión", color = Color.White.copy(alpha = 0.7f), fontSize = 10.5.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlowPrimaryButton(
                        text = "▶ JUGAR DE NUEVO",
                        onClick = onPlayAgain,
                        containerColor = Color(0xFFFF007F),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlowSecondaryButton(
                            text = "🏠 MENÚ",
                            onClick = onMenu,
                            modifier = Modifier.weight(1f),
                            borderColor = ElectricCyan,
                            textColor = Color.White,
                            fontSize = 12.sp,
                            contentPadding = PaddingValues(vertical = 10.dp)
                        )

                        GlowSecondaryButton(
                            text = "CONTINUAR ▶",
                            onClick = onContinue,
                            modifier = Modifier.weight(1f),
                            borderColor = NeonLime,
                            textColor = NeonLime,
                            fontSize = 12.sp,
                            contentPadding = PaddingValues(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun saveMatchResultsLocally(context: Context, coinsEarned: Int, xpEarned: Int) {
    try {
        val prefs = context.getSharedPreferences("GlowinkPrefs", Context.MODE_PRIVATE)
        val currentCoins = prefs.getInt("glow_coins", 1500)
        val currentXp = prefs.getInt("glow_xp", 500)

        prefs.edit()
            .putInt("glow_coins", currentCoins + coinsEarned)
            .putInt("glow_xp", currentXp + xpEarned)
            .apply()
    } catch (_: Exception) {
    }
}
