package com.example.glowink.ui.games.glowblast.online

import com.example.glowink.ui.games.glowblast.model.GlowBlastGameState
import com.example.glowink.ui.games.glowblast.model.GlowPlayer

/**
 * Repositorio de salas y sincronización multijugador para Glow Blast.
 * Arquitectura modular preparada para conectar con Cloud Firestore en fases posteriores.
 */
class RoomRepository {

    fun createRoom(hostPlayer: GlowPlayer, onRoomCreated: (roomId: String) -> Unit) {
        val mockRoomId = "room_${System.currentTimeMillis()}"
        onRoomCreated(mockRoomId)
    }

    fun joinRoom(roomId: String, player: GlowPlayer, onJoined: (Boolean) -> Unit) {
        onJoined(true)
    }

    fun listenToRoom(roomId: String, onStateUpdate: (GlowBlastGameState) -> Unit) {
        // Reservado para listener de Firestore
    }

    fun setPlayerReady(roomId: String, playerId: String, isReady: Boolean) {
        // Reservado para actualización de estado en Firestore
    }
}
