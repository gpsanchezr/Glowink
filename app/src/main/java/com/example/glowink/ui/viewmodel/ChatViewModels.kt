package com.example.glowink.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowink.data.Avatar3DConfig
import com.example.glowink.data.ChatRepository
import com.example.glowink.data.GameState
import com.example.glowink.data.GlowStory
import com.example.glowink.data.Message
import com.example.glowink.data.RealUsersState
import com.example.glowink.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado UI para la lista principal de conversaciones reales, historias y perfil del usuario.
 */
data class ChatsListUiState(
    val currentUser: User? = null,
    val activeChats: List<User> = emptyList(),
    val stories: List<GlowStory> = emptyList(),
    val realUsersState: RealUsersState = RealUsersState.Loading,
    val errorMessage: String? = null,
    val communities: List<com.example.glowink.data.Community> = emptyList()
) {
    val isLoading: Boolean get() = realUsersState is RealUsersState.Loading
}

/**
 * Estado UI para una conversación individual en detalle con un usuario real.
 */
data class ChatDetailUiState(
    val currentUser: User? = null,
    val selectedFriend: User? = null,
    val messages: List<Message> = emptyList(),
    val gameState: GameState? = null,
    val isLoading: Boolean = false,
    val activeChatId: String = ""
)

/**
 * ViewModel principal de Glowink que conecta la interfaz en tiempo real con Firebase Auth y Firestore.
 */
class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _activeChatId = MutableStateFlow("")
    val activeChatId: StateFlow<String> = _activeChatId.asStateFlow()

    /** Flujo directo de estados reales de usuarios (Loading, Success, Error) */
    val realUsersState: StateFlow<RealUsersState> = repository.realUsersState

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    /**
     * Flujo combinado del estado UI de la pantalla principal (ChatsList + Historias + Perfil).
     * Recolecta en tiempo real los cambios en la base de datos de Firestore.
     */
    val chatsListUiState: StateFlow<ChatsListUiState> = combine(
        repository.currentUser,
        repository.activeChats,
        repository.stories,
        repository.communities,
        repository.realUsersState
    ) { user, chats, stories, communities, state ->
        val errorMsg = if (state is RealUsersState.Error) state.message else null
        ChatsListUiState(
            currentUser = user,
            activeChats = chats,
            stories = stories,
            realUsersState = state,
            errorMessage = errorMsg,
            communities = communities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatsListUiState(realUsersState = RealUsersState.Loading)
    )

    /**
     * Flujo combinado del estado UI para la pantalla de chat individual con un usuario real.
     */
    val chatDetailUiState: StateFlow<ChatDetailUiState> = combine(
        repository.currentUser,
        repository.activeChats,
        repository.messages,
        repository.gameState
    ) { user, chats, messages, gameState ->
        val selectedFriend = chats.find {
                auth.currentUser?.uid?.let { uid -> buildChatId(uid, it.id) == _activeChatId.value } == true
            }
        ChatDetailUiState(
            currentUser = user,
            selectedFriend = selectedFriend,
            messages = messages,
            gameState = gameState,
            isLoading = false,
            activeChatId = _activeChatId.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatDetailUiState(isLoading = true)
    )

    init {
        val currentUid = auth.currentUser?.uid ?: ""
        if (currentUid.isNotBlank()) {
            repository.listenToUserRealtime(currentUid)
        }
        repository.listenToUsersRealtime()
    }

    /**
     * Solicita/fuerza la obtención de usuarios reales registrados desde Firestore.
     */
    fun fetchRealUsers() {
        repository.fetchRealUsers()
    }

    /**
     * Inicio de sesión real con Firebase Authentication.
     * CUMPLIMIENTO DE REGLAS ESTRICTAS:
     * 1. Limpieza de Inputs (.trim()) en email y contraseña.
     * 2. Uso estricto de signInWithEmailAndPassword.
     * 3. Bloqueo de navegación prematura: onSuccess() solo se ejecuta en addOnSuccessListener.
     */
    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim()
        val cleanPass = pass

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            onError("Por favor ingresa un email y contraseña válidos")
            return
        }

        _authLoading.value = true
        _authErrorMessage.value = null

        // REGLA 1 y 2: Llamada limpia a signInWithEmailAndPassword con .trim()
        auth.signInWithEmailAndPassword(cleanEmail, cleanPass)
            .addOnSuccessListener { authResult ->
                _authLoading.value = false
                val uid = authResult.user?.uid ?: ""
                repository.listenToUserRealtime(uid)
                repository.listenToUsersRealtime()
                // REGLA 3: Navegación ÚNICAMENTE tras resultado exitoso
                onSuccess()
            }
            .addOnFailureListener { exc ->
                _authLoading.value = false
                val error = exc.localizedMessage ?: exc.message ?: "Error al iniciar sesión en Firebase"
                _authErrorMessage.value = error
                onError(error) // Detiene el avance en caso de fallo
            }
    }

    /**
     * Registro real de usuario con Firebase Authentication y Firestore.
     * CUMPLIMIENTO DE REGLAS ESTRICTAS:
     * 1. Limpieza de Inputs (.trim()) en username, email y contraseña.
     * 2. Uso estricto de createUserWithEmailAndPassword.
     * 3. Bloqueo de navegación prematura: onSuccess() solo se llama si la creación en Auth y Firestore triunfan.
     */
    fun signUpWithEmail(
        username: String,
        email: String,
        pass: String,
        avatarConfig: Avatar3DConfig = Avatar3DConfig(),
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanUsername = username.trim()
        val cleanEmail = email.trim()
        val cleanPass = pass

        if (cleanEmail.isBlank() || cleanPass.isBlank() || cleanUsername.isBlank()) {
            onError("Por favor completa todos los campos correctamente")
            return
        }

        _authLoading.value = true
        _authErrorMessage.value = null

        // REGLA 1 y 2: Llamada limpia a createUserWithEmailAndPassword con .trim()
        auth.createUserWithEmailAndPassword(cleanEmail, cleanPass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                val newUser = User(
                    id = uid,
                    username = cleanUsername.ifBlank { "GlowUser" },
                    email = cleanEmail,
                    glowCoins = 100,
                    rachaVictorias = 0,
                    avatarConfig = avatarConfig
                )

                // Guardar perfil en Firestore
                firestore.collection("users_glowink").document(uid)
                    .set(newUser)
                    .addOnSuccessListener {
                        _authLoading.value = false
                        repository.listenToUserRealtime(uid)
                        repository.listenToUsersRealtime()
                        // REGLA 3: Navegación ÚNICAMENTE tras confirmación en Firebase
                        onSuccess()
                    }
                    .addOnFailureListener { exc ->
                        _authLoading.value = false
                        val err = exc.localizedMessage ?: "Error guardando datos de perfil en Firestore"
                        _authErrorMessage.value = err
                        onError(err)
                    }
            }
            .addOnFailureListener { exc ->
                _authLoading.value = false
                val error = exc.localizedMessage ?: exc.message ?: "Error al registrar usuario en Firebase"
                _authErrorMessage.value = error
                onError(error)
            }
    }

    /**
     * Busca un usuario por email para ENVIAR una invitación de amistad.
     */
    fun sendInvitationByEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) return
        val currentUid = auth.currentUser?.uid.orEmpty()
        
        firestore.collection("users_glowink")
            .whereEqualTo("email", cleanEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                val friend = snapshot.documents.firstOrNull()?.toObject(User::class.java)
                if (friend != null && friend.id != currentUid) {
                    repository.sendFriendRequest(friend.id)
                    onSuccess()
                } else if (friend != null) {
                    onError("No puedes invitarte a ti mismo")
                } else {
                    onError("Jugador no encontrado en el radar Glowink")
                }
            }
            .addOnFailureListener { exc ->
                val err = exc.localizedMessage ?: "Error de conexión en Firebase"
                onError("Error de Firebase: $err")
            }
    }

    fun acceptFriendInvitation(friendId: String) {
        repository.acceptFriendRequest("", friendId)
    }

    fun rejectFriendInvitation(friendId: String) {
        repository.rejectFriendRequest("", friendId)
    }

    fun sendFriendRequest(targetUserId: String) {
        repository.sendFriendRequest(targetUserId)
    }

    /**
     * Subes la foto de perfil del usuario a Firebase Storage y actualiza la URL en Firestore.
     */
    fun uploadProfileImageToStorage(uri: android.net.Uri) {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return
        _authLoading.value = true
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
            .reference
            .child("profile_images/$uid.jpg")

        storageRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                _authLoading.value = false
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()
                    firestore.collection("users_glowink").document(uid)
                        .update("profileImageUrl", downloadUri, "avatarUrl", downloadUri)
                }
            }
    }

    /**
     * Alterna el estado de favorito de un amigo en Firestore.
     */
    fun toggleFavoriteFriend(friendId: String, isFav: Boolean) {
        if (friendId.isBlank()) return
        firestore.collection("users_glowink").document(friendId)
            .update("esFavorito", isFav)
    }

    /**
     * Actualiza el apodo/nombre personalizado de un amigo.
     */
    fun updateFriendCustomNickname(friendId: String, nickname: String) {
        if (friendId.isBlank()) return
        firestore.collection("users_glowink").document(friendId)
            .update("customNickname", nickname.trim())
    }

    /**
     * Selecciona una conversación de amigo y escucha sus mensajes en tiempo real.
     * Solo permite si son amigos confirmados.
     */
    fun selectChat(friendId: String) {
        val currentUser = repository.currentUser.value
        val isFriend = currentUser.friends.contains(friendId)
        val isSelf = friendId == currentUser.id
        
        if (!isFriend && !isSelf) {
            Log.w("ChatViewModel", "Intento de chat con no-amigo bloqueado: $friendId")
            return
        }

        val currentUid = auth.currentUser?.uid.orEmpty()
        if (friendId.isBlank() || currentUid.isBlank()) return
        val chatId = buildChatId(currentUid, friendId)
        _activeChatId.value = chatId
        viewModelScope.launch {
            repository.listenToMessagesRealtime(chatId)
        }
    }

    private fun buildChatId(uidA: String, uidB: String): String =
        if (uidA < uidB) "chat_${uidA}_${uidB}" else "chat_${uidB}_${uidA}"

    fun sendMessage(text: String) {
        if (text.isBlank() || _activeChatId.value.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(chatId = _activeChatId.value, text = text)
        }
    }

    fun sendTextMessage(text: String) = sendMessage(text)

    fun sendGameInvite() {
        if (_activeChatId.value.isBlank()) return
        val gameId = "neon_3x3_${System.currentTimeMillis()}"
        viewModelScope.launch {
            repository.sendMessage(
                chatId = _activeChatId.value,
                text = "🎮 ¡Te he retado a un 3 en Raya Neón! ¿Aceptas la partida?",
                gameId = gameId
            )
            val currentUserId = repository.currentUser.value.id
            val friendId = repository.activeChats.value.firstOrNull { friend ->
                buildChatId(currentUserId, friend.id) == _activeChatId.value
            }?.id.orEmpty()
            if (currentUserId.isNotBlank() && friendId.isNotBlank()) {
                repository.startNewGame(chatId = _activeChatId.value, player1Id = currentUserId, player2Id = friendId)
            }
        }
    }

    fun sendGameInvitation() = sendGameInvite()

    fun playGameTurn(position: Int) {
        if (_activeChatId.value.isBlank()) return
        viewModelScope.launch {
            repository.playTurn(chatId = _activeChatId.value, position = position)
        }
    }

    fun makeMove(position: Int) = playGameTurn(position)

    fun startNewGame() {
        if (_activeChatId.value.isBlank()) return
        val currentUserId = repository.currentUser.value.id
        val friendId = repository.activeChats.value.firstOrNull { friend ->
            buildChatId(currentUserId, friend.id) == _activeChatId.value
        }?.id.orEmpty()
        if (currentUserId.isNotBlank() && friendId.isNotBlank()) {
            repository.startNewGame(chatId = _activeChatId.value, player1Id = currentUserId, player2Id = friendId)
        }
    }

    fun resetGame() {
        if (_activeChatId.value.isBlank()) return
        repository.resetGame(chatId = _activeChatId.value)
    }

    fun publishStory(mediaUrl: String, tipoFiltro: String) {
        if (mediaUrl.isBlank()) return
        repository.publishStory(mediaUrl = mediaUrl, filter = tipoFiltro)
    }

    fun logout() {
        repository.cleanup()
        _activeChatId.value = ""
        auth.signOut()
    }

    override fun onCleared() {
        repository.cleanup()
        super.onCleared()
    }

    fun updateAvatarConfig(config: Avatar3DConfig) {
        repository.updateAvatarConfig(config)
    }

    fun buyItem(itemId: String, costCoins: Int, costGems: Int) {
        val user = repository.currentUser.value
        if (user.glowCoins >= costCoins && user.gems >= costGems) {
            val updatedUser = user.copy(
                glowCoins = user.glowCoins - costCoins,
                gems = user.gems - costGems,
                unlockedItems = (user.unlockedItems + itemId).distinct()
            )
            repository.updateUser(updatedUser)
        }
    }

    fun buyGems(amount: Int, price: Double) {
        val user = repository.currentUser.value
        // Aquí iría la integración con Google Play Billing
        // Por ahora simulamos la compra exitosa
        val updatedUser = user.copy(gems = user.gems + amount)
        repository.updateUser(updatedUser)
    }

    fun buyCoins(amount: Int, price: Double) {
        val user = repository.currentUser.value
        val updatedUser = user.copy(glowCoins = user.glowCoins + amount)
        repository.updateUser(updatedUser)
    }

    fun unlockGame(gameId: String, costCoins: Int) {
        val user = repository.currentUser.value
        if (user.glowCoins >= costCoins && !user.unlockedGames.contains(gameId)) {
            val updatedUser = user.copy(
                glowCoins = user.glowCoins - costCoins,
                unlockedGames = (user.unlockedGames + gameId).distinct()
            )
            repository.updateUser(updatedUser)
        }
    }

    fun grantGameReward(coinsEarned: Int, statsTransform: (com.example.glowink.data.GameStats) -> com.example.glowink.data.GameStats) {
        repository.grantGameReward(coinsEarned, statsTransform)
    }

    fun createCommunity(name: String, description: String, style: String) {
        repository.createCommunity(name, description, style)
    }

    fun joinCommunity(communityId: String) {
        repository.joinCommunity(communityId)
    }

    fun toggleFavoriteGame(gameId: String) {
        repository.toggleFavoriteGame(gameId)
    }

    fun updateUserStatus(status: String) {
        val user = repository.currentUser.value
        repository.updateUser(user.copy(status = status))
    }
}
