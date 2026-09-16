package com.example.glowink.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.*

/**
 * Componentes de Monetización Neón para Glowink.
 */

@Composable
fun GlowBannerAd(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF0F0A21))
            .border(1.dp, Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PUBLICIDAD NEÓN", fontSize = 9.sp, color = OnSurfaceMuted, letterSpacing = 1.sp)
            Text("¡Obtén 500 GlowCoins extra hoy! Toca aquí", fontSize = 13.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GlowPremiumCard(
    onUpgradeClick: () -> Unit
) {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        borderBrush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8C00))),
        borderWidth = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💎 GLOW PREMIUM", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sin anuncios • Juegos exclusivos • Skins legendarias",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onUpgradeClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("MEJORAR AHORA", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun GlowRewardedAdButton(
    onAdComplete: () -> Unit
) {
    var showLoading by remember { mutableStateOf(false) }
    
    if (showLoading) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF1B1233),
            confirmButton = { },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(color = NeonLime)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando video de recompensa...", color = Color.White, textAlign = TextAlign.Center)
                }
            }
        )
        
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            showLoading = false
            onAdComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(NeonLime.copy(alpha = 0.2f), ElectricCyan.copy(alpha = 0.2f))))
            .border(1.dp, NeonLime, RoundedCornerShape(14.dp))
            .clickable { showLoading = true },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📺", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text("VER VIDEO PARA 🪙 50 GRATIS", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun GameActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(50),
        modifier = Modifier.background(NeonGradientPrimary, RoundedCornerShape(50))
    ) {
        Text(text, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}
