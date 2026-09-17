package com.example.glowink.data

/**
 * Modelos de datos principales para la aplicación de chat gamificada y social "Glowink".
 * Todos los campos incluyen valores por defecto para permitir deserialización con Firebase Firestore.
 */

/**
 * Configuración del Avatar personalizable del usuario.
 */
data class Avatar3DConfig(
    val siluetaBase: String = "FEMENINO",
    val colorPiel: String = "#F4C2C2",
    val estiloPelo: String = "PURPLE_BOB",
    val colorPeloPrimario: String = "#9D4EDD",
    val colorPeloSecundario: String = "#00F0FF",
    val estiloOjos: String = "REDONDOS",
    val colorOjos: String = "#39FF14",
    val estiloRopa: String = "HOODIE_NEON",
    val colorRopaPrimario: String = "#1E1735",
    val colorRopaSecundario: String = "#00F0FF",
    val accesorioId: String = "NINGUNO"
)

/**
 * Progreso y puntajes de cada mini-juego de la Glow Arena.
 */
data class GameStats(
    val highScoreCulebra: Int = 0,
    val victoriasDuelo: Int = 0,
    val partidasDuelo: Int = 0,
    val victoriasCarrera: Int = 0,
    val highScoreQuiz: Int = 0,
    val victoriasTresEnRaya: Int = 0
)

/**
 * Modelo de Ítems de la Tienda Glowink.
 */
enum class ShopItemType { SKIN, AURA, PET, EMOTE }
enum class Rarity { COMMON, RARE, EPIC, LEGENDARY }

data class ShopItem(
    val id: String = "",
    val name: String = "",
    val type: ShopItemType = ShopItemType.SKIN,
    val priceCoins: Int = 0,
    val priceGems: Int = 0,
    val rarity: Rarity = Rarity.COMMON,
    val drawableRes: Int = 0,
    val owned: Boolean = false,
    val equipped: Boolean = false
)

data class UserShop(
    val ownedItems: List<String> = emptyList(),
    val equippedSkin: String? = null,
    val equippedAura: String? = "aura_cyan",
    val equippedPet: String? = "pet_dragon"
)

/**
 * Modelo de Usuario en Glowink.
 */
data class User(
    val id: String = "",
    val username: String = "GlowPro99",
    val email: String = "",
    val avatarUrl: String = "",
    val profileImageUrl: String = "",
    val glowCoins: Int = 500,
    val gems: Int = 10,
    val rachaVictorias: Int = 0,
    val esFavorito: Boolean = false,
    val customNickname: String = "",
    val unreadCount: Int = 0,
    val avatarConfig: Avatar3DConfig = Avatar3DConfig(),
    val gameStats: GameStats = GameStats(),
    val shop: UserShop = UserShop(),
    val fcmToken: String = "",
    val friends: List<String> = emptyList(),
    val sentRequests: List<String> = emptyList(),
    val receivedRequests: List<String> = emptyList(),
    val unlockedItems: List<String> = emptyList(),
    val unlockedGames: List<String> = listOf("culebra"),
    val favoriteGames: List<String> = emptyList(),
    val status: String = "ONLINE" // ONLINE, AWAY, BUSY
) {
    val nivel: Int get() = 1 + (rachaVictorias / 3)
}

/**
 * Modelo de Mensaje para el chat individual o grupal.
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val gameId: String? = null,
    val auraReactionType: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val messageType: String = "TEXT"
) {
    val isGameInvite: Boolean get() = !gameId.isNullOrBlank() || messageType == "GAME_INVITE"
    val isImage: Boolean get() = !imageUrl.isNullOrBlank() || messageType == "IMAGE" || text.startsWith("IMAGE:") || text.startsWith("🖼️") || text.startsWith("📸")
    val isAudio: Boolean get() = !audioUrl.isNullOrBlank() || messageType == "AUDIO" || text.startsWith("AUDIO:") || text.startsWith("🎙️")
}

/**
 * Modelo de Estado o Historia temporal compartida con el Clan ("GlowStory").
 */
data class GlowStory(
    val id: String = "",
    val userId: String = "",
    val mediaUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tipoFiltro: String = "AURA_NEON"
)

/**
 * Modelo de Estado para los mini-juegos.
 */
data class GameState(
    val gameId: String = "",
    val player1Id: String = "",
    val player2Id: String = "",
    val board: List<String> = List(9) { "" },
    val turnPlayerId: String = "",
    val status: String = STATUS_ACTIVE,
    val winnerId: String? = null
) {
    companion object {
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_FINISHED = "FINISHED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
    val isFinished: Boolean get() = status == STATUS_FINISHED
}

/**
 * Modelo para Torneos en la Glow Arena.
 */
data class Tournament(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val entryFeeCoins: Int = 0,
    val entryFeeGems: Int = 0,
    val prizePool: String = "",
    val startTime: Long = 0,
    val isPaid: Boolean = false,
    val participantsCount: Int = 0
)

/**
 * Modelo para Comunidades (Squads) reales en Glowink.
 */
data class Community(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val members: List<String> = emptyList(),
    val style: String = "SPRING", // FIRE, ICE, SUMMER, SPRING
    val timestamp: Long = System.currentTimeMillis()
)

object CommunityStyles {
    const val FIRE = "FIRE"
    const val ICE = "ICE"
    const val SUMMER = "SUMMER"
    const val SPRING = "SPRING"
}

/**
 * Modelo de Solicitud de Amistad en la colección Firestore 'friend_requests'.
 */
data class FriendRequest(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val timestamp: Long = System.currentTimeMillis()
)
