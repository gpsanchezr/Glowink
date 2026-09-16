package com.example.glowink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatViewModel

data class ClanRank(
    val rank: Int,
    val name: String,
    val emblem: String,
    val wins: Int,
    val level: Int,
    val isUserClan: Boolean = false
)

/**
 * Pantalla de Ranking de Clanes y Temporadas para Glowink.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClanRankingScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val clanList = remember {
        listOf(
            ClanRank(1, "Glow Clan 👑", "🐺", 142, 5, isUserClan = true),
            ClanRank(2, "Cyber Titans", "🤖", 128, 4),
            ClanRank(3, "Neon Squad", "⚡", 115, 4),
            ClanRank(4, "Volcanic Force", "🔥", 98, 3),
            ClanRank(5, "Cosmic Legion", "👾", 84, 3),
            ClanRank(6, "Alpha Wolves", "🐺", 76, 2),
            ClanRank(7, "Shadow Knights", "🥷", 65, 2),
            ClanRank(8, "Speed Racers", "🏎️", 54, 2),
            ClanRank(9, "Cyber Archers", "🏹", 42, 1),
            ClanRank(10, "Glow Warriors", "⚔️", 38, 1)
        )
    }

    Scaffold(
        containerColor = ObsidianBackground,
        topBar = {
            TopAppBar(
                title = { Text("🏆 RANKING DE TEMPORADA", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = ElectricCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF151026))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Color(0xFF151026), Color(0xFF0A071A))))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Selector de Temporada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x221E1735))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Temporada 1 — Neón Genesis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PODIO TOP 3 (1.2x Oro, 1.1x Plata, 1.0x Bronce)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // #2 Plata
                PodiumCard(clan = clanList[1], rankColor = Color(0xFFC0C0C0), scale = 1.05f, medal = "🥈")
                // #1 Oro
                PodiumCard(clan = clanList[0], rankColor = Color(0xFFFFD700), scale = 1.2f, medal = "🥇")
                // #3 Bronce
                PodiumCard(clan = clanList[2], rankColor = Color(0xFFCD7F32), scale = 1.0f, medal = "🥉")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lista 4 al 100
            Text("Tabla General de Clanes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(clanList.drop(3)) { _, clan ->
                    ClanRankRow(clan = clan)
                }
            }
        }
    }
}

@Composable
private fun PodiumCard(clan: ClanRank, rankColor: Color, scale: Float, medal: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width((100 * scale).dp)
    ) {
        Text(medal, fontSize = (28 * scale).sp)
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((110 * scale).dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(rankColor.copy(alpha = 0.25f), Color(0xFF1E1735))))
                .border(1.5.dp, rankColor, RoundedCornerShape(18.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(clan.emblem, fontSize = (32 * scale).sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(clan.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${clan.wins} Victorias", color = rankColor, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ClanRankRow(clan: ClanRank) {
    val borderColor = if (clan.isUserClan) ElectricCyan else Color.White.copy(alpha = 0.15f)
    val bgColor = if (clan.isUserClan) Color(0x3300F0FF) else Color(0x221E1735)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#${clan.rank}", color = ElectricCyan, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Text(clan.emblem, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(clan.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Nivel ${clan.level}", color = OnSurfaceMuted, fontSize = 11.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${clan.wins} Victorias", color = NeonLime, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}
