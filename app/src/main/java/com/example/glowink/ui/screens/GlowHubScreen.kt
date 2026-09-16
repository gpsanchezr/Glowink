package com.example.glowink.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.data.RealUsersState
import com.example.glowink.data.User
import com.example.glowink.ui.avatar.GlowAvatar
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatViewModel
import com.example.glowink.ui.viewmodel.ChatsListUiState

@Composable
fun GlowHubScreen(
    viewModel: ChatViewModel,
    onChatClick: (friendId: String) -> Unit = {},
    onAddStoryClick: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    onNavigateToClan: () -> Unit = {},
    onLogout: () -> Unit = {},
    onTabClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.chatsListUiState.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }

    Scaffold(
        containerColor = Color(0xFF070514),
        bottomBar = {
            GlowinkBottomNavigationBar(selectedTab = 0, onTabClick = onTabClick)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToContacts,
                containerColor = Color.Transparent,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(ElectricCyan, Color(0xFF007FFF))))
                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Add, "Nuevo Chat", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Background with subtle neon rays
            HubBackgroundEffect()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                HubHeader(
                    user = uiState.currentUser,
                    onStatusChange = { viewModel.updateUserStatus(it) },
                    onShopClick = onNavigateToShop,
                    onAvatarClick = { onTabClick(2) }
                )

                StoriesSection(
                    currentUser = uiState.currentUser,
                    friends = uiState.activeChats,
                    onAddStoryClick = onAddStoryClick,
                    onVerMasClick = { /* Ver mas logic */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                HubSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onFilterSelect = { selectedFilter = it }
                )

                ClanFeaturedCard(onClanClick = onNavigateToClan)

                ConversationsSection(
                    uiState = uiState,
                    searchQuery = searchQuery,
                    filter = selectedFilter,
                    onChatClick = onChatClick
                )

                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
        }
    }
}

@Composable
private fun HubBackgroundEffect() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Sutiles rayos de luz neon
        drawPath(
            path = Path().apply {
                moveTo(w * 0.8f, 0f)
                lineTo(w, h * 0.2f)
            },
            color = ElectricCyan.copy(alpha = 0.05f),
            style = Stroke(width = 100f)
        )
        
        drawPath(
            path = Path().apply {
                moveTo(0f, h * 0.6f)
                lineTo(w * 0.4f, h)
            },
            color = UltravioletPurple.copy(alpha = 0.05f),
            style = Stroke(width = 150f)
        )
    }
}

@Composable
private fun HubHeader(
    user: User?,
    onStatusChange: (String) -> Unit,
    onShopClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF007F).copy(alpha = 0.2f))
                    .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("💬", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "GLOW HUB",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Conecta y juega en tiempo real",
                    fontSize = 12.sp,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Status/Power Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFFF007F), CircleShape)
                    .clickable { 
                        val nextStatus = when(user?.status) {
                            "ONLINE" -> "AWAY"
                            "AWAY" -> "BUSY"
                            else -> "ONLINE"
                        }
                        onStatusChange(nextStatus)
                    },
                contentAlignment = Alignment.Center
            ) {
                NeonLogoutIcon(color = Color(0xFFFF007F), modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Coins Counter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFB8860B).copy(alpha = 0.3f), Color(0xFFDAA520).copy(alpha = 0.3f))))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onShopClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${user?.glowCoins ?: 0}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("＋", color = ElectricCyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(2.dp, Brush.sweepGradient(listOf(ElectricCyan, UltravioletPurple, ElectricCyan)), CircleShape)
                    .clickable { onAvatarClick() }
            ) {
                GlowAvatar(
                    config = user?.avatarConfig ?: com.example.glowink.data.Avatar3DConfig(),
                    profileImageUrl = user?.profileImageUrl,
                    animate = false,
                    modifier = Modifier.fillMaxSize().padding(2.dp)
                )
            }
        }
    }
}

@Composable
private fun StoriesSection(
    currentUser: User?,
    friends: List<User>,
    onAddStoryClick: () -> Unit,
    onVerMasClick: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Tu Historia
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onAddStoryClick() }) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF007FFF).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFF007FFF), modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Tu Historia", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }

        items(friends.take(5)) { friend ->
            StoryItem(friend)
        }

        // Ver más
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onVerMasClick() }) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👥", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Ver más", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun StoryItem(user: User) {
    val statusColor = when(user.status) {
        "ONLINE" -> Color(0xFF39FF14)
        "AWAY" -> Color(0xFFFFD24C)
        else -> Color(0xFFFF4444)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .border(2.5.dp, Brush.sweepGradient(listOf(UltravioletPurple, ElectricCyan, UltravioletPurple)), CircleShape)
                .padding(4.dp)
        ) {
            GlowAvatar(
                config = user.avatarConfig,
                profileImageUrl = user.profileImageUrl,
                animate = false,
                modifier = Modifier.fillMaxSize().padding(2.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
            Spacer(modifier = Modifier.width(4.dp))
            Text(user.username, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HubSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(54.dp),
        placeholder = { Text("Buscar en el chat...", color = OnSurfaceMuted) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
        trailingIcon = { Text("⚙️", modifier = Modifier.padding(end = 12.dp)) },
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0x1A1E1735),
            unfocusedContainerColor = Color(0x1A1E1735),
            focusedBorderColor = Color(0xFF007FFF),
            unfocusedBorderColor = Color(0xFF007FFF).copy(alpha = 0.5f),
            focusedTextColor = Color.White
        )
    )
}

@Composable
private fun FilterChipsRow(selectedFilter: String, onFilterSelect: (String) -> Unit) {
    val filters = listOf("Todos", "Amigos", "Clan", "Directos")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF007FFF), UltravioletPurple))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .border(
                        1.dp,
                        if (isSelected) Color.Transparent else Color(0xFF007FFF).copy(alpha = 0.6f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onFilterSelect(filter) }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) Text("👥", fontSize = 14.sp)
                    else if (filter == "Clan") Text("＋", fontSize = 14.sp)
                    
                    if (isSelected || filter == "Clan") Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = filter,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ClanFeaturedCard(onClanClick: () -> Unit) {
    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(110.dp)
            .clickable { onClanClick() },
        shape = RoundedCornerShape(24.dp),
        borderColor = UltravioletPurple.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFF0F0A21), Color(0xFF1B1233)))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            // Clan Logo Placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4A00E0).copy(alpha = 0.2f))
                    .border(2.dp, Color(0xFF4A00E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🐺", fontSize = 32.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Glow Clan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("👑", fontSize = 14.sp)
                }
                Text("Juntos somos más fuertes", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👥", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("24 miembros", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // Level Info
            Column(modifier = Modifier.padding(end = 20.dp), horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD24C), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nivel 3", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Progress Bar
                Box(modifier = Modifier.width(80.dp).height(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))) {
                    Box(modifier = Modifier.fillMaxWidth(0.7f).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFFFF007F), UltravioletPurple))))
                }
            }
        }
    }
}

@Composable
private fun ConversationsSection(
    uiState: ChatsListUiState,
    searchQuery: String,
    filter: String,
    onChatClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👥", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conversaciones del Clan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Ver todas >",
                color = UltravioletPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* Ver todas logic */ }
            )
        }

        val filteredUsers = uiState.activeChats.filter { 
            it.username.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
        }

        if (filteredUsers.isEmpty()) {
            EmptyHubState()
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filteredUsers.forEach { user ->
                    HubChatRow(user = user, onClick = { onChatClick(user.id) })
                }
            }
        }
    }
}

@Composable
private fun HubChatRow(user: User, onClick: () -> Unit) {
    val statusColor = if (user.status == "ONLINE") Color(0xFF39FF14) else Color.Transparent
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0x1A1E1735))
            .border(1.dp, Color(0xFF007FFF).copy(alpha = 0.3f), RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            // Avatar with status
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, Brush.sweepGradient(listOf(ElectricCyan, UltravioletPurple)), CircleShape)
                ) {
                    GlowAvatar(
                        config = user.avatarConfig,
                        profileImageUrl = user.profileImageUrl,
                        animate = false,
                        modifier = Modifier.fillMaxSize().padding(3.dp)
                    )
                }
                if (user.status == "ONLINE") {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF070514))
                            .padding(2.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(statusColor))
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.username, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (user.status == "ONLINE") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("• En línea", color = Color(0xFF39FF14), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "¡Vamos al próximo nivel en Culebra Glow! 🐍", // Mock last message
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("2:15 p. m.", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (user.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(UltravioletPurple, Color(0xFF4A00E0))))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${user.unreadCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(">", color = Color(0xFF007FFF), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyHubState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📡", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No encontramos conversaciones", color = Color.White, fontWeight = FontWeight.Bold)
        Text("Intenta con otro nombre o etiqueta", color = ElectricCyan.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}
