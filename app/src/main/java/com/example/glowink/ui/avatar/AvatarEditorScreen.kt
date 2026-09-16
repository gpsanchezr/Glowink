package com.example.glowink.ui.avatar

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.R
import com.example.glowink.data.Avatar3DConfig
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.GlowinkTheme
import com.example.glowink.ui.theme.NeonGradientLimeCyan
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.theme.ObsidianBackground
import com.example.glowink.ui.theme.OnSurfaceMuted
import com.example.glowink.ui.theme.UltravioletPurple
import com.example.glowink.ui.theme.glassmorphic
import com.example.glowink.ui.viewmodel.ChatViewModel
import kotlin.math.roundToInt

/** Categorías del editor de avatares */
enum class AvatarEditorCategory(val label: String, val icon: String) {
    ROSTRO("Rostro", "🗣️"),
    PEINADO("Peinado", "💇"),
    COLOR_CABELLO("Color Pelo", "🎨"),
    OJOS("Ojos", "👁️"),
    ROPA("Ropa", "👕"),
    ZAPATOS("Zapatos", "👟"),
    ACCESORIOS("Accesorios", "👓")
}

data class CustomOption(
    val id: String,
    val name: String,
    val drawableResId: Int? = null,
    val colorHex: Color? = null
)

// OPCIONES FEMENINAS
val femaleClothesOptionsList = listOf(
    CustomOption("ropa_1_fem", "Conjunto Cyber 1", R.drawable.ropa_1_fem),
    CustomOption("ropa_2_fem", "Chaqueta Holo 2", R.drawable.ropa_2_fem),
    CustomOption("ropa_3_fem", "Top Neón 3", R.drawable.ropa_3_fem),
    CustomOption("ropa_4_fem", "Vestido Cyber 4", R.drawable.ropa_4_fem)
)

val femaleHairOptionsList = listOf(
    CustomOption("hair_fem_ondas", "Ondas Neón", R.drawable.hair_fem_ondas),
    CustomOption("hair_fem_trenzas", "Trenzas Glow", R.drawable.hair_fem_trenzas),
    CustomOption("hair_fem_asimetrico", "Asimétrico", R.drawable.hair_fem_asimetrico),
    CustomOption("hair_fem_liso", "Liso Largo", R.drawable.hair_fem_liso),
    CustomOption("hair_fem_coleta", "Coleta Alta", R.drawable.hair_fem_coleta),
    CustomOption("hair_fem_mediano", "Corte Mediano", R.drawable.hair_fem_mediano),
    CustomOption("hair_fem_2coleta", "Doble Coleta", R.drawable.hair_fem_2coleta)
)

val femaleShoesOptionsList = listOf(
    CustomOption("tenis_1_fem", "Tenis Glow 1", R.drawable.tenis_1_fem),
    CustomOption("botas_1_fem", "Botas Cyber 1", R.drawable.botas_fem_1)
)

// OPCIONES MASCULINAS
val maleClothesOptionsList = listOf(
    CustomOption("ropa_1_masc", "Traje Cyber 1", R.drawable.ropa_1_masc),
    CustomOption("ropa_2_masc", "Chaqueta Neón 2", R.drawable.ropa_2_masc),
    CustomOption("ropa_3_masc", "Armor Holo 3", R.drawable.ropa_3_masc)
)

val maleHairOptionsList = listOf(
    CustomOption("hair_masc_1", "Pelo Corto 1", R.drawable.hair_masc_1),
    CustomOption("hair_masc_2", "Mohawk Cyber 2", R.drawable.hair_masc_2),
    CustomOption("hair_masc_3", "Ondas Masc 3", R.drawable.hair_masc_3),
    CustomOption("hair_masc_4", "Largo Silver 4", R.drawable.hair_masc_4)
)

val hairColorSwatches = listOf(
    CustomOption("c_original", "Original PNG", colorHex = Color.White),
    CustomOption("c_magenta", "Rosa Neón", colorHex = Color(0xFFFF007F)),
    CustomOption("c_violeta", "Violeta Glow", colorHex = Color(0xFF7B2CBF)),
    CustomOption("c_cyan", "Cian Eléctrico", colorHex = Color(0xFF00F0FF)),
    CustomOption("c_lima", "Verde Lima", colorHex = Color(0xFF39FF14)),
    CustomOption("c_oro", "Dorado Cyber", colorHex = Color(0xFFFFD700))
)

/**
 * Pantalla Completa "AvatarEditorScreen.kt" - Stateful Composable
 */
@Composable
fun AvatarEditorScreen(
    viewModel: ChatViewModel? = if (LocalInspectionMode.current) null else ChatViewModel(),
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current
    val profileUiState = viewModel?.chatsListUiState?.collectAsState()?.value
    val currentUser = profileUiState?.currentUser
    val isLoading = profileUiState?.isLoading ?: false

    AvatarEditorScreenContent(
        initialConfig = currentUser?.avatarConfig ?: Avatar3DConfig(),
        isLoading = isLoading,
        onSaveAvatar = { config ->
            if (viewModel != null) {
                viewModel.updateAvatarConfig(config)
                Toast.makeText(context, "¡Avatar guardado con éxito! ✨", Toast.LENGTH_SHORT).show()
                onDone()
            } else {
                Toast.makeText(context, "Modo Vista Previa: No se conectó a Firebase", Toast.LENGTH_SHORT).show()
            }
        },
        onUploadPhoto = { uri ->
            viewModel?.uploadProfileImageToStorage(uri)
            onDone()
        }
    )
}

/**
 * Componente puramente visual (Stateless) para el Editor de Avatar.
 */
@OptIn(ExperimentalAnimationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AvatarEditorScreenContent(
    initialConfig: Avatar3DConfig = Avatar3DConfig(),
    isLoading: Boolean = false,
    onSaveAvatar: (Avatar3DConfig) -> Unit = {},
    onUploadPhoto: (android.net.Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUploadPhoto(uri)
        }
    }
    
    var isCustomizing by remember { mutableStateOf(false) }
    var isFemale by remember { mutableStateOf(initialConfig.siluetaBase != "MASCULINO") }

    var selectedHairResId by remember { mutableStateOf<Int?>(hairResourceId(initialConfig.estiloPelo, isFemale)) }
    var selectedClothesResId by remember { mutableStateOf<Int?>(clothingResourceId(initialConfig.estiloRopa, isFemale)) }
    var selectedShoesResId by remember { mutableStateOf<Int?>(if (isFemale) R.drawable.tenis_1_fem else null) }
    var selectedHairColor by remember {
        mutableStateOf(
            try {
                Color(android.graphics.Color.parseColor(initialConfig.colorPeloPrimario))
            } catch (_: Exception) {
                Color.White
            }
        )
    }

    var activeCategory by remember { mutableStateOf(AvatarEditorCategory.PEINADO) }
    val categoriesScrollState = rememberScrollState()

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(ObsidianBackground, Color(0xFF140D2B), ObsidianBackground)))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Crear tu Personaje ✨", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(
                    text = if (!isCustomizing) "Avatar vestido por defecto • Selecciona prendas para editar"
                    else "Modo Maniquí Activo en Capas ⚡",
                    fontSize = 12.sp,
                    color = if (isCustomizing) NeonLime else ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isFemale) Brush.horizontalGradient(listOf(UltravioletPurple, ElectricCyan)) else Brush.horizontalGradient(listOf(Color(0x18FFFFFF), Color(0x18FFFFFF))))
                        .clickable {
                            if (!isFemale) {
                                isFemale = true
                                selectedClothesResId = R.drawable.ropa_1_fem
                                selectedHairResId = R.drawable.hair_fem_ondas
                                selectedShoesResId = R.drawable.tenis_1_fem
                            }
                        }
                ) {
                    Text("👩 Femenino", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isFemale) Color.Black else Color.White)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!isFemale) Brush.horizontalGradient(listOf(UltravioletPurple, ElectricCyan)) else Brush.horizontalGradient(listOf(Color(0x18FFFFFF), Color(0x18FFFFFF))))
                        .clickable {
                            if (isFemale) {
                                isFemale = false
                                selectedClothesResId = R.drawable.ropa_1_masc
                                selectedHairResId = R.drawable.hair_masc_1
                                selectedShoesResId = null
                            }
                        }
                ) {
                    Text("👨 Masculino", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (!isFemale) Color.Black else Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón para usar foto de perfil real en vez de avatar
            Button(
                onClick = { 
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(ElectricCyan.copy(alpha = 0.3f), UltravioletPurple.copy(alpha = 0.3f))))
                    .border(1.dp, ElectricCyan, RoundedCornerShape(14.dp))
            ) {
                Text("🖼️ USAR MI PROPIA FOTO", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (!isCustomizing) Brush.horizontalGradient(listOf(ElectricCyan, NeonLime)) else Brush.horizontalGradient(listOf(Color(0x18FFFFFF), Color(0x18FFFFFF))))
                        .clickable { isCustomizing = false }
                ) {
                    Text(
                        text = if (isFemale) "👗 Avatar Vestido" else "👕 Avatar Vestido",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isCustomizing) Color.Black else Color.White
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isCustomizing) Brush.horizontalGradient(listOf(UltravioletPurple, ElectricCyan)) else Brush.horizontalGradient(listOf(Color(0x18FFFFFF), Color(0x18FFFFFF))))
                        .clickable { isCustomizing = true }
                ) {
                    Text(
                        text = "🧍 Maniquí en Capas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCustomizing) Color.Black else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1.25f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x22120B24))
                        .border(1.5.dp, Brush.linearGradient(listOf(ElectricCyan.copy(alpha = 0.6f), UltravioletPurple.copy(alpha = 0.6f))), RoundedCornerShape(24.dp))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(ElectricCyan.copy(alpha = 0.25f), UltravioletPurple.copy(alpha = 0.15f), Color.Transparent)))
                    )

                    AnimatedContent(
                        targetState = isCustomizing,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "VisorMode"
                    ) { customizingActive ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth(0.88f).fillMaxHeight(0.95f)
                        ) {
                            if (!customizingActive) {
                                val defaultAvatarRes = if (isFemale) R.drawable.avatar_fem_base else R.drawable.avatar_masc_base
                                Image(
                                    painter = painterResource(id = defaultAvatarRes),
                                    contentDescription = "Avatar Vestido Por Defecto",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val mannequinRes = if (isFemale) R.drawable.avatar_base_fem_maniqui else R.drawable.avatar_base_masc_maniqui

                                selectedHairResId?.let { hair ->
                                    Image(
                                        painter = painterResource(id = hair),
                                        contentDescription = "Cabello seleccionado",
                                        contentScale = ContentScale.Fit,
                                        colorFilter = if (selectedHairColor != Color.White) {
                                            ColorFilter.tint(selectedHairColor, blendMode = BlendMode.Modulate)
                                        } else null,
                                        modifier = Modifier
                                            .fillMaxWidth(hairScale(hair))
                                            .fillMaxHeight(hairScale(hair))
                                            .align(Alignment.TopCenter)
                                    )
                                }

                                Image(
                                    painter = painterResource(id = mannequinRes),
                                    contentDescription = "Maniquí base",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )

                                selectedClothesResId?.let { clothes ->
                                    Image(
                                        painter = painterResource(id = clothes),
                                        contentDescription = "Ropa seleccionada",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                selectedShoesResId?.let { shoes ->
                                    Image(
                                        painter = painterResource(id = shoes),
                                        contentDescription = "Calzado seleccionado",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .glassmorphic(shape = RoundedCornerShape(24.dp), borderWidth = 1.dp, borderColor = Color(0x33FFFFFF))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(categoriesScrollState),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AvatarEditorCategory.entries.forEach { cat ->
                            val isSelected = activeCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(if (isSelected) Modifier.background(Brush.linearGradient(listOf(UltravioletPurple, ElectricCyan))) else Modifier.background(Color(0x18FFFFFF)))
                                    .clickable { activeCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "${cat.icon} ${cat.label}", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.Black else Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val currentHairList = if (isFemale) femaleHairOptionsList else maleHairOptionsList
                        val currentClothesList = if (isFemale) femaleClothesOptionsList else maleClothesOptionsList

                        when (activeCategory) {
                            AvatarEditorCategory.PEINADO -> {
                                GridOptionSelector(
                                    options = currentHairList,
                                    selectedResId = selectedHairResId,
                                    onSelectOption = { resId ->
                                        selectedHairResId = resId
                                        isCustomizing = true
                                    }
                                )
                            }
                            AvatarEditorCategory.ROPA -> {
                                GridOptionSelector(
                                    options = currentClothesList,
                                    selectedResId = selectedClothesResId,
                                    onSelectOption = { resId ->
                                        selectedClothesResId = resId
                                        isCustomizing = true
                                    }
                                )
                            }
                            AvatarEditorCategory.ZAPATOS -> {
                                if (isFemale) {
                                    GridOptionSelector(
                                        options = femaleShoesOptionsList,
                                        selectedResId = selectedShoesResId,
                                        onSelectOption = { resId ->
                                            selectedShoesResId = resId
                                            isCustomizing = true
                                        }
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text("👟 Calzado Masculino Próximamente", fontSize = 11.sp, color = OnSurfaceMuted, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                            AvatarEditorCategory.COLOR_CABELLO -> {
                                ColorSwatchGrid(
                                    swatches = hairColorSwatches,
                                    selectedColor = selectedHairColor,
                                    onSelectColor = { color ->
                                        selectedHairColor = color
                                        isCustomizing = true
                                    }
                                )
                            }
                            else -> {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text("✨ Opciones en Desarrollo ✨", fontSize = 11.5.sp, color = OnSurfaceMuted)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { isCustomizing = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Por Defecto", color = Color.White, fontSize = 13.5.sp)
                }

                Button(
                    onClick = {
                        val config = Avatar3DConfig(
                            siluetaBase = if (isFemale) "FEMENINO" else "MASCULINO",
                            estiloPelo = hairStyleId(selectedHairResId, isFemale),
                            colorPeloPrimario = colorToHex(selectedHairColor),
                            estiloRopa = clothingStyleId(selectedClothesResId, isFemale),
                            colorRopaPrimario = if (isFemale) "#1E1735" else "#1E1735",
                            accesorioId = "NINGUNO"
                        )
                        onSaveAvatar(config)
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1.3f).height(48.dp).clip(RoundedCornerShape(20.dp)).background(NeonGradientLimeCyan)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("✓ Guardar Avatar", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

private fun hairResourceId(style: String, isFemale: Boolean): Int = when (style) {
    "hair_fem_trenzas" -> R.drawable.hair_fem_trenzas
    "hair_fem_asimetrico" -> R.drawable.hair_fem_asimetrico
    "hair_fem_liso" -> R.drawable.hair_fem_liso
    "hair_fem_coleta" -> R.drawable.hair_fem_coleta
    "hair_fem_mediano" -> R.drawable.hair_fem_mediano
    "hair_fem_2coleta" -> R.drawable.hair_fem_2coleta
    "hair_masc_2" -> R.drawable.hair_masc_2
    "hair_masc_3" -> R.drawable.hair_masc_3
    "hair_masc_4" -> R.drawable.hair_masc_4
    "hair_masc_1" -> R.drawable.hair_masc_1
    else -> if (isFemale) R.drawable.hair_fem_ondas else R.drawable.hair_masc_1
}

private fun clothingResourceId(style: String, isFemale: Boolean): Int = when (style) {
    "ropa_2_fem" -> R.drawable.ropa_2_fem
    "ropa_3_fem" -> R.drawable.ropa_3_fem
    "ropa_4_fem" -> R.drawable.ropa_4_fem
    "ropa_1_masc" -> R.drawable.ropa_1_masc
    "ropa_2_masc" -> R.drawable.ropa_2_masc
    "ropa_3_masc" -> R.drawable.ropa_3_masc
    else -> if (isFemale) R.drawable.ropa_1_fem else R.drawable.ropa_1_masc
}

private fun hairScale(resId: Int): Float =
    if (resId == R.drawable.hair_fem_trenzas || resId == R.drawable.hair_fem_asimetrico) 1f else 0.60f

private fun hairStyleId(resId: Int?, isFemale: Boolean): String = when (resId) {
    R.drawable.hair_fem_trenzas -> "hair_fem_trenzas"
    R.drawable.hair_fem_asimetrico -> "hair_fem_asimetrico"
    R.drawable.hair_fem_liso -> "hair_fem_liso"
    R.drawable.hair_fem_coleta -> "hair_fem_coleta"
    R.drawable.hair_fem_mediano -> "hair_fem_mediano"
    R.drawable.hair_fem_2coleta -> "hair_fem_2coleta"
    R.drawable.hair_masc_2 -> "hair_masc_2"
    R.drawable.hair_masc_3 -> "hair_masc_3"
    R.drawable.hair_masc_4 -> "hair_masc_4"
    R.drawable.hair_masc_1 -> "hair_masc_1"
    else -> if (isFemale) "hair_fem_ondas" else "hair_masc_1"
}

private fun clothingStyleId(resId: Int?, isFemale: Boolean): String = when (resId) {
    R.drawable.ropa_2_fem -> "ropa_2_fem"
    R.drawable.ropa_3_fem -> "ropa_3_fem"
    R.drawable.ropa_4_fem -> "ropa_4_fem"
    R.drawable.ropa_1_masc -> "ropa_1_masc"
    R.drawable.ropa_2_masc -> "ropa_2_masc"
    R.drawable.ropa_3_masc -> "ropa_3_masc"
    else -> if (isFemale) "ropa_1_fem" else "ropa_1_masc"
}

private fun colorToHex(color: Color): String {
    val alpha = (color.alpha * 255f).roundToInt().coerceIn(0, 255)
    val red = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val green = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val blue = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    return "#%02X%02X%02X%02X".format(alpha, red, green, blue)
}

/** Grilla interactiva para prendas y peinados */
@Composable
private fun GridOptionSelector(
    options: List<CustomOption>,
    selectedResId: Int?,
    onSelectOption: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(options) { opt ->
            val isSelected = selectedResId == opt.drawableResId
            opt.drawableResId?.let { resId ->
                Box(
                    modifier = Modifier
                        .height(82.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) NeonLime.copy(alpha = 0.22f) else Color(0x18FFFFFF))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NeonLime else Color(0x2AFFFFFF),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectOption(resId) }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = opt.name,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = opt.name,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NeonLime else Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/** Grilla para paletas de color de cabello */
@Composable
private fun ColorSwatchGrid(
    swatches: List<CustomOption>,
    selectedColor: Color,
    onSelectColor: (Color) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(swatches) { opt ->
            opt.colorHex?.let { color ->
                val isSelected = selectedColor == color
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectColor(color) }
                        .padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(if (isSelected) 3.dp else 1.dp, if (isSelected) NeonLime else Color(0x40FFFFFF), CircleShape)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(opt.name, fontSize = 9.5.sp, color = if (isSelected) NeonLime else OnSurfaceMuted, maxLines = 1)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AvatarEditorScreenPreview() {
    GlowinkTheme {
        AvatarEditorScreenContent(
            initialConfig = Avatar3DConfig()
        )
    }
}
