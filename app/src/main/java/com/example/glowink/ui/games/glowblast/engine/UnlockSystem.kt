package com.example.glowink.ui.games.glowblast.engine

import android.content.Context

/**
 * Sistema de Desbloqueo por Progresión de Nivel o Compra en Tienda.
 */
object UnlockSystem {

    fun canUnlockByLevel(context: Context, requiredLevel: Int): Boolean {
        val wallet = RewardSystem.getWallet(context)
        return wallet.level >= requiredLevel
    }

    fun buyItemWithCoins(context: Context, itemId: String, priceCoins: Int): Boolean {
        if (InventoryManager.isItemUnlocked(context, itemId)) return true
        if (RewardSystem.spendCoins(context, priceCoins)) {
            InventoryManager.unlockItem(context, itemId)
            return true
        }
        return false
    }
}
