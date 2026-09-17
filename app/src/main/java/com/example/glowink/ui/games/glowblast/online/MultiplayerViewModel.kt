package com.example.glowink.ui.games.glowblast.online

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowink.ui.games.glowblast.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel Multijugador para gestionar estados de salas de Cloud Firestore.
 */
class MultiplayerViewModel(
    private val repository: FirebaseRoomRepository = FirebaseRoomRepository()
) : ViewModel() {

    private val _roomState = MutableStateFlow<GlowBlastGameState?>(null)
    val roomState: StateFlow<GlowBlastGameState?> = _roomState.asStateFlow()

    private val _currentRoomId = MutableStateFlow<String?>(null)
    val currentRoomId: StateFlow<String?> = _currentRoomId.asStateFlow()

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun createRoom(mapTheme: MapTheme, gameMode: GameMode, maxPlayers: Int = 4) {
        _isLoading.value = true
        _errorMessage.value = null

        val currentUserId = repository.getCurrentUserId()
        val currentUserName = repository.getCurrentUserName()

        val hostPlayer = GlowPlayer(
            id = currentUserId,
            name = currentUserName,
            isHost = true,
            isReady = true
        )

        repository.createRoom(hostPlayer, mapTheme, gameMode, maxPlayers) { roomId, error ->
            _isLoading.value = false
            if (roomId != null) {
                _currentRoomId.value = roomId
                _isHost.value = true
                listenToRoom(roomId)
            } else {
                _errorMessage.value = error ?: "Error al crear la sala multijugador"
            }
        }
    }

    fun joinRoom(roomId: String) {
        if (roomId.isBlank()) {
            _errorMessage.value = "Ingresa un código de sala válido"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        val currentUserId = repository.getCurrentUserId()
        val currentUserName = repository.getCurrentUserName()

        val player = GlowPlayer(
            id = currentUserId,
            name = currentUserName,
            isHost = false,
            isReady = false
        )

        repository.joinRoom(roomId.trim(), player) { success, error ->
            _isLoading.value = false
            if (success) {
                _currentRoomId.value = roomId
                _isHost.value = false
                listenToRoom(roomId)
            } else {
                _errorMessage.value = error ?: "No se pudo ingresar a la sala"
            }
        }
    }

    fun toggleReady(isReady: Boolean) {
        val roomId = _currentRoomId.value ?: return
        val playerId = repository.getCurrentUserId()
        repository.setPlayerReady(roomId, playerId, isReady)
    }

    fun startMatch() {
        val roomId = _currentRoomId.value ?: return
        if (_isHost.value) {
            repository.startMatch(roomId)
        }
    }

    fun leaveRoom() {
        val roomId = _currentRoomId.value ?: return
        val playerId = repository.getCurrentUserId()
        repository.leaveRoom(roomId, playerId)
        _currentRoomId.value = null
        _roomState.value = null
        _isHost.value = false
    }

    private fun listenToRoom(roomId: String) {
        viewModelScope.launch {
            repository.listenToRoomState(roomId).collect { state ->
                _roomState.value = state
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
