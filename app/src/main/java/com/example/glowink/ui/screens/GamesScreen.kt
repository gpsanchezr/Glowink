package com.example.glowink.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.data.User
import com.example.glowink.ui.avatar.GlowAvatar
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

/**
 * Modelo de datos completo para la Plataforma de Juegos de Glowink.
 */
private data class Game(
    val id: String,
    val name: String,
    val category: String,
    val categoryTag: String,
    val rating: Double,
    val players: String,
    val emoji: String,
    val subtitle: String = "",
    val description: String = "",
    val isNew: Boolean = false,
    val isFeatured: Boolean = false,
    val isTopRanked: Boolean = false,
    val accentColor: Color = ElectricCyan,
    val bgGradient: List<Color> = listOf(Color(0xFF0F0A21), Color(0xFF1E1735))
)

private data class Category(
    val name: String,
    val icon: String,
    val color: Color
)

/**
 * "JUEGOS 🎮": Plataforma Arcade Cyberpunk / Neon completa para Glowink.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    viewModel: ChatViewModel,
    onTabClick: (Int) -> Unit = {},
    onPlayCulebra: () -> Unit = {},
    onPlayDuelo: () -> Unit = {},
    onPlayCarrera: () -> Unit = {},
    onPlayQuiz: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.chatsListUiState.collectAsState()
    val currentUser = uiState.currentUser

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    
    var selectedGameForDetail by remember { mutableStateOf<Game?>(null) }
    var verTodosSectionTitle by remember { mutableStateOf<String?>(null) }
    var showTournamentDialog by remember { mutableStateOf(false) }

    // 1. Categorías disponibles
    val categories = listOf(
        Category("Todos", "🎮", ElectricCyan),
        Category("Arcade", "🕹️", NeonLime),
        Category("Acción", "⚔️", Color(0xFFFF007F)),
        Category("Carreras", "🏁", Color(0xFFFFD24C)),
        Category("Peleas", "🥊", Color(0xFFFF4444)),
        Category("Aventura", "⛰️", UltravioletPurpleLight),
        Category("Multijugador", "👥", Color(0xFF007FFF)),
        Category("Favoritos", "⭐", Color(0xFFFFD24C))
    )

    // 2. Catálogo Oficial de los 12 Juegos de Glowink
    val allGames = remember {
        listOf(
            // --- JUEGO ESTRELLA PROGRESIVO CON MUNDOS ---
            Game(
                id = "culebra",
                name = "Culebra Glow",
                category = "Arcade",
                categoryTag = "Niveles y Mundos",
                rating = 4.8,
                players = "68K",
                emoji = "🐍",
                subtitle = "Mundo 2 • Nivel 7",
                description = "Come, crece, supera obstáculos y derrota al Rey Serpiente. ¡Completa niveles, acumula GlowCoins y consigue Gemas del Mundo!",
                isFeatured = true,
                accentColor = NeonLime,
                bgGradient = listOf(Color(0xFF0F2B07), Color(0xFF0A071A), Color(0xFF003D33))
            ),
            Game(
                id = "neon_invasion",
                name = "Neon Invasion",
                category = "Acción",
                categoryTag = "Shooter",
                rating = 4.7,
                players = "54K",
                emoji = "👾",
                subtitle = "Oleadas de enemigos y jefes",
                description = "Controla tu nave cibernética, dispara rayos láser, esquiva proyectiles y destruye a las hordas invasoras y jefes de nivel.",
                isFeatured = true,
                accentColor = ElectricCyan,
                bgGradient = listOf(Color(0xFF002F4B), Color(0xFF0A071A), Color(0xFF2E004F))
            ),
            Game(
                id = "glow_fighters",
                name = "Glow Fighters",
                category = "Peleas",
                categoryTag = "1 vs 1",
                rating = 4.8,
                players = "38K",
                emoji = "🥊",
                subtitle = "Combates 1v1, habilidades y combos",
                description = "Enfréntate en duelos de combate físico con golpes especiales, combos mortales y barras de energía en tiempo real.",
                accentColor = Color(0xFFFF4444),
                bgGradient = listOf(Color(0xFF4A0E0E), Color(0xFF0A071A))
            ),
            Game(
                id = "neon_rush",
                name = "Neon Rush",
                category = "Carreras",
                categoryTag = "Carreras",
                rating = 4.6,
                players = "42K",
                emoji = "🏎️",
                subtitle = "Carros, nitro, obstáculos y pistas",
                description = "Pilota superdeportivos neón en circuitos de alta velocidad. Usa nitro en rectas y esquiva trampas en la pista.",
                accentColor = Color(0xFFFFD24C),
                bgGradient = listOf(Color(0xFF3B2E0B), Color(0xFF0A071A))
            ),
            Game(
                id = "cyber_riders",
                name = "Cyber Riders",
                category = "Carreras",
                categoryTag = "Motos",
                rating = 4.7,
                players = "32K",
                emoji = "🏍️",
                subtitle = "Carreras extremas, saltos y rampas",
                description = "Compite en motocicletas futuristas saltando por rampas luminosas y sorteando abismos en la ciudad nocturna.",
                isNew = true,
                accentColor = ElectricCyan,
                bgGradient = listOf(Color(0xFF0E2A38), Color(0xFF0A071A))
            ),
            Game(
                id = "dungeon_glow",
                name = "Dungeon Glow",
                category = "Aventura",
                categoryTag = "Aventura",
                rating = 4.8,
                players = "41K",
                emoji = "⚔️",
                subtitle = "Explorar escenarios, llaves y jefe",
                description = "Adéntrate en mazmorras subterráneas, encuentra llaves secretas, abre cofres de tesoro y derrota al Guardián de las sombras.",
                isNew = true,
                accentColor = UltravioletPurpleLight,
                bgGradient = listOf(Color(0xFF28133D), Color(0xFF0A071A))
            ),
            Game(
                id = "neon_archers",
                name = "Neon Archers",
                category = "Acción",
                categoryTag = "Precisión",
                rating = 4.6,
                players = "28K",
                emoji = "🏹",
                subtitle = "Arcos, enemigos y objetivos móviles",
                description = "Pon a prueba tu puntería disparando flechas de plasma a blancos en movimiento e invasores lejanos.",
                isNew = true,
                accentColor = Color(0xFFFF007F),
                bgGradient = listOf(Color(0xFF3D051E), Color(0xFF0A071A))
            ),
            Game(
                id = "bomb_squad",
                name = "Bomb Squad",
                category = "Arcade",
                categoryTag = "Estrategia",
                rating = 4.5,
                players = "25K",
                emoji = "🧨",
                subtitle = "Bombas, laberintos, enemigos y jefe",
                description = "Coloca bombas con temporizador para abrirte paso a través de laberintos, destruir bloques y atrapar monstruos.",
                isNew = true,
                accentColor = Color(0xFFFFA500),
                bgGradient = listOf(Color(0xFF3D2005), Color(0xFF0A071A))
            ),
            Game(
                id = "glow_run",
                name = "Glow Run",
                category = "Arcade",
                categoryTag = "Runner",
                rating = 4.6,
                players = "30K",
                emoji = "🏃",
                subtitle = "Correr, saltar, deslizarse y esquivar",
                description = "Un runner desenfrenado donde debes saltar obstáculos, deslizarte por debajo de barreras y recolectar miles de GlowCoins.",
                accentColor = NeonLime,
                bgGradient = listOf(Color(0xFF133B18), Color(0xFF0A071A))
            ),
            Game(
                id = "cyber_rush",
                name = "Cyber Rush",
                category = "Estrategia",
                categoryTag = "Reflejos",
                rating = 4.9,
                players = "64K",
                emoji = "🧠",
                subtitle = "Decisiones rápidas, obstáculos y combos",
                description = "Toma decisiones relámpago respondiendo preguntas y esquivando trampas a velocidades vertiginosas.",
                accentColor = Color(0xFFFFD24C),
                bgGradient = listOf(Color(0xFF3D2E00), Color(0xFF0A071A))
            ),
            Game(
                id = "kingdom_clash",
                name = "Kingdom Clash",
                category = "Aventura",
                categoryTag = "Arcade Aventura",
                rating = 4.8,
                players = "35K",
                emoji = "👑",
                subtitle = "Avanzar por mapas y derrotar al jefe",
                description = "Recorre mapas dinámicos liberando zonas conquistadas hasta llegar al castillo del jefe final del reino.",
                accentColor = UltravioletPurpleLight,
                bgGradient = listOf(Color(0xFF29103D), Color(0xFF0A071A))
            ),
            Game(
                id = "duel_neon",
                name = "Duel Neon",
                category = "Multijugador",
                categoryTag = "PvP",
                rating = 4.8,
                players = "98K",
                emoji = "⚡",
                subtitle = "Duelo rápido contra otro jugador",
                description = "Reta a tus amigos del Chat en duelos cara a cara. Demuestra tu tiempo de reacción y reclama la victoria.",
                accentColor = ElectricCyan,
                bgGradient = listOf(Color(0xFF002F4B), Color(0xFF0A071A))
            )
        )
    }

    // 3. Lógica de Filtrado
    val filteredGames = remember(searchQuery, selectedCategory, currentUser?.favoriteGames) {
        allGames.filter { game ->
            val matchesSearch = searchQuery.isBlank() ||
                    game.name.contains(searchQuery, ignoreCase = true) ||
                    game.category.contains(searchQuery, ignoreCase = true) ||
                    game.categoryTag.contains(searchQuery, ignoreCase = true)
            
            val matchesCategory = when (selectedCategory) {
                "Todos" -> true
                "Favoritos" -> currentUser?.favoriteGames?.contains(game.id) == true
                else -> game.category.equals(selectedCategory, ignoreCase = true)
            }
            matchesSearch && matchesCategory
        }
    }

    val featuredGames = remember { allGames.filter { it.isFeatured } }
    val popularGames = remember { allGames.filter { !it.isFeatured && !it.isNew } }
    val newGames = remember { allGames.filter { it.isNew } }

    val isSearchingOrFiltering = searchQuery.isNotBlank() || selectedCategory != "Todos"

    Scaffold(
        containerColor = ObsidianBackground,
        bottomBar = { GlowinkBottomNavigationBar(selectedTab = 1, onTabClick = onTabClick) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GlowinkArenaBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. HEADER (Con Avatar de Usuario + Nivel + GlowCoins + Gemas + Buscador)
                GamesHeader(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    user = currentUser,
                    onAvatarClick = { onTabClick(2) },
                    onCoinsClick = { /* Abre Tienda/Economía */ }
                )

                // 2. CATEGORÍAS (Fila horizontal de chips neón)
                CategorySection(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it }
                )

                if (isSearchingOrFiltering) {
                    if (filteredGames.isEmpty()) {
                        EmptySearchState(query = searchQuery, category = selectedCategory)
                    } else {
                        SectionHeader(
                            title = if (searchQuery.isNotBlank()) "Resultados (${filteredGames.size})" else "Juegos de $selectedCategory",
                            showVerTodos = false
                        )
                        FilteredGamesGrid(games = filteredGames) { game ->
                            selectedGameForDetail = game
                        }
                    }
                } else {
                    // 3. HERO BANNER DESTACADO (Con Panel de Progresión de Mundos)
                    SectionHeader("🔥 DESTACADO", showVerTodos = false)
                    HeroFeaturedBanner(
                        featuredGames = featuredGames,
                        onPlayClick = { game -> selectedGameForDetail = game }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. MÁS JUGADOS
                    SectionHeader(
                        title = "🔥 Más jugados",
                        onVerTodos = { verTodosSectionTitle = "Más jugados" }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(popularGames) { game ->
                            PopularGameCard(game = game) {
                                selectedGameForDetail = game
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. BANNER RETOS Y TORNEOS
                    TournamentPromotionBanner(
                        onClick = { showTournamentDialog = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. NUEVOS LANZAMIENTOS
                    SectionHeader(
                        title = "⭐ Nuevos lanzamientos",
                        onVerTodos = { verTodosSectionTitle = "Nuevos lanzamientos" }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(newGames) { game ->
                            PopularGameCard(game = game, showNewLabel = true) {
                                selectedGameForDetail = game
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7. JUEGOS POR CATEGORÍA
                    SectionHeader(
                        title = "🎮 Juegos por categoría",
                        onVerTodos = { selectedCategory = "Todos" }
                    )
                    CategoryGridSection(
                        onSelectCategory = { catName -> selectedCategory = catName }
                    )

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }

    // Modal de Detalle de Juego con Animación
    if (selectedGameForDetail != null) {
        val g = selectedGameForDetail!!
        val isFav = currentUser?.favoriteGames?.contains(g.id) == true
        GameDetailDialog(
            game = g,
            isFavorite = isFav,
            onToggleFavorite = { viewModel.toggleFavoriteGame(g.id) },
            onDismiss = { selectedGameForDetail = null },
            onPlay = {
                selectedGameForDetail = null
                launchGame(g.id, onPlayCulebra, onPlayDuelo, onPlayCarrera, onPlayQuiz, context)
            }
        )
    }

    // Modal "Ver Todos"
    if (verTodosSectionTitle != null) {
        val gamesToShow = if (verTodosSectionTitle == "Nuevos lanzamientos") newGames else popularGames
        VerTodosDialog(
            title = verTodosSectionTitle!!,
            games = gamesToShow,
            onDismiss = { verTodosSectionTitle = null },
            onGameClick = { game ->
                verTodosSectionTitle = null
                selectedGameForDetail = game
            }
        )
    }

    // Modal Informativo de Torneos con Animación
    if (showTournamentDialog) {
        TournamentDetailDialog(onDismiss = { showTournamentDialog = false })
    }
}

// ============================================================================
// COMPONENTES DE UI
// ============================================================================

/**
 * 1. HEADER: Responsive para pantallas pequeñas (<360dp) + Pill Bar de Usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamesHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    user: User?,
    onAvatarClick: () -> Unit,
    onCoinsClick: () -> Unit
) {
    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val isNarrow = screenWidthDp < 360
    val innerSpacing = if (isNarrow) 6.dp else 12.dp

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isNarrow) 40.dp else 46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(listOf(Color(0x3300F0FF), Color(0x337B2CBF)))
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(listOf(ElectricCyan, UltravioletPurpleLight, NeonLime)),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎮", fontSize = if (isNarrow) 20.sp else 24.sp)
                }

                Spacer(modifier = Modifier.width(innerSpacing))

                Column {
                    Text(
                        text = "JUEGOS",
                        fontSize = if (isNarrow) 22.sp else 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Juega • Conecta • Gana",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElectricCyan.copy(alpha = 0.9f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x221E1735))
                    .border(1.2.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .bounceClick { onCoinsClick() }
                    .padding(horizontal = if (isNarrow) 8.dp else 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NeonLime, CircleShape)
                            .bounceClick { onAvatarClick() }
                    ) {
                        GlowAvatar(
                            config = user?.avatarConfig ?: com.example.glowink.data.Avatar3DConfig(),
                            profileImageUrl = user?.profileImageUrl,
                            animate = false,
                            modifier = Modifier.fillMaxSize().padding(2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = "Nivel ${user?.nivel ?: 1}",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isNarrow) "🪙 ${user?.glowCoins ?: 238}" else "🪙 ${user?.glowCoins ?: 238} Coins", color = NeonLime, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("💎 ${user?.gems ?: 3}", color = ElectricCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            placeholder = { Text("Buscar juegos...", fontSize = 14.sp, color = OnSurfaceMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Filtros",
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(26.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0x26140F24),
                unfocusedContainerColor = Color(0x1F140F24),
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = ElectricCyan.copy(alpha = 0.4f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

/**
 * 2. CATEGORÍAS: Fila horizontal de chips neón con microanimación bounceClick.
 */
@Composable
private fun CategorySection(
    categories: List<Category>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        items(categories) { cat ->
            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(
                                listOf(cat.color.copy(alpha = 0.35f), Color(0x331E1735))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(Color(0x1AFFFFFF), Color(0x0AFFFFFF))
                            )
                        }
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) cat.color else Color(0x22FFFFFF),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .bounceClick { onCategorySelect(cat.name) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = cat.icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cat.name,
                        color = if (isSelected) Color.White else OnSurfaceMuted,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 3. HERO FEATURED BANNER CON PANEL DE PROGRESIÓN DE MUNDOS.
 */
@Composable
private fun HeroFeaturedBanner(
    featuredGames: List<Game>,
    onPlayClick: (Game) -> Unit
) {
    if (featuredGames.isEmpty()) return

    var currentPage by remember { mutableIntStateOf(0) }
    val currentGame = featuredGames[currentPage % featuredGames.size]

    LaunchedEffect(currentPage) {
        delay(6000L)
        currentPage = (currentPage + 1) % featuredGames.size
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(currentGame.bgGradient)
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        currentGame.accentColor,
                        ElectricCyan.copy(alpha = 0.8f),
                        UltravioletPurpleLight
                    )
                ),
                RoundedCornerShape(26.dp)
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -30) {
                        currentPage = (currentPage + 1) % featuredGames.size
                    } else if (dragAmount > 30) {
                        currentPage = if (currentPage - 1 < 0) featuredGames.size - 1 else currentPage - 1
                    }
                }
            }
            .bounceClick { onPlayClick(currentGame) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFF007F).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFFF007F), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 DESTACADO",
                        color = Color(0xFFFF80BF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column {
                    Text(
                        text = currentGame.name.uppercase(),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = currentGame.subtitle.ifBlank { "Come, crece y derrota al Rey Serpiente." },
                        color = ElectricCyan,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "¡Explora nuevos mundos y junta Gemas!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.5.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD24C), modifier = Modifier.size(13.dp))
                        Text(
                            text = " ${currentGame.rating}   👥 ${currentGame.players} jugadores",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = { onPlayClick(currentGame) },
                    colors = ButtonDefaults.buttonColors(containerColor = currentGame.accentColor),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("▶  JUGAR AHORA", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                        Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .width(105.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val worldList = listOf(
                    Triple(1, "Mundo 1", "✓"),
                    Triple(2, "Mundo 2", ">"),
                    Triple(3, "Mundo 3", "🔒"),
                    Triple(4, "Mundo 4", "🔒"),
                    Triple(5, "Mundo 5", "🔒")
                )

                worldList.forEach { (wNum, wName, wIcon) ->
                    val isActive = wNum == 2
                    val isDone = wNum == 1

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isActive) ElectricCyan.copy(alpha = 0.25f) else Color(0x33140F24)
                            )
                            .border(
                                width = if (isActive) 1.2.dp else 0.8.dp,
                                color = if (isActive) ElectricCyan else (if (isDone) NeonLime.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(wName, color = Color.White, fontSize = 10.sp, fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium)
                            Text(
                                text = wIcon,
                                color = if (isDone) NeonLime else (if (isActive) ElectricCyan else Color.White.copy(alpha = 0.5f)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. TARJETA COMPACTA DE JUEGO (Popular / Nuevo Lanzamiento con BounceClick).
 */
@Composable
private fun PopularGameCard(
    game: Game,
    showNewLabel: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(135.dp)
            .bounceClick { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(width = 135.dp, height = 135.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.linearGradient(game.bgGradient))
                .border(1.2.dp, game.accentColor.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = game.accentColor.copy(alpha = 0.25f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            }

            Text(text = game.emoji, fontSize = 54.sp)

            if (showNewLabel || game.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricCyan)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("NEW", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC0A071A))
                    .border(0.8.dp, game.accentColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = game.categoryTag,
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = game.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD24C), modifier = Modifier.size(12.dp))
            Text(
                text = " ${game.rating}  👤 ${game.players}",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.sp
            )
        }
    }
}

/**
 * 5. BANNER ESPECIAL RETOS Y TORNEOS.
 */
@Composable
private fun TournamentPromotionBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF001F3F), Color(0xFF1B1038), Color(0xFF2E004F))
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(NeonLime, ElectricCyan, UltravioletPurpleLight)),
                RoundedCornerShape(24.dp)
            )
            .bounceClick { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x3339FF14)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏆", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "RETOS Y TORNEOS",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Compite y consigue recompensas",
                        color = NeonLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("VER TORNEOS", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/**
 * 7. JUEGOS POR CATEGORÍA.
 */
@Composable
private fun CategoryGridSection(onSelectCategory: (String) -> Unit) {
    val categoryCards = remember {
        listOf(
            Category("Arcade", "🕹️", NeonLime),
            Category("Carreras", "🏁", Color(0xFFFFD24C)),
            Category("Acción", "⚔️", Color(0xFFFF007F)),
            Category("Aventura", "⛰️", UltravioletPurpleLight),
            Category("Multijugador", "👥", Color(0xFF007FFF))
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    ) {
        items(categoryCards) { cat ->
            Box(
                modifier = Modifier
                    .width(145.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(cat.color.copy(alpha = 0.25f), Color(0x221E1735))
                        )
                    )
                    .border(1.2.dp, cat.color.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                    .bounceClick { onSelectCategory(cat.name) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = cat.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = cat.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FilteredGamesGrid(games: List<Game>, onGameClick: (Game) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        games.chunked(2).forEach { rowGames ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowGames.forEach { game ->
                    Box(modifier = Modifier.weight(1f)) {
                        PopularGameCard(game = game) { onGameClick(game) }
                    }
                }
                if (rowGames.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, showVerTodos: Boolean = true, onVerTodos: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        if (showVerTodos) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.bounceClick { onVerTodos() }
            ) {
                Text(
                    text = "Ver todos",
                    color = UltravioletPurpleLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.KeyboardArrowRight, null, tint = UltravioletPurpleLight, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmptySearchState(query: String, category: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🛰️", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No encontramos juegos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(
            text = if (query.isNotBlank()) "No hay resultados para '$query'" else "No hay juegos en '$category'",
            color = OnSurfaceMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GameDetailDialog(
    game: Game,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1233),
        shape = RoundedCornerShape(28.dp),
        confirmButton = {
            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = game.accentColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("▶  JUGAR AHORA", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFFFD24C) else Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(26.dp)
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("CERRAR", color = OnSurfaceMuted, fontWeight = FontWeight.Bold)
                }
            }
        },
        title = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(95.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Brush.linearGradient(game.bgGradient))
                            .border(1.5.dp, game.accentColor, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(game.emoji, fontSize = 48.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(game.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    Text("${game.category} • ${game.categoryTag}", color = game.accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD24C), modifier = Modifier.size(15.dp))
                        Text(" ${game.rating}   👥 ${game.players} jugadores", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = game.description.ifBlank { "Domina las arenas neón de Glowink. Reta a tus amigos y acumula victorias." },
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}

@Composable
private fun VerTodosDialog(title: String, games: List<Game>, onDismiss: () -> Unit, onGameClick: (Game) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianBackground,
        shape = RoundedCornerShape(24.dp),
        confirmButton = { TextButton(onClick = onDismiss) { Text("CERRAR", color = ElectricCyan, fontWeight = FontWeight.Bold) } },
        title = { Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black) },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(380.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(games) { game -> PopularGameCard(game = game) { onGameClick(game) } }
                }
            }
        }
    )
}

@Composable
private fun TournamentDetailDialog(onDismiss: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1233),
        shape = RoundedCornerShape(24.dp),
        confirmButton = { Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = NeonLime)) { Text("ENTENDIDO", color = Color.Black, fontWeight = FontWeight.Black) } },
        title = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut()
            ) {
                Row { Text("🏆 ", fontSize = 24.sp); Text("Torneos Glowink", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp) }
            }
        },
        text = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut()
            ) {
                Text("Demuestra tu habilidad en Culebra Glow, Duelo Neon y Neon Rush para ganar más de 10,000 GlowCoins y Gemas especiales.", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }
    )
}

/**
 * Helper para mapear los juegos del catálogo a sus pantallas dedicadas según Categoría o ID.
 */
fun launchGame(
    gameId: String,
    category: String,
    navController: androidx.navigation.NavController
) {
    when {
        category.equals("Peleas", ignoreCase = true) || gameId in listOf("glow_fighters", "duel_neon", "duelo") -> {
            navController.navigate(com.example.glowink.ui.navigation.GlowinkRoutes.GAME_DUEL)
        }
        category.equals("Carreras", ignoreCase = true) || gameId in listOf("neon_rush", "cyber_riders", "carrera", "asphalt", "glow_run") -> {
            navController.navigate(com.example.glowink.ui.navigation.GlowinkRoutes.GAME_RACE)
        }
        category.equals("Estrategia", ignoreCase = true) || category.equals("Trivia", ignoreCase = true) || gameId in listOf("cyber_rush", "quiz", "cyber_quiz") -> {
            navController.navigate(com.example.glowink.ui.navigation.GlowinkRoutes.GAME_QUIZ)
        }
        else -> {
            navController.navigate(com.example.glowink.ui.navigation.GlowinkRoutes.GAME_SNAKE)
        }
    }
}

private fun launchGame(
    id: String,
    onCulebra: () -> Unit,
    onDuelo: () -> Unit,
    onCarrera: () -> Unit,
    onQuiz: () -> Unit,
    context: android.content.Context
) {
    when (id.lowercase()) {
        "glow_fighters", "duel_neon", "duelo", "free_fire", "cod", "brawl" -> onDuelo()
        "neon_rush", "cyber_riders", "carrera", "asphalt", "glow_run" -> onCarrera()
        "cyber_rush", "quiz", "cyber_quiz" -> onQuiz()
        else -> onCulebra()
    }
}
