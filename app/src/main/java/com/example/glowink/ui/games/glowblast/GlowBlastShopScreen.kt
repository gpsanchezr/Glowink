package com.example.glowink.ui.games.glowblast

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.components.GlowCard
import com.example.glowink.ui.components.GlowPrimaryButton
import com.example.glowink.ui.components.GlowSecondaryButton
import com.example.glowink.ui.games.glowblast.engine.InventoryManager
import com.example.glowink.ui.games.glowblast.engine.RewardSystem
import com.example.glowink.ui.games.glowblast.engine.UnlockSystem
import com.example.glowink.ui.games.glowblast.model.GlowAvatarConfig
import com.example.glowink.ui.games.glowblast.renderer.GlowCharacterCanvas
import com.example.glowink.ui.games.glowblast.renderer.GlowCharacterPresets
import com.example.glowink.ui.theme.*

private data class ShopItem(
    val id: String,
    val name: String,
    val category: String,
    val priceCoins: Int,
    val value: String
)

/**
 * FASE 17: Pantalla de Tienda Neón Integrada con el Ecosistema y AvatarConfig de Glowink.
 */
@Composable
fun GlowBlastShopScreen(
    currentConfig: GlowAvatarConfig = GlowCharacterPresets.NEO,
    onAvatarUpdated: (GlowAvatarConfig) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var wallet by remember { mutableStateOf(RewardSystem.getWallet(context)) }
    var activeCategory by remember { mutableStateOf("PEINADOS") }
    var avatarConfig by remember { mutableStateOf(currentConfig) }

    val shopCatalog = listOf(
        // PEINADOS
        ShopItem("MOHAWK_NEON", "Mohawk Neón", "PEINADOS", 0, "MOHAWK_NEON"),
        ShopItem("LARGO_ONDULADO", "Largo Ondulado", "PEINADOS", 250, "LARGO_ONDULADO"),
        ShopItem("BOB_PIXIE", "Bob Pixie", "PEINADOS", 300, "BOB_PIXIE"),
        ShopItem("CORTO_CLASICO", "Corto Clásico", "PEINADOS", 200, "CORTO_CLASICO"),

        // AURAS
        ShopItem("#00F0FF", "Aura Cian", "AURAS", 0, "#00F0FF"),
        ShopItem("#FF007F", "Aura Magenta", "AURAS", 400, "#FF007F"),
        ShopItem("#9D4EDD", "Aura Púrpura", "AURAS", 350, "#9D4EDD"),
        ShopItem("#39FF14", "Aura Lima", "AURAS", 300, "#39FF14"),

        // ATUENDOS
        ShopItem("HOODIE_NEON", "Hoodie Neón", "ATUENDOS", 0, "HOODIE_NEON"),
        ShopItem("CHAQUETA_CIRCUITO", "Chaqueta Circuito", "ATUENDOS", 500, "CHAQUETA_CIRCUITO"),
        ShopItem("BOMBER_HOLO", "Bomber Holo", "ATUENDOS", 450, "BOMBER_HOLO"),
        ShopItem("OVEROL_CYBER", "Overol Cyber", "ATUENDOS", 400, "OVEROL_CYBER"),

        // GAFAS/VISOR
        ShopItem("NEON_GLASSES", "Gafas Píxel", "GAFAS/VISOR", 0, "NEON_GLASSES"),
        ShopItem("VR_HEADSET", "Visor VR", "GAFAS/VISOR", 600, "VR_HEADSET")
    )

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            com.example.glowink.ui.theme.GlowinkMainBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // BARRA SUPERIOR CON MONEDAS Y GEMAS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlowSecondaryButton(
                            text = "←",
                            onClick = onBack,
                            borderColor = ElectricCyan,
                            textColor = ElectricCyan,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 16.sp
                        )

                        Text(
                            text = "TIENDA NEÓN",
                            color = ElectricCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xAA100B2A))
                                    .border(1.dp, NeonLime, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("🪙 ${wallet.glowCoins}", color = NeonLime, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xAA100B2A))
                                    .border(1.dp, ElectricCyan, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("💎 ${wallet.gems}", color = ElectricCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // VISTA PREVIA DEL AVATAR VECTORIAL EN TIEMPO REAL
                    GlowCard(
                        shape = RoundedCornerShape(26.dp),
                        borderColor = ElectricCyan,
                        borderWidth = 2.dp,
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GlowCharacterCanvas(
                                config = avatarConfig,
                                modifier = Modifier.size(170.dp)
                            )
                        }

                        Text(
                            text = "AVATAR ACTUALIZADO EN TIEMPO REAL",
                            color = NeonLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // PESTAÑAS DE CATEGORÍA
                    val categories = listOf("PEINADOS", "AURAS", "ATUENDOS", "GAFAS/VISOR")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat == activeCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ElectricCyan else Color(0x221E1735))
                                    .border(1.dp, if (isSelected) ElectricCyan else Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable { activeCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // REJILLA DE ARTÍCULOS DE LA CATEGORÍA
                    val categoryItems = shopCatalog.filter { it.category == activeCategory }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categoryItems.forEach { item ->
                            val isUnlocked = InventoryManager.isItemUnlocked(context, item.id)
                            val isEquipped = when (item.category) {
                                "PEINADOS" -> avatarConfig.hairStyle == item.value
                                "AURAS" -> avatarConfig.auraColor == item.value
                                "ATUENDOS" -> avatarConfig.clothingStyle == item.value
                                "GAFAS/VISOR" -> avatarConfig.accessoryId == item.value
                                else -> false
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isEquipped) Color(0x3300F0FF) else Color(0x221E1735))
                                    .border(1.dp, if (isEquipped) ElectricCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = if (isUnlocked) "Desbloqueado" else "Precio: 🪙 ${item.priceCoins}",
                                            color = if (isUnlocked) NeonLime else Color(0xFFFFD24C),
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isEquipped) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ElectricCyan)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("✓ EQUIPADO", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    } else if (isUnlocked) {
                                        GlowSecondaryButton(
                                            text = "EQUIPAR",
                                            onClick = {
                                                avatarConfig = when (item.category) {
                                                    "PEINADOS" -> avatarConfig.copy(hairStyle = item.value, hairColor = if (item.value == "LARGO_ONDULADO") "#FF007F" else "#00F0FF")
                                                    "AURAS" -> avatarConfig.copy(auraColor = item.value)
                                                    "ATUENDOS" -> avatarConfig.copy(clothingStyle = item.value)
                                                    "GAFAS/VISOR" -> avatarConfig.copy(accessoryId = item.value)
                                                    else -> avatarConfig
                                                }
                                                onAvatarUpdated(avatarConfig)
                                                Toast.makeText(context, "✨ ${item.name} equipado", Toast.LENGTH_SHORT).show()
                                            },
                                            borderColor = NeonLime,
                                            textColor = NeonLime,
                                            fontSize = 11.sp,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    } else {
                                        GlowPrimaryButton(
                                            text = "COMPRAR 🪙${item.priceCoins}",
                                            onClick = {
                                                if (UnlockSystem.buyItemWithCoins(context, item.id, item.priceCoins)) {
                                                    wallet = RewardSystem.getWallet(context)
                                                    Toast.makeText(context, "🎉 ¡Compraste ${item.name}!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "❌ Monedas insuficientes", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            containerColor = Color(0xFFFF007F),
                                            contentColor = Color.White,
                                            shape = RoundedCornerShape(10.dp),
                                            fontSize = 11.sp,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                GlowPrimaryButton(
                    text = "GUARDAR Y CONTINUAR ▶",
                    onClick = {
                        onAvatarUpdated(avatarConfig)
                        onBack()
                    },
                    containerColor = ElectricCyan,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
