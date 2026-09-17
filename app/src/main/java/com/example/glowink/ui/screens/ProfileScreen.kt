package com.example.glowink.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.data.GameStats
import com.example.glowink.data.User
import com.example.glowink.ui.avatar.GlowAvatarFrame
import com.example.glowink.ui.components.GlowPrimaryButton
import com.example.glowink.ui.components.GlowSecondaryButton
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatViewModel

@Composable
fun ProfileScreen(
    viewModel: ChatViewModel,
    onLogout: () -> Unit = {},
    onEditAvatar: () -> Unit = {},
    onOpen3DViewer: () -> Unit = {},
    onTabClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.chatsListUiState.collectAsState()
    val user = uiState.currentUser
    val isLoading = uiState.isLoading
    val context = LocalContext.current

    ProfileScreenContent(
        user = user,
        isLoading = isLoading,
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        onEditAvatar = onEditAvatar,
        onOpen3DViewer = onOpen3DViewer,
        onChangeProfileImageClick = onEditAvatar,
        onTabClick = onTabClick,
        onStatusChange = { newStatus -> viewModel.updateUserStatus(newStatus) }
    )
}

/**
 * Componente puramente visual (Stateless) para la pantalla de Perfil.
 */
@Composable
fun ProfileScreenContent(
    user: User? = null,
    isLoading: Boolean = false,
    onLogout: () -> Unit = {},
    onEditAvatar: () -> Unit = {},
    onOpen3DViewer: () -> Unit = {},
    onChangeProfileImageClick: () -> Unit = {},
    onTabClick: (Int) -> Unit = {},
    onStatusChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    Scaffold(
        containerColor = Color(0xFF070514),
        bottomBar = {
            GlowinkBottomNavigationBar(selectedTab = 2, onTabClick = onTabClick)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            com.example.glowink.ui.theme.GlowinkMainBackground()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading && user == null) {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UltravioletPurple)
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "TU IDENTIDAD NEÓN",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text("Nivel ${user?.nivel ?: 1} • Squad Elite", color = ElectricCyan, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Contenedor Circular de Avatar de 120dp con Headshot Busto
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(ElectricCyan.copy(alpha = 0.35f), Color.Transparent)))
                        )
                        GlowAvatarFrame(
                            config = user?.avatarConfig ?: com.example.glowink.data.Avatar3DConfig(),
                            size = 120.dp,
                            isHeadshot = true,
                            ringBrush = Brush.sweepGradient(listOf(ElectricCyan, NeonLime, UltravioletPurple, ElectricCyan)),
                            ringWidth = 4.dp,
                            backgroundColor = Color(0xFF1A1430)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(ElectricCyan, UltravioletPurple)))
                                .border(1.5.dp, Color.White, CircleShape)
                                .clickable(onClick = onEditAvatar),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✏️", fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = user?.username ?: "Buscando...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    
                    // Selector de Estado en Perfil
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip("ONLINE", "🟢", user?.status == "ONLINE") { onStatusChange("ONLINE") }
                        StatusChip("AWAY", "🌙", user?.status == "AWAY") { onStatusChange("AWAY") }
                        StatusChip("BUSY", "🔴", user?.status == "BUSY") { onStatusChange("BUSY") }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Monetización: Glow Premium
                    com.example.glowink.ui.components.GlowPremiumCard(
                        onUpgradeClick = {
                            Toast.makeText(context, "Procesando mejora a Premium... 💎", Toast.LENGTH_LONG).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GlassContainer(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        borderColor = ElectricCyan.copy(alpha = 0.4f)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("ESTADÍSTICAS DE COMBATE", fontSize = 12.sp, color = OnSurfaceMuted, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            val stats = user?.gameStats
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatChip("🐍", "${stats?.highScoreCulebra ?: 0}", "Culebra")
                                StatChip("⚡", "${stats?.victoriasDuelo ?: 0}", "Duelos")
                                StatChip("🏁", "${stats?.victoriasCarrera ?: 0}", "Carreras")
                                StatChip("⭕", "${stats?.victoriasTresEnRaya ?: 0}", "3 en Raya")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlowSecondaryButton(
                            text = "Avatar 🎨",
                            onClick = onEditAvatar,
                            modifier = Modifier.weight(1f),
                            borderColor = ElectricCyan,
                            textColor = Color.White
                        )
                        GlowPrimaryButton(
                            text = "Visor 3D 🧊",
                            onClick = onOpen3DViewer,
                            modifier = Modifier.weight(1f),
                            containerColor = UltravioletPurple,
                            contentColor = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GlowSecondaryButton(
                        text = "CERRAR SESIÓN",
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = Color(0xFFFF4444),
                        textColor = Color(0xFFFF4444),
                        icon = { NeonLogoutIcon(color = Color(0xFFFF4444)) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatusChip(status: String, emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White.copy(0.1f) else Color.Transparent)
            .border(1.dp, if (isSelected) ElectricCyan else Color.White.copy(0.1f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 16.sp)
    }
}

@Composable
private fun RowScope.StatChip(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(emoji, fontSize = 18.sp)
        Text(value, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Black)
        Text(label, fontSize = 9.5.sp, color = OnSurfaceMuted)
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, valueColor: Color = Color.White) {
    Column {
        Text(text = label, fontSize = 11.sp, color = OnSurfaceMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(text = value, fontSize = 17.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    GlowinkTheme {
        ProfileScreenContent(
            user = User(
                id = "usr_123456789",
                username = "GlowMaster",
                email = "glowmaster@glowink.com",
                glowCoins = 1500,
                rachaVictorias = 12,
                gameStats = GameStats(
                    highScoreCulebra = 450,
                    victoriasDuelo = 15,
                    victoriasCarrera = 8,
                    victoriasTresEnRaya = 10
                )
            )
        )
    }
}
