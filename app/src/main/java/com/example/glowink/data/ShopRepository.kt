package com.example.glowink.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de la Tienda Glowink. Maneja transacciones atómicas en Firestore para compras y equipamiento.
 */
class ShopRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersRef = firestore.collection("users_glowink")

    fun getCatalog(): List<ShopItem> {
        return listOf(
            ShopItem("skin_1", "Armadura Diamante", ShopItemType.SKIN, priceCoins = 500, rarity = Rarity.RARE, drawableRes = 0),
            ShopItem("skin_2", "Traje Sombra", ShopItemType.SKIN, priceCoins = 800, rarity = Rarity.EPIC, drawableRes = 0),
            ShopItem("skin_3", "Armadura Rey Neón", ShopItemType.SKIN, priceGems = 5, rarity = Rarity.LEGENDARY, drawableRes = 0),
            ShopItem("aura_cyan", "Aura Cyan", ShopItemType.AURA, priceCoins = 200, rarity = Rarity.COMMON, drawableRes = 0),
            ShopItem("aura_magma", "Aura Magma", ShopItemType.AURA, priceCoins = 300, rarity = Rarity.RARE, drawableRes = 0),
            ShopItem("aura_cosmica", "Aura Cósmica", ShopItemType.AURA, priceGems = 4, rarity = Rarity.LEGENDARY, drawableRes = 0),
            ShopItem("pet_dragon", "Dragón Bebé", ShopItemType.PET, priceGems = 3, rarity = Rarity.EPIC, drawableRes = 0),
            ShopItem("pet_fenix", "Fénix Neón", ShopItemType.PET, priceGems = 6, rarity = Rarity.LEGENDARY, drawableRes = 0),
            ShopItem("pet_robo", "Robito Glow", ShopItemType.PET, priceCoins = 150, rarity = Rarity.COMMON, drawableRes = 0),
            ShopItem("emote_dance", "Baila Neón", ShopItemType.EMOTE, priceCoins = 100, rarity = Rarity.COMMON, drawableRes = 0),
            ShopItem("emote_jump", "Salto Cuántico", ShopItemType.EMOTE, priceCoins = 250, rarity = Rarity.RARE, drawableRes = 0)
        )
    }

    suspend fun purchaseItem(uid: String, item: ShopItem): Boolean {
        if (uid.isBlank()) return false
        val userDoc = usersRef.document(uid)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDoc)
                val currentCoins = snapshot.getLong("glowCoins")?.toInt() ?: 0
                val currentGems = snapshot.getLong("gems")?.toInt() ?: 0

                val canAffordCoins = item.priceCoins == 0 || currentCoins >= item.priceCoins
                val canAffordGems = item.priceGems == 0 || currentGems >= item.priceGems

                if (!canAffordCoins || !canAffordGems) {
                    throw IllegalStateException("Saldo insuficiente")
                }

                if (item.priceCoins > 0) {
                    transaction.update(userDoc, "glowCoins", FieldValue.increment(-item.priceCoins.toLong()))
                }
                if (item.priceGems > 0) {
                    transaction.update(userDoc, "gems", FieldValue.increment(-item.priceGems.toLong()))
                }
                transaction.update(userDoc, "shop.ownedItems", FieldValue.arrayUnion(item.id))
            }.await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun equipItem(uid: String, itemId: String, type: ShopItemType): Boolean {
        if (uid.isBlank()) return false
        val userDoc = usersRef.document(uid)
        val field = when (type) {
            ShopItemType.SKIN -> "shop.equippedSkin"
            ShopItemType.AURA -> "shop.equippedAura"
            ShopItemType.PET -> "shop.equippedPet"
            ShopItemType.EMOTE -> "shop.equippedEmote"
        }
        return try {
            userDoc.update(field, itemId).await()
            true
        } catch (_: Exception) {
            false
        }
    }
}
