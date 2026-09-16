package com.example.glowink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.data.CommunityStyles
import com.example.glowink.ui.avatar.GlowAvatar
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(
    viewModel: ChatViewModel,
    communityId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.chatsListUiState.collectAsState()
    val community = uiState.communities.find { it.id == communityId }
    val allUsers = (uiState.realUsersState as? com.example.glowink.data.RealUsersState.Success)?.users ?: emptyList()
    val members = allUsers.filter { it.id in (community?.members ?: emptyList()) }

    if (community == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Comunidad no encontrada", color = Color.White)
        }
        return
    }

    val backgroundBrush = when (community.style) {
        CommunityStyles.FIRE -> Brush.verticalGradient(listOf(Color(0xFF420000), Color(0xFFFF4500), Color(0xFF2B0000)))
        CommunityStyles.ICE -> Brush.verticalGradient(listOf(Color(0xFF003366), Color(0xFFE0FFFF), Color(0xFF001F3F)))
        CommunityStyles.SUMMER -> Brush.verticalGradient(listOf(Color(0xFF00BFFF), Color(0xFFFFFACD), Color(0xFF228B22)))
        else -> Brush.verticalGradient(listOf(Color(0xFF32CD32), Color(0xFFF0E68C), Color(0xFF6B8E23)))
    }

    val themeEmoji = when (community.style) {
        CommunityStyles.FIRE -> "🔥"
        CommunityStyles.ICE -> "🧊"
        CommunityStyles.SUMMER -> "☀️"
        else -> "🌸"
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(community.name, fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.3f))
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
            // Elementos visuales extra según el estilo
            when (community.style) {
                CommunityStyles.FIRE -> FireParticles()
                CommunityStyles.ICE -> SnowOverlay()
                else -> Unit
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(themeEmoji, fontSize = 60.sp)
                Text(community.description, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("INTEGRANTES DEL SQUAD (${members.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(members) { member ->
                        MemberGridItem(member)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberGridItem(user: com.example.glowink.data.User) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.2f)).padding(4.dp)) {
            GlowAvatar(config = user.avatarConfig, profileImageUrl = user.profileImageUrl, size = 62.dp, animate = false)
        }
        Text(user.username, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun FireParticles() {
    // Placeholder para efectos visuales de fuego
}

@Composable
private fun SnowOverlay() {
    // Placeholder para efectos visuales de nieve
}
