package com.example.glowink.ui.games

import androidx.compose.runtime.Composable
import com.example.glowink.ui.games.glowblast.GlowBlastScreen

@Composable
fun BombGameScreen(
    onExit: () -> Unit,
    onGameOver: (Int) -> Unit = {}
) {
    GlowBlastScreen(
        onBack = onExit,
        onPlayClick = { },
        onMultiplayerClick = { },
        onSettingsClick = { },
        onShopClick = { }
    )
}
