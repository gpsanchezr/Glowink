package com.example.glowink.ui.games.glowblast.engine

import android.content.Context

/**
 * Gestor de Inventario Local de Cosméticos Desbloqueados para Glow Blast.
 */
object InventoryManager {

    fun getUnlockedItemIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences("GlowBlastInventory", Context.MODE_PRIVATE)
        val defaultItems = setOf("NEO", "LUNA", "ZETA", "VOLT", "MOHAWK_NEON", "NEON_GLASSES", "#00F0FF")
        return prefs.getStringSet("unlocked_items", defaultItems) ?: defaultItems
    }

    fun isItemUnlocked(context: Context, itemId: String): Boolean {
        return getUnlockedItemIds(context).contains(itemId)
    }

    fun unlockItem(context: Context, itemId: String) {
        val current = getUnlockedItemIds(context).toMutableSet()
        current.add(itemId)
        val prefs = context.getSharedPreferences("GlowBlastInventory", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("unlocked_items", current).apply()
    }
}
