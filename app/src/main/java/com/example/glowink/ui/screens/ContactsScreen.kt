package com.example.glowink.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.data.RealUsersState
import com.example.glowink.data.User
import com.example.glowink.ui.avatar.GlowAvatar
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla de Contactos y Sistema de Amigos Mutuo en Tiempo Real para Glowink.
 * Sin datos hardcodeados: 100% reactiva mediante StateFlow y Firestore (colección 'friend_requests').
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit = {},
    onUserSelected: (userId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.chatsListUiState.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val currentUser = uiState.currentUser ?: User(id = currentUid, username = "Usuario")

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mis Amigos 👥", "Solicitudes 📩", "Buscar Usuarios 🔍")

    var searchQuery by remember { mutableStateOf("") }

    val realUsers = (uiState.realUsersState as? RealUsersState.Success)?.users ?: emptyList()

    // 1. Amigos confirmados (status = ACCEPTED / en currentUser.friends)
    val myFriends = remember(realUsers, currentUser.friends) {
        realUsers.filter { it.id in currentUser.friends && it.id != currentUid }
    }

    // 2. Solicitudes recibidas pendientes
    val pendingRequests = remember(realUsers, currentUser.receivedRequests) {
        realUsers.filter { it.id in currentUser.receivedRequests && it.id != currentUid }
    }

    // 3. Búsqueda de usuarios globales registrados
    val searchedUsers = remember(realUsers, searchQuery, currentUid) {
        realUsers.filter { user ->
            user.id != currentUid &&
                    (searchQuery.isBlank() ||
                            user.username.contains(searchQuery, ignoreCase = true) ||
                            user.email.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        containerColor = Color(0xFF151026),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MUNDO GLOW 👥", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("${myFriends.size} Amigos Confirmados", color = ElectricCyan, fontSize = 11.5.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color(0xFFFF4FD8))
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF151026), Color(0xFF1B1233), Color(0xFF151026))
                    )
                )
                .padding(horizontal = 16.dp)
        ) {
            // Pestañas de Navegación (Mis Amigos | Solicitudes | Buscar)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = ElectricCyan,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = if (index == 1 && pendingRequests.isNotEmpty()) "$title (${pendingRequests.size})" else title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Black else FontWeight.Bold,
                                color = if (selectedTabIndex == index) ElectricCyan else OnSurfaceMuted
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTabIndex) {
                // TAB 0: MIS AMIGOS CONFIRMADOS
                0 -> {
                    if (myFriends.isEmpty()) {
                        EmptyFriendsState(onNavigateToSearch = { selectedTabIndex = 2 })
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(myFriends, key = { it.id }) { friend ->
                                FriendRowCard(
                                    user = friend,
                                    onChatClick = {
                                        viewModel.selectChat(friend.id)
                                        onUserSelected(friend.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // TAB 1: SOLICITUDES DE AMISTAD RECIBIDAS
                1 -> {
                    if (pendingRequests.isEmpty()) {
                        EmptyRequestsState()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(pendingRequests, key = { it.id }) { requester ->
                                PendingRequestCard(
                                    user = requester,
                                    onAccept = {
                                        viewModel.acceptFriendInvitation(requester.id)
                                        Toast.makeText(context, "¡Ahora son amigos! 🤝", Toast.LENGTH_SHORT).show()
                                    },
                                    onReject = {
                                        viewModel.rejectFriendInvitation(requester.id)
                                        Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

                // TAB 2: BUSCAR USUARIOS Y ENVIAR SOLICITUD
                2 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por nombre o correo...", color = OnSurfaceMuted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0x221E1735),
                                unfocusedContainerColor = Color(0x221E1735),
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = ElectricCyan.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (searchedUsers.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No encontramos jugadores con ese nombre", color = OnSurfaceMuted, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(searchedUsers, key = { it.id }) { user ->
                                    val isFriend = currentUser.friends.contains(user.id)
                                    val isPending = currentUser.sentRequests.contains(user.id)

                                    SearchUserCard(
                                        user = user,
                                        isFriend = isFriend,
                                        isPending = isPending,
                                        onSendRequest = {
                                            viewModel.sendInvitationByEmail(
                                                email = user.email,
                                                onSuccess = {
                                                    Toast.makeText(context, "Solicitud enviada a ${user.username} 🚀", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de Fila de Amigo Confirmado (Con Busto Avatar Headshot y Botón Chatear).
 */
@Composable
private fun FriendRowCard(user: User, onChatClick: () -> Unit) {
    val statusText = when (user.status) {
        "ONLINE" -> "🟢 En línea"
        "AWAY" -> "🌙 Ausente"
        else -> "👤 Desconectado"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x221E1735))
            .border(1.2.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // GlowAvatar Busto Headshot
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(2.dp, Brush.sweepGradient(listOf(ElectricCyan, NeonLime, ElectricCyan)), CircleShape)
                ) {
                    GlowAvatar(
                        config = user.avatarConfig,
                        size = 54.dp,
                        isHeadshot = true,
                        animate = false,
                        modifier = Modifier.fillMaxSize().padding(2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = user.username,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = statusText,
                        color = if (user.status == "ONLINE") NeonLime else OnSurfaceMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = onChatClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Chatear 💬", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Tarjeta para Aceptar o Rechazar Solicitudes de Amistad Recibidas.
 */
@Composable
private fun PendingRequestCard(user: User, onAccept: () -> Unit, onReject: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x221E1735))
            .border(1.2.dp, NeonLime.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(2.dp, NeonLime, CircleShape)
                ) {
                    GlowAvatar(
                        config = user.avatarConfig,
                        size = 52.dp,
                        isHeadshot = true,
                        animate = false,
                        modifier = Modifier.fillMaxSize().padding(2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(user.username, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Quiere ser tu amigo", color = ElectricCyan, fontSize = 11.5.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("ACEPTAR", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF4444)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF4444)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("RECHAZAR", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Tarjeta de Resultado de Búsqueda para enviar solicitud de amistad.
 */
@Composable
private fun SearchUserCard(
    user: User,
    isFriend: Boolean,
    isPending: Boolean,
    onSendRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x1F1E1735))
            .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, ElectricCyan, CircleShape)
                ) {
                    GlowAvatar(
                        config = user.avatarConfig,
                        size = 50.dp,
                        isHeadshot = true,
                        animate = false,
                        modifier = Modifier.fillMaxSize().padding(2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(user.username, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(user.email, color = OnSurfaceMuted, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            when {
                isFriend -> {
                    Text("AMIGOS ✅", color = NeonLime, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                }
                isPending -> {
                    Text("PENDIENTE ⏳", color = Color(0xFFFFD24C), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                }
                else -> {
                    Button(
                        onClick = onSendRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("＋ AGREGAR", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFriendsState(onNavigateToSearch: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👥", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aún no tienes amigos confirmados", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Busca jugadores por nombre o correo para agregarlos a tu radar Glowink.", color = OnSurfaceMuted, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onNavigateToSearch,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Buscar Jugadores 🔍", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun EmptyRequestsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📩", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No tienes solicitudes pendientes", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Las invitaciones de otros jugadores aparecerán aquí.", color = OnSurfaceMuted, fontSize = 13.sp)
    }
}
