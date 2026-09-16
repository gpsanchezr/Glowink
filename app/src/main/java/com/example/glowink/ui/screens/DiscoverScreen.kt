package com.example.glowink.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.data.Community
import com.example.glowink.data.CommunityStyles
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: ChatViewModel,
    onTabClick: (Int) -> Unit = {},
    onCommunityClick: (String) -> Unit = {}
) {
    val uiState by viewModel.chatsListUiState.collectAsState()
    val communities = uiState.communities
    var showCreateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = ObsidianBackground,
        bottomBar = { GlowinkBottomNavigationBar(selectedTab = 3, onTabClick = onTabClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = ElectricCyan,
                shape = CircleShape
            ) {
                Text("＋", fontSize = 24.sp, color = Color.Black)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(ObsidianBackground, Color(0xFF1A0F2E), ObsidianBackground)))
                .padding(20.dp)
        ) {
            Text("DESCUBRIR ✨", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp)
            Text("Explora y crea comunidades reales en Glowink", fontSize = 13.sp, color = ElectricCyan)

            Spacer(modifier = Modifier.height(18.dp))

            if (communities.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay comunidades aún. ¡Sé el primero en crear una!", color = OnSurfaceMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(communities) { community ->
                        CommunityCard(community = community, onClick = { onCommunityClick(community.id) }) {
                            viewModel.joinCommunity(community.id)
                            Toast.makeText(context, "¡Te has unido a ${community.name}!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCommunityDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, style ->
                viewModel.createCommunity(name, desc, style)
                showCreateDialog = false
                Toast.makeText(context, "Comunidad creada con éxito 🚀", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun CommunityCard(community: Community, onClick: () -> Unit, onJoin: () -> Unit) {
    val styleIcon = when (community.style) {
        CommunityStyles.FIRE -> "🔥"
        CommunityStyles.ICE -> "🧊"
        CommunityStyles.SUMMER -> "☀️"
        else -> "🌸"
    }
    
    GlassContainer(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        borderColor = Color.White.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(listOf(UltravioletPurple, ElectricCyan))),
                contentAlignment = Alignment.Center
            ) {
                Text(styleIcon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(community.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${community.members.size} miembros", color = NeonLime, fontSize = 11.sp)
                Text(community.description, color = OnSurfaceMuted, fontSize = 12.sp, maxLines = 1)
            }
            Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                shape = RoundedCornerShape(50)
            ) {
                Text("Unirse", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCommunityDialog(onDismiss: () -> Unit, onCreate: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(CommunityStyles.SPRING) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1233),
        title = { Text("CREAR COMUNIDAD NEÓN 🌌", color = ElectricCyan, fontSize = 18.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text("Selecciona el Estilo:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    StyleOption(CommunityStyles.FIRE, "🔥", selectedStyle == CommunityStyles.FIRE) { selectedStyle = it }
                    StyleOption(CommunityStyles.ICE, "🧊", selectedStyle == CommunityStyles.ICE) { selectedStyle = it }
                    StyleOption(CommunityStyles.SUMMER, "☀️", selectedStyle == CommunityStyles.SUMMER) { selectedStyle = it }
                    StyleOption(CommunityStyles.SPRING, "🌸", selectedStyle == CommunityStyles.SPRING) { selectedStyle = it }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onCreate(name, desc, selectedStyle) }, colors = ButtonDefaults.buttonColors(containerColor = NeonLime)) {
                Text("CREAR", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = OnSurfaceMuted) }
        }
    )
}

@Composable
private fun StyleOption(style: String, emoji: String, isSelected: Boolean, onSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isSelected) ElectricCyan else Color(0x1AFFFFFF))
            .clickable { onSelect(style) },
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 20.sp)
    }
}
