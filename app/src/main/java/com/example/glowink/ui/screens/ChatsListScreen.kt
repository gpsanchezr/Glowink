package com.example.glowink.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.data.RealUsersState
import com.example.glowink.data.User
import com.example.glowink.ui.avatar.GlowAvatar
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatViewModel
import com.example.glowink.ui.viewmodel.ChatsListUiState
import kotlinx.coroutines.delay

/**
 * "GLOW HUB" - Centro Social y de Chat en Tiempo Real para Glowink.
 * Reproduce exactamente la jerarquía visual, botones y diseño de la imagen de referencia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    viewModel: ChatViewModel? = if (LocalInspectionMode.current) null else ChatViewModel(),
    onChatClick: (friendId: String) -> Unit = {},
    onAddStoryClick: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    onNavigateToClan: () -> Unit = {},
    onLogout: () -> Unit = {},
    onTabClick: (Int) -> Unit = {}
) {
    if (viewModel == null) return
    val context = LocalContext.current
    val uiState by viewModel.chatsListUiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    
    var selectedStoryUser by remember { mutableStateOf<User?>(null) }
    var showFabMenuDialog by remember { mutableStateOf(false) }
    var showCreateClanDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF070514),
        bottomBar = {
            GlowinkBottomNavigationBar(selectedTab = 0, onTabClick = onTabClick)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFabMenuDialog = true },
                containerColor = Color.Transparent,
                shape = CircleShape,
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(ElectricCyan, Color(0xFF007FFF), UltravioletPurple))
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("💬", fontSize = 24.sp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(NeonLime)
                            .border(1.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("＋", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            HubBackgroundEffect()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. HEADER (Logo, Título, Estado Power, GlowCoins, Avatar)
                HubHeader(
                    user = uiState.currentUser,
                    onStatusChange = { viewModel.updateUserStatus(it) },
                    onShopClick = onNavigateToShop,
                    onAvatarClick = { onTabClick(2) }
                )

                // 2. HISTORIAS / ESTADOS
                StoriesSection(
                    friends = uiState.activeChats,
                    onAddStoryClick = onAddStoryClick,
                    onStoryClick = { user -> selectedStoryUser = user },
                    onMoreClick = onNavigateToContacts
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. BUSCADOR EN EL CHAT
                HubSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                // 4. FILTROS (Todos, Amigos, Clan, Directos)
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onFilterSelect = { selectedFilter = it }
                )

                // 5. GLOW CLAN FEATURED CARD
                ClanFeaturedCard(
                    community = uiState.communities.firstOrNull(),
                    onClanClick = onNavigateToClan
                )

                // 6. LISTA DE CONVERSACIONES DEL CLAN / CHATS
                when (val state = uiState.realUsersState) {
                    is RealUsersState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonLime)
                        }
                    }
                    is RealUsersState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(state.message, color = Color.Red, fontSize = 12.sp)
                        }
                    }
                    is RealUsersState.Success -> {
                        ConversationsSection(
                            uiState = uiState,
                            searchQuery = searchQuery,
                            filter = selectedFilter,
                            onChatClick = onChatClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }

    // Modal de Visualizador de Historias con Animación
    if (selectedStoryUser != null) {
        StoryViewerDialog(
            user = selectedStoryUser!!,
            onDismiss = { selectedStoryUser = null }
        )
    }

    // Modal del Botón Flotante con Animación
    if (showFabMenuDialog) {
        FabActionsMenuDialog(
            onDismiss = { showFabMenuDialog = false },
            onNewChat = {
                showFabMenuDialog = false
                onNavigateToContacts()
            },
            onCreateClan = {
                showFabMenuDialog = false
                showCreateClanDialog = true
            },
            onInviteGame = {
                showFabMenuDialog = false
                onTabClick(1) // Ir a Juegos
            }
        )
    }

    // Modal de Creación de Clan / Squad
    if (showCreateClanDialog) {
        CreateClanDialog(
            onDismiss = { showCreateClanDialog = false },
            onCreateClan = { clanName, clanSlogan ->
                viewModel.createCommunity(clanName, clanSlogan, "FIRE")
                showCreateClanDialog = false
                Toast.makeText(context, "¡Clan '$clanName' fundado exitosamente! 🛡️👑", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun HubBackgroundEffect() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawPath(
            path = Path().apply { moveTo(w * 0.8f, 0f); lineTo(w, h * 0.2f) },
            color = Color(0xFF007FFF).copy(alpha = 0.12f),
            style = Stroke(width = 80f)
        )
        drawPath(
            path = Path().apply { moveTo(0f, h * 0.7f); lineTo(w * 0.3f, h) },
            color = Color(0xFFFF007F).copy(alpha = 0.1f),
            style = Stroke(width = 120f)
        )
    }
}

/**
 * 1. HEADER: Responsive para pantallas pequeñas (<360dp), Estado Power, Coins y Avatar.
 */
@Composable
private fun HubHeader(
    user: User?,
    onStatusChange: (String) -> Unit,
    onShopClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val isNarrow = screenWidthDp < 360
    val innerSpacing = if (isNarrow) 6.dp else 10.dp

    val statusColor = when (user?.status) {
        "AWAY" -> Color(0xFFFFD24C)
        "BUSY" -> Color(0xFFFF4444)
        else -> Color(0xFF39FF14)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo e Identidad Glow Hub
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(if (isNarrow) 40.dp else 46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFF007F).copy(alpha = 0.18f))
                    .border(1.5.dp, Color(0xFFFF007F), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("💬", fontSize = if (isNarrow) 20.sp else 24.sp)
            }

            Spacer(modifier = Modifier.width(innerSpacing))

            Column {
                Text(
                    text = "GLOW HUB",
                    fontSize = if (isNarrow) 22.sp else 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Conecta y juega en tiempo real",
                    fontSize = 11.sp,
                    color = ElectricCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Acciones del Header (Estado Power + GlowCoins + Avatar)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.2f))
                    .border(1.5.dp, statusColor, CircleShape)
                    .bounceClick {
                        val next = when (user?.status) {
                            "ONLINE" -> "AWAY"
                            "AWAY" -> "BUSY"
                            else -> "ONLINE"
                        }
                        onStatusChange(next)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("⏻", color = statusColor, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.width(innerSpacing))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x331E1735))
                    .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .bounceClick { onShopClick() }
                    .padding(horizontal = if (isNarrow) 8.dp else 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${user?.glowCoins ?: 238}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("＋", color = ElectricCyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.width(innerSpacing))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(2.dp, Brush.sweepGradient(listOf(ElectricCyan, UltravioletPurple, ElectricCyan)), CircleShape)
                    .bounceClick { onAvatarClick() }
            ) {
                GlowAvatar(
                    config = user?.avatarConfig ?: com.example.glowink.data.Avatar3DConfig(),
                    profileImageUrl = user?.profileImageUrl,
                    animate = false,
                    modifier = Modifier.fillMaxSize().padding(3.dp)
                )
            }
        }
    }
}

/**
 * 2. HISTORIAS / ESTADOS: Tu historia (+) + Amigos con microanimación bounceClick.
 */
@Composable
private fun StoriesSection(
    friends: List<User>,
    onAddStoryClick: () -> Unit,
    onStoryClick: (User) -> Unit,
    onMoreClick: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.bounceClick { onAddStoryClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0x22007FFF))
                        .border(2.dp, Color(0xFF007FFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar Historia",
                        tint = Color(0xFF007FFF),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tu Historia", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        items(friends.take(6)) { friend ->
            val statusColor = when (friend.status) {
                "ONLINE" -> Color(0xFF39FF14)
                "AWAY" -> Color(0xFFFFD24C)
                else -> Color(0xFFFF4444)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.bounceClick { onStoryClick(friend) }
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(
                            2.5.dp,
                            Brush.sweepGradient(listOf(UltravioletPurple, ElectricCyan, UltravioletPurple)),
                            CircleShape
                        )
                        .padding(5.dp)
                ) {
                    GlowAvatar(
                        config = friend.avatarConfig,
                        profileImageUrl = friend.profileImageUrl,
                        animate = false,
                        modifier = Modifier.fillMaxSize().padding(2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(statusColor))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = friend.username,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.bounceClick { onMoreClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0x11FFFFFF))
                        .border(1.2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👥", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ver más", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

/**
 * 3. BUSCADOR EN EL CHAT.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HubSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(54.dp),
        placeholder = { Text("Buscar en el chat...", color = OnSurfaceMuted, fontSize = 14.5.sp) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(22.dp)) },
        trailingIcon = { Icon(Icons.Default.Settings, null, tint = ElectricCyan, modifier = Modifier.size(20.dp)) },
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0x221E1735),
            unfocusedContainerColor = Color(0x221E1735),
            focusedBorderColor = Color(0xFF007FFF),
            unfocusedBorderColor = Color(0xFF007FFF).copy(alpha = 0.4f),
            focusedTextColor = Color.White
        )
    )
}

/**
 * 4. FILTROS DE CHAT (Todos, Amigos, Clan, Directos) con bounceClick.
 */
@Composable
private fun FilterChipsRow(selectedFilter: String, onFilterSelect: (String) -> Unit) {
    val filters = listOf("Todos", "Amigos", "Clan", "Directos")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter.equals(filter, ignoreCase = true)
            val icon = when (filter) {
                "Todos" -> "👥"
                "Amigos" -> "👥"
                "Clan" -> "🛡️"
                else -> "🚀"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(listOf(Color(0xFF007FFF), UltravioletPurple))
                        } else {
                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.2.dp,
                        color = Color(0xFF007FFF).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .bounceClick { onFilterSelect(filter) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = filter,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 5. GLOW CLAN FEATURED CARD (Dinamizado con comunidades reales).
 */
@Composable
private fun ClanFeaturedCard(
    community: com.example.glowink.data.Community?,
    onClanClick: () -> Unit
) {
    val clanName = community?.name?.ifBlank { "Glow Clan" } ?: "Glow Clan"
    val clanSlogan = community?.description?.ifBlank { "Juntos somos más fuertes" } ?: "Juntos somos más fuertes"
    val memberCount = community?.members?.size ?: 24

    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(120.dp)
            .bounceClick { onClanClick() },
        shape = RoundedCornerShape(28.dp),
        borderColor = UltravioletPurple.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F0A21), Color(0xFF1B1233), Color(0xFF0F0A21))
                    )
                )
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A00E0).copy(alpha = 0.25f))
                        .border(2.dp, Color(0xFF4A00E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐺", fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(clanName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("👑", fontSize = 14.sp)
                    }
                    Text(clanSlogan, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👥", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$memberCount miembros", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD24C), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nivel 3", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFF007F), UltravioletPurple)))
                    )
                }
            }
        }
    }
}

/**
 * 6. SECCIÓN DE CONVERSACIONES DEL CLAN Y CHATS REALES.
 */
@Composable
private fun ConversationsSection(
    uiState: ChatsListUiState,
    searchQuery: String,
    filter: String,
    onChatClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👥", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Conversaciones del Clan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Text(
                text = "Ver todas >",
                color = UltravioletPurpleLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.bounceClick { }
            )
        }

        val currentUser = uiState.currentUser
        val filtered = uiState.activeChats.filter { user ->
            val matchesSearch = searchQuery.isBlank() || user.username.contains(searchQuery, ignoreCase = true) || user.email.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filter) {
                "Amigos" -> currentUser?.friends?.contains(user.id) == true
                "Clan" -> user.username.contains("Clan", ignoreCase = true)
                "Directos" -> !user.username.contains("Clan", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }

        if (filtered.isEmpty()) {
            EmptyHubState()
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                filtered.forEach { user ->
                    HubChatRow(user = user, onClick = { onChatClick(user.id) })
                }
            }
        }
    }
}

/**
 * Tarjeta de Fila de Conversación con bounceClick.
 */
@Composable
private fun HubChatRow(user: User, onClick: () -> Unit) {
    val statusText = when (user.status) {
        "ONLINE" -> "🟢 En línea"
        "AWAY" -> "🌙 Ausente"
        else -> "🔴 Ocupado"
    }

    val sampleMsg = when (user.username.lowercase()) {
        "eli" -> "¡Vamos al próximo nivel en Culebra Glow! 🐍"
        "gise" -> "Te desafío en Duelo Neon ⚡ ¿Aceptas?"
        "fabio" -> "Mira mi récord en Neon Rush 😾"
        else -> "¡Hola! ¿Echamos una partida en la Arena?"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0x1A1E1735))
            .border(1.2.dp, Color(0xFF007FFF).copy(alpha = 0.4f), RoundedCornerShape(28.dp))
            .bounceClick { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .border(2.dp, Brush.sweepGradient(listOf(ElectricCyan, UltravioletPurple, ElectricCyan)), CircleShape)
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
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF070514))
                            .padding(3.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF39FF14)))
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        color = if (user.status == "ONLINE") Color(0xFF39FF14) else OnSurfaceMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = sampleMsg,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("2:15 p. m.", color = Color.White.copy(alpha = 0.45f), fontSize = 11.5.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(UltravioletPurple, Color(0xFF4A00E0)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${user.unreadCount.coerceAtLeast(1)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = Color(0xFF007FFF), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyHubState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📡", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No encontramos conversaciones", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text("Intenta con otro nombre o filtro", color = ElectricCyan.copy(alpha = 0.65f), fontSize = 13.sp)
    }
}

/**
 * Modal para ver la Historia de un Usuario con animación.
 */
@Composable
private fun StoryViewerDialog(user: User, onDismiss: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF130A28),
        shape = RoundedCornerShape(28.dp),
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("CERRAR", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        title = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).border(1.5.dp, NeonLime, CircleShape)) {
                        GlowAvatar(config = user.avatarConfig, profileImageUrl = user.profileImageUrl, animate = false, modifier = Modifier.fillMaxSize().padding(2.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(user.username, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("Hace 15 min", color = ElectricCyan, fontSize = 11.sp)
                    }
                }
            }
        },
        text = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF28133D), Color(0xFF0A071A)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎮", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("¡Nivel 7 superado en Culebra Glow!", color = NeonLime, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    )
}

/**
 * Modal de Opciones del Botón Flotante (+) con animación.
 */
@Composable
private fun FabActionsMenuDialog(
    onDismiss: () -> Unit,
    onNewChat: () -> Unit,
    onCreateClan: () -> Unit,
    onInviteGame: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1233),
        shape = RoundedCornerShape(28.dp),
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = OnSurfaceMuted, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut()
            ) {
                Text("Menú Rápido Glowink 💬", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        },
        text = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onNewChat,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33007FFF)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("👤  Nuevo Chat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Button(
                        onClick = onCreateClan,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x339D4EDD)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("🛡️  Crear Clan / Squad", color = UltravioletPurpleLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Button(
                        onClick = onInviteGame,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x3339FF14)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("🎮  Invitar a Jugar / Desafiar", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    )
}

/**
 * Modal de Creación de Clan o Escuadrón Neón.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateClanDialog(
    onDismiss: () -> Unit,
    onCreateClan: (name: String, slogan: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var slogan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1233),
        shape = RoundedCornerShape(28.dp),
        title = {
            Text("CREAR NUEVO CLAN 🛡️", color = ElectricCyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Conviértete en Líder y funda tu escuadrón neón:", color = Color.White, fontSize = 12.5.sp)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nombre del Clan", color = OnSurfaceMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonLime, unfocusedBorderColor = ElectricCyan, focusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = slogan,
                    onValueChange = { slogan = it },
                    placeholder = { Text("Lema o descripción", color = OnSurfaceMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonLime, unfocusedBorderColor = ElectricCyan, focusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreateClan(name.trim(), slogan.ifBlank { "Juntos somos más fuertes" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Fundar Clan 👑", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = OnSurfaceMuted)
            }
        }
    )
}

/**
 * Barra de navegación inferior estilizada con Glassmorphism.
 */
@Composable
fun GlowinkBottomNavigationBar(
    selectedTab: Int = 0,
    onTabClick: (Int) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .glassmorphic(
                shape = RoundedCornerShape(28.dp),
                borderWidth = 1.5.dp,
                borderColor = Color(0x6600F0FF)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            NavTabItem(icon = "💬", label = "Chat", isSelected = selectedTab == 0, onClick = { onTabClick(0) })
            NavTabItem(icon = "🎮", label = "Juegos", isSelected = selectedTab == 1, onClick = { onTabClick(1) })
            NavTabItem(icon = "👤", label = "Perfil", isSelected = selectedTab == 2, onClick = { onTabClick(2) })
            NavTabItem(icon = "🧩", label = "Descubrir", isSelected = selectedTab == 3, onClick = { onTabClick(3) })
        }
    }
}

@Composable
private fun NavTabItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .bounceClick { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = icon, fontSize = 22.sp)
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NeonLime else OnSurfaceMuted
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) NeonLime else Color.Transparent)
        )
    }
}
