package com.example.glowink.ui.avatar

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glowink.R
import com.example.glowink.ui.theme.*
import com.example.glowink.ui.viewmodel.AvatarCategory
import com.example.glowink.ui.viewmodel.AvatarItem
import com.example.glowink.ui.viewmodel.AvatarState
import com.example.glowink.ui.viewmodel.AvatarViewModel
import com.example.glowink.ui.viewmodel.ChatViewModel
import com.example.glowink.ui.viewmodel.CurrencyType

/**
 * Stateful Composable: Maneja la lógica del ViewModel y el estado del Avatar.
 */
@Composable
fun AvatarCreationScreen(
    viewModel: AvatarViewModel = viewModel(),
    chatViewModel: ChatViewModel? = null,
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    AvatarCreationContent(
        state = state,
        catalog = viewModel.catalog,
        onCategorySelected = viewModel::onCategorySelected,
        onItemEquipped = viewModel::onItemEquipped,
        onColorSelected = viewModel::onColorSelected,
        onResetToDefault = viewModel::onResetToDefault,
        onGenderSelected = viewModel::onGenderSelected,
        onViewModeSelected = viewModel::onViewModeSelected,
        onSaveAvatar = {
            viewModel.onSaveAvatar {
                // Actualiza la configuración global del avatar en ChatViewModel y Firestore
                chatViewModel?.updateAvatarConfig(
                    com.example.glowink.data.Avatar3DConfig(
                        siluetaBase = state.gender,
                        estiloPelo = state.equippedItems[AvatarCategory.HAIR]?.id ?: "hair_fem_ondas",
                        estiloRopa = state.equippedItems[AvatarCategory.TOP]?.id ?: "ropa_1_fem"
                    )
                )
                Toast.makeText(context, "¡Avatar guardado exitosamente! ✨", Toast.LENGTH_SHORT).show()
                onDone()
            }
        }
    )
}

/**
 * Stateless Composable: UI pura que recibe el estado y los callbacks de acción.
 */
@Composable
fun AvatarCreationContent(
    state: AvatarState,
    catalog: List<AvatarItem>,
    onCategorySelected: (AvatarCategory) -> Unit,
    onItemEquipped: (AvatarItem) -> Unit,
    onColorSelected: (Color) -> Unit,
    onResetToDefault: () -> Unit,
    onGenderSelected: (String) -> Unit,
    onViewModeSelected: (String) -> Unit,
    onSaveAvatar: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF0C1017),
        bottomBar = {
            AvatarBottomBar(
                isSaving = state.isSaving,
                onReset = onResetToDefault,
                onSave = onSaveAvatar
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. HEADER Y CONTROLES SUPERIORES (Género + Capas)
            AvatarHeaderSection(
                gender = state.gender,
                viewMode = state.viewMode,
                onGenderSelected = onGenderSelected,
                onViewModeSelected = onViewModeSelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. CUERPO PRINCIPAL (Vista 45% Izquierda | Panel Control 55% Derecha)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ÁREA IZQUIERDA: Renderizado del Avatar con Z-Index Estricto
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                ) {
                    AvatarPreviewCard(state = state)
                }

                // ÁREA DERECHA: Panel de Selección Multicapa (Navegación + Grilla + Colores)
                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // SECCIÓN A: Navegación por Categorías (LazyRow)
                    CategoryNavBar(
                        selectedCategory = state.selectedCategory,
                        onCategorySelect = onCategorySelected
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Selecciona ${state.selectedCategory.label}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // SECCIÓN B: Catálogo en Grilla de 2 Columnas (LazyVerticalGrid)
                    Box(modifier = Modifier.weight(1f)) {
                        CatalogGrid(
                            catalog = catalog,
                            state = state,
                            onItemEquipped = onItemEquipped
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // SECCIÓN C: Selector de Colores (LazyRow)
                    ColorPickerBar(
                        appliedColor = state.appliedColors[state.selectedCategory],
                        onColorSelected = onColorSelected
                    )
                }
            }
        }
    }
}

// ============================================================================
// COMPONENTES DE UI
// ============================================================================

/**
 * Header con Título, Subtítulo y selectores de Género y Modo de Vista.
 */
@Composable
private fun AvatarHeaderSection(
    gender: String,
    viewMode: String,
    onGenderSelected: (String) -> Unit,
    onViewModeSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Crear tu Personaje",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Avatar vestido por defecto • Selecciona prendas para editar",
            color = ElectricCyan,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Fila 1: Selector de Género
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x221E1735))
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isFem = gender == "FEMENINO"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isFem) Color(0x3300F0FF) else Color.Transparent)
                    .border(if (isFem) 1.dp else 0.dp, if (isFem) ElectricCyan else Color.Transparent, RoundedCornerShape(18.dp))
                    .clickable { onGenderSelected("FEMENINO") },
                contentAlignment = Alignment.Center
            ) {
                Text("👧 FEMENINO", color = if (isFem) Color.White else OnSurfaceMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (!isFem) Color(0x3300F0FF) else Color.Transparent)
                    .border(if (!isFem) 1.dp else 0.dp, if (!isFem) ElectricCyan else Color.Transparent, RoundedCornerShape(18.dp))
                    .clickable { onGenderSelected("MASCULINO") },
                contentAlignment = Alignment.Center
            ) {
                Text("👦 MASCULINO", color = if (!isFem) Color.White else OnSurfaceMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Fila 2: Modo de Vista
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0x11FFFFFF))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isFull = viewMode == "AVATAR_COMPLETO"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isFull) Color(0x2200F0FF) else Color.Transparent)
                    .clickable { onViewModeSelected("AVATAR_COMPLETO") },
                contentAlignment = Alignment.Center
            ) {
                Text("👗 AVATAR COMPLETO", color = if (isFull) ElectricCyan else OnSurfaceMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (!isFull) Color(0x2200F0FF) else Color.Transparent)
                    .clickable { onViewModeSelected("VISTA_CAPAS") },
                contentAlignment = Alignment.Center
            ) {
                Text("🥞 VISTA EN CAPAS", color = if (!isFull) ElectricCyan else OnSurfaceMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Renderizado del Avatar con apilado Z-Index estricto y plataforma con controles.
 */
@Composable
private fun AvatarPreviewCard(state: AvatarState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF090D16)))
            )
            .border(1.5.dp, ElectricCyan, RoundedCornerShape(26.dp))
    ) {
        // 1. BASE / FONDO PLATAFORMA NEÓN
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = ElectricCyan.copy(alpha = 0.15f),
                radius = w * 0.42f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.82f)
            )
        }

        // 2. APILADO Z-INDEX ESTRICTO DE CAPAS DEL AVATAR
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            // Z-Index 1: BODY (Cuerpo Base)
            state.equippedItems[AvatarCategory.BODY]?.let { item ->
                RenderLayerImage(item = item, color = state.appliedColors[AvatarCategory.BODY])
            }

            // Z-Index 2: FACE (Rostro / Ojos)
            state.equippedItems[AvatarCategory.FACE]?.let { item ->
                RenderLayerImage(item = item, color = state.appliedColors[AvatarCategory.FACE])
            }

            // Z-Index 3: BOTTOM (Pantalones / Faldas)
            state.equippedItems[AvatarCategory.BOTTOM]?.let { item ->
                RenderLayerImage(item = item, color = state.appliedColors[AvatarCategory.BOTTOM])
            }

            // Z-Index 4: SHOES (Zapatos sobre pies)
            state.equippedItems[AvatarCategory.SHOES]?.let { item ->
                RenderLayerImage(item = item, color = state.appliedColors[AvatarCategory.SHOES])
            }

            // Z-Index 5: TOP (Camisetas / Chaquetas)
            state.equippedItems[AvatarCategory.TOP]?.let { item ->
                RenderLayerImage(item = item, color = state.appliedColors[AvatarCategory.TOP])
            }

            // Z-Index 6: HAIR (Peinado)
            state.equippedItems[AvatarCategory.HAIR]?.let { item ->
                RenderLayerImage(item = item, color = state.appliedColors[AvatarCategory.HAIR])
            }

            // Z-Index 7: ACCESSORY (Accesorios extra)
            state.equippedItems[AvatarCategory.ACCESSORY]?.let { item ->
                RenderLayerImage(item = item, color = state.appliedColors[AvatarCategory.ACCESSORY])
            }
        }

        // 3. BARRA DE CONTROLES INFERIORES DE LA PLATAFORMA (Rotación, Pan, Zoom)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xCC090D16))
                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔄", fontSize = 14.sp, modifier = Modifier.clickable { })
            Text("✥", fontSize = 14.sp, color = ElectricCyan, modifier = Modifier.clickable { })
            Text("🔍-", fontSize = 13.sp, color = Color.White, modifier = Modifier.clickable { })
            Text("🔍+", fontSize = 13.sp, color = Color.White, modifier = Modifier.clickable { })
        }
    }
}

/**
 * Renderizado de cada capa individual de prenda con soporte para tinte de color.
 */
@Composable
private fun RenderLayerImage(item: AvatarItem, color: Color?) {
    Image(
        painter = painterResource(id = item.imageResId),
        contentDescription = item.name,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        colorFilter = color?.let { ColorFilter.tint(it, BlendMode.Modulate) }
    )
}

/**
 * SECCIÓN A: Barra de Navegación por Categorías (LazyRow).
 */
@Composable
private fun CategoryNavBar(
    selectedCategory: AvatarCategory,
    onCategorySelect: (AvatarCategory) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(AvatarCategory.entries.toTypedArray()) { category ->
            val isSelected = selectedCategory == category

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0x3300F0FF) else Color(0x11FFFFFF))
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ElectricCyan else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onCategorySelect(category) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(category.icon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = category.label,
                        color = if (isSelected) ElectricCyan else OnSurfaceMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * SECCIÓN B: Catálogo de ítems en grilla de 2 columnas (LazyVerticalGrid).
 */
@Composable
private fun CatalogGrid(
    catalog: List<AvatarItem>,
    state: AvatarState,
    onItemEquipped: (AvatarItem) -> Unit
) {
    val itemsForCategory = remember(catalog, state.selectedCategory) {
        catalog.filter { it.category == state.selectedCategory }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(itemsForCategory) { item ->
            val isEquipped = state.equippedItems[item.category]?.id == item.id

            Box(
                modifier = Modifier
                    .height(110.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isEquipped) Color(0x3300F0FF) else Color(0x1A1E1735)
                    )
                    .border(
                        width = if (isEquipped) 1.5.dp else 1.dp,
                        color = if (isEquipped) ElectricCyan else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onItemEquipped(item) }
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previsualización de la Prenda
                    Image(
                        painter = painterResource(id = item.imageResId),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.name,
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                // Superposición Monetización (Gratis vs GlowCoins/Gemas)
                if (item.currencyType != CurrencyType.FREE) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔒 ", fontSize = 8.sp)
                            val symbol = if (item.currencyType == CurrencyType.GLOWCOINS) "🪙" else "💎"
                            Text(
                                text = "$symbol ${item.cost}",
                                color = if (item.currencyType == CurrencyType.GLOWCOINS) NeonLime else ElectricCyan,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * SECCIÓN C: Selector de Colores (LazyRow).
 */
@Composable
private fun ColorPickerBar(
    appliedColor: Color?,
    onColorSelected: (Color) -> Unit
) {
    val swatches = remember {
        listOf(
            Color(0xFF00F0FF), // 1. Cyan Neón
            Color(0xFF000000), // 2. Negro
            Color(0xFFFF007F), // 3. Magenta
            Color(0xFFFF1493), // 4. Rosa
            Color(0xFFFF3B30), // 5. Rojo
            Color(0xFFFFCC00), // 6. Amarillo
            Color(0xFF00FF00), // 7. Verde
            Color(0xFF9900FF), // 8. Morado
            Color(0xFFFFFFFF)  // 9. Arcoíris / Personalizado
        )
    }

    Column {
        Text("Color de Peinado / Prenda", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(swatches.indices.toList()) { index ->
                val color = swatches[index]
                val isSelected = appliedColor == color || (appliedColor == null && index == 0)
                val isRainbow = index == 8

                Box(
                    modifier = Modifier
                        .size(if (isSelected) 36.dp else 30.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRainbow) {
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFFF0000), Color(0xFFFF8800), Color(0xFFFFFF00),
                                        Color(0xFF00FF00), Color(0xFF00FFFF), Color(0xFF0000FF),
                                        Color(0xFF8800FF), Color(0xFFFF0000)
                                    )
                                )
                            } else {
                                Brush.linearGradient(listOf(color, color))
                            }
                        )
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

/**
 * BARRA INFERIOR (Sticky Bottom Bar con "Por Defecto" y "Guardar Avatar").
 */
@Composable
private fun AvatarBottomBar(
    isSaving: Boolean,
    onReset: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Izquierdo: Por Defecto
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x331E1735)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("↺ ", fontSize = 14.sp)
                Text("POR DEFECTO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Botón Derecho: Guardar Avatar (Cian / Lima Neón)
        Button(
            onClick = onSave,
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Color.Black,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💾 ", fontSize = 14.sp)
                    Text("GUARDAR AVATAR", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}
