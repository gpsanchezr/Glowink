package com.example.glowink.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface RealUsersState {
    object Loading : RealUsersState
    data class Success(val users: List<User>) : RealUsersState
    data class Error(val message: String) : RealUsersState
}

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var userListener: ListenerRegistration? = null
    private var chatsListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    private var gameListener: ListenerRegistration? = null
    private var storiesListener: ListenerRegistration? = null
    private var communitiesListener: ListenerRegistration? = null

    private val _currentUser = MutableStateFlow(User(id = auth.currentUser?.uid ?: ""))
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _realUsersState = MutableStateFlow<RealUsersState>(RealUsersState.Loading)
    val realUsersState: StateFlow<RealUsersState> = _realUsersState.asStateFlow()

    private val _activeChats = MutableStateFlow<List<User>>(emptyList())
    val activeChats: StateFlow<List<User>> = _activeChats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _stories = MutableStateFlow<List<GlowStory>>(emptyList())
    val stories: StateFlow<List<GlowStory>> = _stories.asStateFlow()

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    init {
        val currentUid = auth.currentUser?.uid
        if (!currentUid.isNullOrBlank()) {
            listenToUserRealtime(currentUid)
            listenToCommunitiesRealtime()
        }
        listenToStoriesRealtime()
        if (currentUid != null) {
            listenToUsersRealtime()
        }
    }

    fun listenToUserRealtime(uid: String) {
        if (uid.isBlank()) return
        userListener?.remove()
        userListener = firestore.collection("users_glowink").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                if (user != null) _currentUser.value = user
            }
    }

    private fun listenToCommunitiesRealtime() {
        communitiesListener?.remove()
        communitiesListener = firestore.collection("communities_glowink")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(Community::class.java)?.copy(id = it.id) } ?: emptyList()
                _communities.value = list
            }
    }

    fun createCommunity(name: String, description: String, style: String) {
        val uid = auth.currentUser?.uid ?: return
        val id = "comm_${System.currentTimeMillis()}"
        val newComm = Community(id = id, name = name, description = description, ownerId = uid, members = listOf(uid), style = style)
        firestore.collection("communities_glowink").document(id).set(newComm)
    }

    fun joinCommunity(communityId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("communities_glowink").document(communityId).get().addOnSuccessListener { doc ->
            val comm = doc.toObject(Community::class.java) ?: return@addOnSuccessListener
            if (!comm.members.contains(uid)) {
                val updatedMembers = comm.members + uid
                firestore.collection("communities_glowink").document(communityId).update("members", updatedMembers)
            }
        }
    }

    fun listenToUsersRealtime() {
        _realUsersState.value = RealUsersState.Loading
        chatsListener?.remove()
        chatsListener = firestore.collection("users_glowink")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _realUsersState.value = RealUsersState.Error(error.localizedMessage ?: "")
                    return@addSnapshotListener
                }
                val allUsers = snapshot?.documents?.mapNotNull { it.toObject(User::class.java)?.copy(id = it.id) } ?: emptyList()
                val friends = allUsers.filter { it.id in _currentUser.value.friends }
                _activeChats.value = friends
                _realUsersState.value = RealUsersState.Success(allUsers)
            }
    }

    fun fetchRealUsers() = listenToUsersRealtime()

    fun listenToMessagesRealtime(chatId: String) {
        if (chatId.isBlank()) return
        messagesListener?.remove()
        messagesListener = firestore.collection("chats_glowink").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val msgs = snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) } ?: emptyList()
                _messages.value = msgs
            }
    }

    private fun listenToStoriesRealtime() {
        storiesListener?.remove()
        storiesListener = firestore.collection("stories_glowink").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val all = snapshot?.documents?.mapNotNull { it.toObject(GlowStory::class.java) } ?: emptyList()
                val friends = _currentUser.value.friends
                val myUid = _currentUser.value.id
                _stories.value = all.filter { it.userId == myUid || it.userId in friends }
            }
    }

    fun sendMessage(
        chatId: String,
        text: String,
        gameId: String? = null,
        auraReaction: String? = null,
        imageUrl: String? = null,
        audioUrl: String? = null,
        messageType: String = "TEXT"
    ) {
        val uid = auth.currentUser?.uid ?: return
        val msgId = "msg_${System.currentTimeMillis()}"
        val msg = Message(
            id = msgId,
            senderId = uid,
            text = text,
            timestamp = System.currentTimeMillis(),
            gameId = gameId,
            auraReactionType = auraReaction,
            imageUrl = imageUrl,
            audioUrl = audioUrl,
            messageType = messageType
        )
        firestore.collection("chats_glowink").document(chatId).collection("messages").document(msgId).set(msg)
    }

    fun startNewGame(chatId: String, player1Id: String, player2Id: String) {
        val newGame = GameState(gameId = "game_${System.currentTimeMillis()}", player1Id = player1Id, player2Id = player2Id, board = List(9) { "" }, turnPlayerId = player1Id, status = GameState.STATUS_ACTIVE)
        _gameState.value = newGame
        firestore.collection("chats_glowink").document(chatId).collection("games").document("active_game").set(newGame)
    }

    fun playTurn(chatId: String, position: Int) {
        val current = _gameState.value ?: return
        val uid = auth.currentUser?.uid ?: return
        if (current.turnPlayerId != uid || position !in 0..8 || current.board[position].isNotEmpty() || current.status != GameState.STATUS_ACTIVE) return
        val symbol = if (uid == current.player1Id) "X" else "O"
        val newBoard = current.board.toMutableList().apply { set(position, symbol) }
        val updatedGame = current.copy(board = newBoard, turnPlayerId = if (uid == current.player1Id) current.player2Id else current.player1Id)
        firestore.collection("chats_glowink").document(chatId).collection("games").document("active_game").set(updatedGame)
    }

    fun updateAvatarConfig(config: Avatar3DConfig) = updateUser(_currentUser.value.copy(avatarConfig = config))
    fun updateUser(user: User) { _currentUser.value = user; firestore.collection("users_glowink").document(user.id).set(user) }
    fun grantGameReward(coins: Int, statsTransform: (GameStats) -> GameStats) = updateUser(_currentUser.value.copy(glowCoins = _currentUser.value.glowCoins + coins, gameStats = statsTransform(_currentUser.value.gameStats)))

    fun publishStory(mediaUrl: String, filter: String) {
        val uid = auth.currentUser?.uid ?: return
        val id = "story_${System.currentTimeMillis()}"
        firestore.collection("stories_glowink").document(id).set(GlowStory(id = id, userId = uid, mediaUrl = mediaUrl, tipoFiltro = filter))
    }

    private var friendRequestsListener: ListenerRegistration? = null
    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    fun listenToFriendRequestsRealtime() {
        val uid = auth.currentUser?.uid ?: return
        friendRequestsListener?.remove()
        friendRequestsListener = firestore.collection("friend_requests")
            .whereEqualTo("toUserId", uid)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FriendRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                _friendRequests.value = list
            }
    }

    fun sendFriendRequest(targetUserId: String) {
        val uid = auth.currentUser?.uid ?: return
        if (targetUserId.isBlank() || targetUserId == uid) return

        val reqId = "req_${uid}_${targetUserId}"
        val req = FriendRequest(
            id = reqId,
            fromUserId = uid,
            toUserId = targetUserId,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("friend_requests").document(reqId).set(req)
        firestore.collection("users_glowink").document(uid).update("sentRequests", com.google.firebase.firestore.FieldValue.arrayUnion(targetUserId))
        firestore.collection("users_glowink").document(targetUserId).update("receivedRequests", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
    }

    fun acceptFriendRequest(requestId: String, fromUserId: String) {
        val uid = auth.currentUser?.uid ?: return
        val docId = if (requestId.isNotBlank()) requestId else "req_${fromUserId}_${uid}"

        firestore.collection("friend_requests").document(docId).update("status", "ACCEPTED")

        firestore.collection("users_glowink").document(uid).update(
            "friends", com.google.firebase.firestore.FieldValue.arrayUnion(fromUserId),
            "receivedRequests", com.google.firebase.firestore.FieldValue.arrayRemove(fromUserId)
        )
        firestore.collection("users_glowink").document(fromUserId).update(
            "friends", com.google.firebase.firestore.FieldValue.arrayUnion(uid),
            "sentRequests", com.google.firebase.firestore.FieldValue.arrayRemove(uid)
        )

        updateUser(_currentUser.value.copy(
            friends = (_currentUser.value.friends + fromUserId).distinct(),
            receivedRequests = _currentUser.value.receivedRequests.filter { it != fromUserId }
        ))
    }

    fun rejectFriendRequest(requestId: String, fromUserId: String) {
        val uid = auth.currentUser?.uid ?: return
        val docId = if (requestId.isNotBlank()) requestId else "req_${fromUserId}_${uid}"

        firestore.collection("friend_requests").document(docId).update("status", "REJECTED")

        firestore.collection("users_glowink").document(uid).update("receivedRequests", com.google.firebase.firestore.FieldValue.arrayRemove(fromUserId))
        firestore.collection("users_glowink").document(fromUserId).update("sentRequests", com.google.firebase.firestore.FieldValue.arrayRemove(uid))

        updateUser(_currentUser.value.copy(
            receivedRequests = _currentUser.value.receivedRequests.filter { it != fromUserId }
        ))
    }

    fun resetGame(chatId: String) {
        _gameState.value = null
        if (chatId.isNotBlank()) firestore.collection("chats_glowink").document(chatId).collection("games").document("active_game").delete()
    }

    fun toggleFavoriteGame(gameId: String) {
        val user = _currentUser.value
        val currentFavs = user.favoriteGames
        val updatedFavs = if (currentFavs.contains(gameId)) currentFavs.filter { it != gameId } else currentFavs + gameId
        updateUser(user.copy(favoriteGames = updatedFavs))
    }

    fun cleanup() {
        userListener?.remove()
        chatsListener?.remove()
        messagesListener?.remove()
        gameListener?.remove()
        storiesListener?.remove()
        communitiesListener?.remove()
        _activeChats.value = emptyList()
        _messages.value = emptyList()
        _stories.value = emptyList()
        _communities.value = emptyList()
        _gameState.value = null
    }
}
