package com.example.glowink.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.chatsListUiState.collectAsState()
    val user = uiState.currentUser
    val context = LocalContext.current
    
    var selectedCategory by remember { mutableStateOf("Avatar") }
    val categories = listOf("Avatar", "Ropa", "Conjuntos", "Monedas", "Gemas", "Juegos")

    Scaffold(
        containerColor = ObsidianBackground,
        topBar = {
            TopAppBar(
                title = { Text("GLOW STORE 🛒", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = ElectricCyan)
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        Text("🪙 ${user?.glowCoins ?: 0}", color = NeonLime, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("💎 ${user?.gems ?: 0}", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF151026))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Color(0xFF151026), Color(0xFF1B1233))))
        ) {
            // Category Selector
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color.Transparent,
                contentColor = ElectricCyan,
                edgePadding = 16.dp,
                divider = {}
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category, fontSize = 14.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedCategory) {
                    "Avatar" -> ShopGrid(items = avatarItems) { item -> 
                        viewModel.buyItem(item.id, item.priceCoins, item.priceGems)
                        Toast.makeText(context, "Avatar ${item.name} adquirido!", Toast.LENGTH_SHORT).show()
                    }
                    "Ropa" -> ShopGrid(items = clothingItems) { item ->
                        viewModel.buyItem(item.id, item.priceCoins, item.priceGems)
                        Toast.makeText(context, "Prenda ${item.name} adquirida!", Toast.LENGTH_SHORT).show()
                    }
                    "Conjuntos" -> ShopGrid(items = bundleItems) { item ->
                        viewModel.buyItem(item.id, item.priceCoins, item.priceGems)
                        Toast.makeText(context, "Conjunto ${item.name} adquirido!", Toast.LENGTH_SHORT).show()
                    }
                    "Monedas" -> CurrencySection(
                        items = coinPacks,
                        onBuy = { pack ->
                            viewModel.buyCoins(pack.amount, pack.price)
                            Toast.makeText(context, "Compra de ${pack.amount} monedas exitosa", Toast.LENGTH_SHORT).show()
                        }
                    )
                    "Gemas" -> CurrencySection(
                        items = gemPacks,
                        onBuy = { pack ->
                            viewModel.buyGems(pack.amount, pack.price)
                            Toast.makeText(context, "Compra de ${pack.amount} gemas exitosa", Toast.LENGTH_SHORT).show()
                        }
                    )
                    "Juegos" -> ShopGrid(items = gameItems) { item ->
                        viewModel.unlockGame(item.id, item.priceCoins)
                        Toast.makeText(context, "${item.name} desbloqueado!", Toast.LENGTH_SHORT).show()
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botón de Cuenta de Pago
                GlassContainer(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    borderColor = UltravioletPurple
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Configurar Cuenta de Pago", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Gestiona tus métodos de pago", color = OnSurfaceMuted, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ShoppingCart, null, tint = ElectricCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun ShopGrid(items: List<ShopItem>, onBuy: (ShopItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(600.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            ShopCard(item, onBuy)
        }
    }
}

@Composable
fun ShopCard(item: ShopItem, onBuy: (ShopItem) -> Unit) {
    GlassContainer(
        shape = RoundedCornerShape(20.dp),
        borderColor = ElectricCyan.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(80.dp).background(Color(0x11FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(item.icon, fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.priceCoins > 0) {
                    Text("🪙 ${item.priceCoins}", color = NeonLime, fontSize = 12.sp)
                    if (item.priceGems > 0) Spacer(modifier = Modifier.width(8.dp))
                }
                if (item.priceGems > 0) {
                    Text("💎 ${item.priceGems}", color = Color(0xFF00F0FF), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onBuy(item) },
                colors = ButtonDefaults.buttonColors(containerColor = UltravioletPurple),
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Comprar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CurrencySection(items: List<CurrencyPack>, onBuy: (CurrencyPack) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { pack ->
            GlassContainer(
                shape = RoundedCornerShape(16.dp),
                borderColor = NeonLime.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(pack.icon, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("${pack.amount} ${pack.name}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Efecto Neón Garantizado", color = OnSurfaceMuted, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = { onBuy(pack) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime)
                    ) {
                        Text("$${pack.price}", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class ShopItem(val id: String, val name: String, val icon: String, val priceCoins: Int, val priceGems: Int)
data class CurrencyPack(val name: String, val amount: Int, val price: Double, val icon: String)

val avatarItems = listOf(
    ShopItem("hair_fem_2coleta", "Doble Coleta", "💇", 200, 0),
    ShopItem("hair_masc_2", "Mohawk Cyber", "💇", 250, 5),
    ShopItem("hair_fem_trenzas", "Trenzas Glow", "💇", 0, 15)
)

val clothingItems = listOf(
    ShopItem("ropa_3_fem", "Top Neón 3", "👕", 150, 0),
    ShopItem("ropa_4_fem", "Vestido Cyber 4", "👗", 0, 40),
    ShopItem("ropa_2_masc", "Chaqueta Neón", "🧥", 300, 0)
)

val bundleItems = listOf(
    ShopItem("b_1", "Kit Ciberpunk", "📦", 1000, 80),
    ShopItem("b_2", "Starter Pack", "🎒", 400, 20)
)

val coinPacks = listOf(
    CurrencyPack("Coins", 1000, 0.99, "💰"),
    CurrencyPack("Coins", 5000, 4.99, "💰💰"),
    CurrencyPack("Coins", 15000, 9.99, "🏦")
)

val gemPacks = listOf(
    CurrencyPack("Gems", 50, 1.99, "💎"),
    CurrencyPack("Gems", 300, 9.99, "💎💎"),
    CurrencyPack("Gems", 1000, 24.99, "👑")
)

val gameItems = listOf(
    ShopItem("quiz", "Cyber Quiz Pro", "🧠", 200, 0),
    ShopItem("chess", "Glow Chess", "♟️", 500, 50)
)
