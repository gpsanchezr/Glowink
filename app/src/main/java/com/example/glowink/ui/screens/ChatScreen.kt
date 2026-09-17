package com.example.glowink.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.glowink.data.GameState
import com.example.glowink.data.Message
import com.example.glowink.data.User
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatDetailUiState
import com.example.glowink.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(
    viewModel: ChatViewModel? = if (LocalInspectionMode.current) null else ChatViewModel(),
    onBackClick: () -> Unit = {},
    onFriendProfileClick: () -> Unit = {}
) {
    val uiState = if (viewModel != null) {
        val state by viewModel.chatDetailUiState.collectAsState()
        state
    } else {
        ChatDetailUiState(
            currentUser = User(id = "me", username = "GlowUser"),
            selectedFriend = User(id = "friend", username = "Gise", rachaVictorias = 5),
            messages = emptyList()
        )
    }

    ChatScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onFriendProfileClick = onFriendProfileClick,
        onSendMessage = { text -> viewModel?.sendTextMessage(text) },
        onSendImageMessage = { uri -> viewModel?.sendImageMessage(uri) },
        onSendAudioMessage = { uri -> viewModel?.sendAudioMessage(uri) },
        onSendGameInvitation = { viewModel?.sendGameInvitation() },
        onMakeMove = { index -> viewModel?.makeMove(index) },
        onStartNewGame = { viewModel?.startNewGame() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    uiState: ChatDetailUiState = ChatDetailUiState(),
    onBackClick: () -> Unit = {},
    onFriendProfileClick: () -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onSendImageMessage: (android.net.Uri) -> Unit = {},
    onSendAudioMessage: (android.net.Uri) -> Unit = {},
    onSendGameInvitation: () -> Unit = {},
    onMakeMove: (Int) -> Unit = {},
    onStartNewGame: () -> Unit = {}
) {
    var isGamePanelOpen by remember { mutableStateOf(false) }
    var isAttachmentMenuOpen by remember { mutableStateOf(false) }
    var isStickerMenuOpen by remember { mutableStateOf(false) }
    var isGamesCatalogOpen by remember { mutableStateOf(false) }
    var isStickerCreatorOpen by remember { mutableStateOf(false) }
    var activeCallType by remember { mutableStateOf<String?>(null) } // null, "VOICE", "VIDEO"

    var selectedStickerUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var userStickers by remember { mutableStateOf(listOf<String>()) }
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val currentUserId = uiState.currentUser?.id.orEmpty()
    val friend = uiState.selectedFriend
    val messages = uiState.messages
    val gameState = uiState.gameState

    val stickerPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { selectedStickerUri = uri; isStickerCreatorOpen = true }
    }

    val attachmentPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            onSendImageMessage(uri)
            Toast.makeText(context, "Enviando imagen...", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            onSendMessage("📸 Foto capturada")
            Toast.makeText(context, "Foto capturada enviada", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            com.example.glowink.ui.theme.GlowinkMainBackground()

            Column(modifier = Modifier.fillMaxSize()) {
                ChatTopBar(
                    friend = friend,
                    currentUser = uiState.currentUser,
                    gameState = gameState,
                    onBackClick = onBackClick,
                    onFriendProfileClick = onFriendProfileClick,
                    onCallClick = {
                        activeCallType = "VOICE"
                        com.example.glowink.util.GlowSoundManager.playGameAction(context)
                    },
                    onVideoCallClick = {
                        activeCallType = "VIDEO"
                        com.example.glowink.util.GlowSoundManager.playGameAction(context)
                    }
                )

                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

                if (messages.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("¡Envía el primer saludo! 🚀", color = ElectricCyan, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(messages) { message ->
                            ChatMessageItem(message = message, currentUserId = currentUserId, onPlayInviteClick = { isGamePanelOpen = true })
                        }
                    }
                }

                ChatBottomInputBar(
                    inputText = inputText,
                    onInputTextChange = { inputText = it },
                    onSendMessage = {
                        if (inputText.isNotBlank()) {
                            com.example.glowink.util.GlowSoundManager.playMessageSent(context)
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    onToggleGamePanel = { isGamesCatalogOpen = true },
                    onToggleAttachmentMenu = { isAttachmentMenuOpen = true },
                    onToggleStickerMenu = { isStickerMenuOpen = true }
                )
            }

            // Overlay de Llamada / Videollamada Activa
            if (activeCallType != null) {
                GlowCallDialog(
                    friend = friend,
                    callType = activeCallType!!,
                    onEndCall = { activeCallType = null }
                )
            }

            if (isAttachmentMenuOpen) {
                ModalBottomSheet(onDismissRequest = { isAttachmentMenuOpen = false }, containerColor = Color(0xFF1B1233)) {
                    NeonAttachmentMenuPanel(
                        onOptionSelected = { opt ->
                            isAttachmentMenuOpen = false
                            if (opt == "Galería") {
                                attachmentPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else if (opt == "Cámara") {
                                cameraLauncher.launch(null)
                            } else if (opt == "Nota de Voz") {
                                onSendAudioMessage(android.net.Uri.EMPTY)
                                onSendMessage("🎙️ Nota de Voz (0:08)")
                                Toast.makeText(context, "Nota de voz enviada", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClose = { isAttachmentMenuOpen = false }
                    )
                }
            }

            if (isStickerMenuOpen) {
                ModalBottomSheet(onDismissRequest = { isStickerMenuOpen = false }, containerColor = Color(0xFF1B1233)) {
                    NeonStickerMenuPanel(
                        userStickers = userStickers,
                        onStickerSelected = { s -> onSendMessage(s); isStickerMenuOpen = false },
                        onCreateStickerClick = { stickerPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onClose = { isStickerMenuOpen = false }
                    )
                }
            }

            if (isGamesCatalogOpen) {
                ModalBottomSheet(onDismissRequest = { isGamesCatalogOpen = false }, containerColor = Color(0xFF1B1233)) {
                    GamesCatalogPanel(onGameSelected = { g -> isGamesCatalogOpen = false; if (g == "Tic Tac Toe Neón") { onSendGameInvitation(); isGamePanelOpen = true } }, onClose = { isGamesCatalogOpen = false })
                }
            }

            if (isStickerCreatorOpen) {
                StickerCreatorDialog(selectedUri = selectedStickerUri, onDismiss = { isStickerCreatorOpen = false }, onUploadSticker = { s -> userStickers = userStickers + s; isStickerCreatorOpen = false })
            }

            AnimatedVisibility(
                visible = isGamePanelOpen || (gameState != null && !gameState.isFinished),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                NeonTicTacToeGamePanel(gameState = gameState, onCellClick = { onMakeMove(it) }, onClosePanel = { isGamePanelOpen = false }, onRestartGame = { onStartNewGame() })
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    friend: User?,
    currentUser: User?,
    gameState: GameState?,
    onBackClick: () -> Unit,
    onFriendProfileClick: () -> Unit,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    val streakValue = friend?.rachaVictorias ?: 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(14.dp).glassmorphic(shape = RoundedCornerShape(20.dp), borderWidth = 1.dp, borderColor = Color(0x6600F0FF)).padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text("◀", color = ElectricCyan, modifier = Modifier.clickable { onBackClick() })
            Spacer(modifier = Modifier.width(8.dp))
            Row(modifier = Modifier.clickable { onFriendProfileClick() }, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, NeonLime, CircleShape).background(Color(0xFF221A3B))) {
                    com.example.glowink.ui.avatar.GlowAvatar(config = friend?.avatarConfig ?: com.example.glowink.data.Avatar3DConfig(), profileImageUrl = friend?.profileImageUrl?.ifBlank { friend.avatarUrl }, animate = false)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(friend?.username ?: "Jugador", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(if (gameState != null && !gameState.isFinished) "Jugando 🎮" else "Online 🎮", color = NeonLime, fontSize = 10.sp)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCallClick) { Icon(Icons.Default.Call, null, tint = ElectricCyan, modifier = Modifier.size(20.dp)) }
            IconButton(onClick = onVideoCallClick) { NeonVideoCallIcon(color = ElectricCyan) }
            Text("⚡ $streakValue", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ChatMessageItem(message: Message, currentUserId: String, onPlayInviteClick: () -> Unit) {
    val isSelf = message.senderId == currentUserId
    val isSticker = message.text.startsWith("STICKER:") || message.text in listOf("🐱", "🤖", "💀", "🎮", "💖", "🚀", "👻", "✨")
    val hasImage = message.isImage || !message.imageUrl.isNullOrBlank()
    val hasAudio = message.isAudio || !message.audioUrl.isNullOrBlank()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isSelf) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        when {
            isSticker -> {
                val stickerSymbol = message.text.removePrefix("STICKER:")
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (isSelf) ElectricCyan.copy(alpha = 0.35f) else Color(0xFFFF007F).copy(alpha = 0.35f),
                                    Color(0xFF1B1233)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            if (isSelf) ElectricCyan else Color(0xFFFF007F),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stickerSymbol, fontSize = 50.sp)
                }
            }
            hasImage -> {
                val imgUrl = message.imageUrl ?: message.text.removePrefix("IMAGE:")
                Column(
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .then(if (isSelf) Modifier.glassmorphicChatBubbleSelf() else Modifier.glassmorphicChatBubbleOther())
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF140D2A)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!imgUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Foto compartida",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("🖼️ [Foto]", color = ElectricCyan, fontSize = 24.sp)
                        }
                    }
                    if (message.text.isNotBlank() && !message.text.startsWith("IMAGE:")) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(message.text, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }
            }
            hasAudio -> {
                Row(
                    modifier = Modifier
                        .widthIn(max = 220.dp)
                        .then(if (isSelf) Modifier.glassmorphicChatBubbleSelf() else Modifier.glassmorphicChatBubbleOther())
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelf) NeonLime else ElectricCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(message.text.ifBlank { "🎙️ Nota de Voz" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("0:05 • Audio Neón", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .then(if (isSelf) Modifier.glassmorphicChatBubbleSelf() else Modifier.glassmorphicChatBubbleOther())
                        .padding(12.dp)
                ) {
                    Text(message.text, color = if (isSelf) OnBackgroundText else Color.White, fontSize = 14.sp)
                    if (message.isGameInvite) {
                        Button(
                            onClick = onPlayInviteClick,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UltravioletPurple)
                        ) {
                            Text("🎮 Jugar Ahora", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBottomInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onToggleGamePanel: () -> Unit,
    onToggleAttachmentMenu: () -> Unit,
    onToggleStickerMenu: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        IconButton(onClick = onToggleAttachmentMenu) { NeonClipIcon(color = ElectricCyan) }
        IconButton(onClick = onToggleStickerMenu) { Text("✨", fontSize = 18.sp) }
        IconButton(onClick = onToggleGamePanel) { Text("🎮", fontSize = 18.sp) }
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputTextChange,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = Color(0x4400F0FF),
                focusedTextColor = Color.White
            ),
            placeholder = { Text("Mensaje...", color = OnSurfaceMuted) }
        )
        IconButton(onClick = onSendMessage) { Text("🚀", fontSize = 20.sp) }
    }
}

@Composable
private fun GlowCallDialog(
    friend: User?,
    callType: String, // "VOICE" or "VIDEO"
    onEndCall: () -> Unit
) {
    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    val minutes = callSeconds / 60
    val seconds = callSeconds % 60
    val timeFormatted = "%02d:%02d".format(minutes, seconds)

    Dialog(onDismissRequest = onEndCall) {
        GlassContainer(
            shape = RoundedCornerShape(28.dp),
            borderColor = if (callType == "VIDEO") ElectricCyan else NeonLime,
            borderWidth = 2.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (callType == "VIDEO") "📹 VIDEOLLAMADA NEÓN" else "📞 LLAMADA DE VOZ NEÓN",
                    color = if (callType == "VIDEO") ElectricCyan else NeonLime,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    (if (callType == "VIDEO") ElectricCyan else NeonLime).copy(alpha = 0.4f),
                                    Color(0xFF140D2A)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.glowink.ui.avatar.GlowAvatarFrame(
                        config = friend?.avatarConfig ?: com.example.glowink.data.Avatar3DConfig(),
                        size = 86.dp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = friend?.username ?: "Jugador",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Conectado • $timeFormatted",
                    color = NeonLime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    text = "Cifrado P2P End-to-End",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color.Red.copy(alpha = 0.3f) else Color(0x331E1735))
                            .border(1.5.dp, if (isMuted) Color.Red else ElectricCyan, CircleShape)
                            .clickable {
                                isMuted = !isMuted
                                Toast.makeText(context, if (isMuted) "Micrófono silenciado" else "Micrófono activo", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isMuted) "🎙️❌" else "🎙️", fontSize = 20.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2222))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                com.example.glowink.util.GlowSoundManager.playGameAction(context)
                                onEndCall()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☎️", fontSize = 28.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (!isSpeakerOn) Color.Red.copy(alpha = 0.3f) else Color(0x331E1735))
                            .border(1.5.dp, ElectricCyan, CircleShape)
                            .clickable {
                                isSpeakerOn = !isSpeakerOn
                                Toast.makeText(context, if (callType == "VIDEO") "Cámara alternada" else "Altavoz activado", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (callType == "VIDEO") "📹" else "🔊", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NeonStickerMenuPanel(userStickers: List<String>, onStickerSelected: (String) -> Unit, onCreateStickerClick: () -> Unit, onClose: () -> Unit) {
    val themeStickers = listOf(
        Triple("🐱", "Gato Neón", ElectricCyan),
        Triple("🤖", "Cyber Bot", ElectricCyan),
        Triple("💀", "Rey Neón", Color(0xFFFF007F)),
        Triple("🎮", "Glow Pad", NeonLime),
        Triple("💖", "Corazón Glow", Color(0xFFFF4FD8)),
        Triple("🚀", "Cohete Cyber", Color(0xFF007FFF))
    )

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("STICKERS NEÓN ✨", color = ElectricCyan, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Button(
                onClick = onCreateStickerClick,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("＋ Crear", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(themeStickers) { (emoji, label, accentColor) ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0x221E1735))
                        .border(1.2.dp, accentColor, RoundedCornerShape(18.dp))
                        .clickable { onStickerSelected("STICKER:$emoji") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 38.sp)
                        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NeonAttachmentMenuPanel(onOptionSelected: (String) -> Unit, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text("ADJUNTOS NEÓN 📎", color = ElectricCyan, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        val opts = listOf("Galería" to "🖼️", "Cámara" to "📸", "Nota de Voz" to "🎙️", "Ubicación" to "📍")
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            opts.forEach { (title, icon) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onOptionSelected(title) }) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(ElectricCyan.copy(0.1f)).border(1.dp, ElectricCyan, CircleShape), contentAlignment = Alignment.Center) { Text(icon, fontSize = 24.sp) }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(title, color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun GamesCatalogPanel(onGameSelected: (String) -> Unit, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text("MINIJUEGOS 🎮", color = ElectricCyan, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        val games = listOf("Tic Tac Toe Neón" to "❌⭕", "Ping Pong Cyber" to "🏓")
        games.forEach { (title, icon) ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onGameSelected(title) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StickerCreatorDialog(selectedUri: android.net.Uri?, onDismiss: () -> Unit, onUploadSticker: (String) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF1B1233), title = { Text("CREAR STICKER", color = ElectricCyan) }, text = { Box(modifier = Modifier.size(100.dp).background(Color.Gray)) { if (selectedUri != null) AsyncImage(model = selectedUri, contentDescription = null) } }, confirmButton = { Button(onClick = { onUploadSticker("🎨") }) { Text("Subir") } })
}

@Composable
private fun NeonTicTacToeGamePanel(gameState: GameState?, onCellClick: (Int) -> Unit, onClosePanel: () -> Unit, onRestartGame: () -> Unit) {
    val board = gameState?.board ?: List(9) { "" }
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1B1233)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("3 EN RAYA NEÓN", color = ElectricCyan, fontWeight = FontWeight.Black)
            IconButton(onClick = onClosePanel) { Text("✕", color = Color.White) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.size(250.dp).border(2.dp, ElectricCyan)) {
            Column {
                for (r in 0..2) {
                    Row(modifier = Modifier.weight(1f)) {
                        for (c in 0..2) {
                            val i = r * 3 + c
                            Box(modifier = Modifier.weight(1f).fillMaxSize().border(1.dp, ElectricCyan.copy(0.3f)).clickable { onCellClick(i) }, contentAlignment = Alignment.Center) {
                                Text(board[i], color = if (board[i] == "X") NeonLime else Color.Magenta, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        if (gameState?.isFinished == true) {
            Button(onClick = onRestartGame, modifier = Modifier.padding(top = 16.dp)) { Text("REVANCHA") }
        }
    }
}

@Composable
private fun NeonVideoCallIcon(color: Color = ElectricCyan) {
    Canvas(modifier = Modifier.size(20.dp)) {
        drawRoundRect(color = color, size = Size(size.width * 0.6f, size.height * 0.5f), topLeft = Offset(0f, size.height * 0.25f), style = Stroke(2f))
        val path = Path().apply { moveTo(size.width * 0.6f, size.height * 0.4f); lineTo(size.width, size.height * 0.2f); lineTo(size.width, size.height * 0.8f); lineTo(size.width * 0.6f, size.height * 0.6f); close() }
        drawPath(path, color)
    }
}

@Composable
private fun NeonClipIcon(color: Color = ElectricCyan) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val p = Path().apply { moveTo(size.width * 0.3f, size.height * 0.8f); lineTo(size.width * 0.3f, size.height * 0.2f); quadraticTo(size.width * 0.3f, 0f, size.width * 0.7f, 0f); lineTo(size.width * 0.7f, size.height * 0.6f) }
        drawPath(p, color, style = Stroke(2f))
    }
}
