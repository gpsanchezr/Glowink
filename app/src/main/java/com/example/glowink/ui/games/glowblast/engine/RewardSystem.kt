package com.example.glowink.ui.games.glowblast.engine

import android.content.Context
import com.example.glowink.ui.games.glowblast.model.Wallet

/**
 * Sistema de Economía y Distribución de Recompensas de Glow Blast.
 */
object RewardSystem {

    fun getWallet(context: Context): Wallet {
        val prefs = context.getSharedPreferences("GlowinkPrefs", Context.MODE_PRIVATE)
        val coins = prefs.getInt("glow_coins", 1500)
        val gems = prefs.getInt("glow_gems", 45)
        val xp = prefs.getInt("glow_xp", 500)
        val tokens = prefs.getInt("glow_tokens", 10)
        val level = (xp / 1000) + 1
        return Wallet(coins, gems, xp, tokens, level)
    }

    fun addRewards(context: Context, coins: Int, gems: Int, xp: Int, tokens: Int = 0): Wallet {
        val current = getWallet(context)
        val newCoins = (current.glowCoins + coins).coerceAtLeast(0)
        val newGems = (current.gems + gems).coerceAtLeast(0)
        val newXp = (current.xp + xp).coerceAtLeast(0)
        val newTokens = (current.tokens + tokens).coerceAtLeast(0)

        val prefs = context.getSharedPreferences("GlowinkPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("glow_coins", newCoins)
            .putInt("glow_gems", newGems)
            .putInt("glow_xp", newXp)
            .putInt("glow_tokens", newTokens)
            .apply()

        return Wallet(newCoins, newGems, newXp, newTokens, (newXp / 1000) + 1)
    }

    fun spendCoins(context: Context, amount: Int): Boolean {
        val wallet = getWallet(context)
        if (wallet.glowCoins >= amount) {
            addRewards(context, -amount, 0, 0, 0)
            return true
        }
        return false
    }

    fun spendGems(context: Context, amount: Int): Boolean {
        val wallet = getWallet(context)
        if (wallet.gems >= amount) {
            addRewards(context, 0, -amount, 0, 0)
            return true
        }
        return false
    }
}
