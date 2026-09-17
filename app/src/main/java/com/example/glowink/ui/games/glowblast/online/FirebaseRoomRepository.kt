package com.example.glowink.ui.games.glowblast.online

import com.example.glowink.ui.games.glowblast.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repositorio multijugador en tiempo real con Cloud Firestore y Firebase Auth para Glow Blast.
 * Optimizado con eventos discretos para evitar saturación de red a 60 FPS.
 */
class FirebaseRoomRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val roomsCollection = firestore.collection("glowink_rooms")

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "user_local_${(System.currentTimeMillis() % 10000)}"
    }

    fun getCurrentUserName(): String {
        return auth.currentUser?.displayName ?: "Jugador Glow"
    }

    /**
     * Crea una sala pública/privada en Cloud Firestore.
     */
    fun createRoom(
        hostPlayer: GlowPlayer,
        mapTheme: MapTheme,
        gameMode: GameMode,
        maxPlayers: Int = 4,
        onResult: (roomId: String?, error: String?) -> Unit
    ) {
        val roomId = "glow_${(100000..999999).random()}"

        val initialRoomData = hashMapOf(
            "roomId" to roomId,
            "hostId" to hostPlayer.id,
            "gameMode" to gameMode.name,
            "mapTheme" to mapTheme.name,
            "maxPlayers" to maxPlayers,
            "isMatchStarted" to false,
            "isMatchFinished" to false,
            "createdAt" to System.currentTimeMillis(),
            "players" to listOf(
                hashMapOf(
                    "id" to hostPlayer.id,
                    "name" to hostPlayer.name,
                    "isHost" to true,
                    "isReady" to true,
                    "x" to 1.0f,
                    "y" to 1.0f,
                    "lives" to 3
                )
            )
        )

        roomsCollection.document(roomId)
            .set(initialRoomData)
            .addOnSuccessListener {
                onResult(roomId, null)
            }
            .addOnFailureListener { e ->
                onResult(null, e.localizedMessage ?: "Error al crear sala multijugador")
            }
    }

    /**
     * Se une a una sala existente en Firestore verificando capacidad máxima y estado.
     */
    @Suppress("UNCHECKED_CAST")
    fun joinRoom(
        roomId: String,
        player: GlowPlayer,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        val roomRef = roomsCollection.document(roomId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(roomRef)
            if (!snapshot.exists()) {
                throw IllegalStateException("La sala $roomId no existe.")
            }

            val isMatchStarted = snapshot.getBoolean("isMatchStarted") ?: false
            if (isMatchStarted) {
                throw IllegalStateException("La partida en esta sala ya ha comenzado.")
            }

            val maxPlayers = (snapshot.getLong("maxPlayers") ?: 4L).toInt()
            val rawPlayers = snapshot.get("players") as? List<Map<String, Any>> ?: emptyList()

            if (rawPlayers.size >= maxPlayers) {
                throw IllegalStateException("La sala $roomId está llena ($maxPlayers/$maxPlayers).")
            }

            val alreadyInRoom = rawPlayers.any { it["id"] == player.id }
            if (!alreadyInRoom) {
                val newPlayerMap = hashMapOf(
                    "id" to player.id,
                    "name" to player.name,
                    "isHost" to false,
                    "isReady" to player.isReady,
                    "x" to 13.0f,
                    "y" to 9.0f,
                    "lives" to 3
                )
                val updatedPlayers = rawPlayers + newPlayerMap
                transaction.update(roomRef, "players", updatedPlayers)
            }
        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { e ->
            onResult(false, e.localizedMessage ?: "No se pudo ingresar a la sala")
        }
    }

    /**
     * Cambia el estado 'ready' del jugador en la sala.
     */
    @Suppress("UNCHECKED_CAST")
    fun setPlayerReady(roomId: String, playerId: String, isReady: Boolean) {
        val roomRef = roomsCollection.document(roomId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(roomRef)
            if (snapshot.exists()) {
                val rawPlayers = snapshot.get("players") as? List<Map<String, Any>> ?: emptyList()
                val updatedPlayers = rawPlayers.map { p ->
                    if (p["id"] == playerId) {
                        p.toMutableMap().apply { put("isReady", isReady) }
                    } else p
                }
                transaction.update(roomRef, "players", updatedPlayers)
            }
        }
    }

    /**
     * Abandona la sala o elimina la sala si era el Host.
     */
    @Suppress("UNCHECKED_CAST")
    fun leaveRoom(roomId: String, playerId: String) {
        val roomRef = roomsCollection.document(roomId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(roomRef)
            if (snapshot.exists()) {
                val hostId = snapshot.getString("hostId")
                val rawPlayers = snapshot.get("players") as? List<Map<String, Any>> ?: emptyList()

                if (hostId == playerId) {
                    transaction.delete(roomRef)
                } else {
                    val updatedPlayers = rawPlayers.filter { it["id"] != playerId }
                    transaction.update(roomRef, "players", updatedPlayers)
                }
            }
        }
    }

    /**
     * Inicia la partida en Cloud Firestore (Invocado por el Host).
     */
    fun startMatch(roomId: String) {
        roomsCollection.document(roomId).update("isMatchStarted", true)
    }

    /**
     * Inicia un Listener en Tiempo Real mediante Coroutine callbackFlow.
     */
    @Suppress("UNCHECKED_CAST")
    fun listenToRoomState(roomId: String): Flow<GlowBlastGameState?> = callbackFlow {
        val subscription = roomsCollection.document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val modeStr = snapshot.getString("gameMode") ?: GameMode.CLASICO.name
                    val mapStr = snapshot.getString("mapTheme") ?: MapTheme.CIUDAD_NEON.name
                    val isMatchStarted = snapshot.getBoolean("isMatchStarted") ?: false
                    val isMatchFinished = snapshot.getBoolean("isMatchFinished") ?: false

                    val rawPlayers = snapshot.get("players") as? List<Map<String, Any>> ?: emptyList()
                    val players = rawPlayers.map { p ->
                        GlowPlayer(
                            id = p["id"] as? String ?: "",
                            name = p["name"] as? String ?: "Jugador",
                            isHost = p["isHost"] as? Boolean ?: false,
                            isReady = p["isReady"] as? Boolean ?: false,
                            lives = (p["lives"] as? Long ?: 3L).toInt()
                        )
                    }

                    val state = GlowBlastGameState(
                        gameId = roomId,
                        mode = try { GameMode.valueOf(modeStr) } catch (_: Exception) { GameMode.CLASICO },
                        mapTheme = try { MapTheme.valueOf(mapStr) } catch (_: Exception) { MapTheme.CIUDAD_NEON },
                        players = players,
                        isMatchStarted = isMatchStarted,
                        isMatchFinished = isMatchFinished
                    )
                    trySend(state)
                } else {
                    trySend(null)
                }
            }

        awaitClose { subscription.remove() }
    }
}
