package com.example.glowink.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowink.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AvatarCategory(val label: String, val icon: String) {
    FACE("Rostro", "👁️"),
    HAIR("Peinado", "💇"),
    BODY("Cuerpo", "🧍"),
    TOP("Ropa", "👕"),
    BOTTOM("Pantalones", "👖"),
    SHOES("Calzado", "👟"),
    ACCESSORY("Accesorios", "👓")
}

enum class CurrencyType {
    FREE, GLOWCOINS, GEMS, POINTS
}

data class AvatarItem(
    val id: String,
    val category: AvatarCategory,
    val name: String,
    val imageResId: Int,
    val currencyType: CurrencyType = CurrencyType.FREE,
    val cost: Int = 0
)

data class AvatarState(
    val selectedCategory: AvatarCategory = AvatarCategory.HAIR,
    val equippedItems: Map<AvatarCategory, AvatarItem> = emptyMap(),
    val appliedColors: Map<AvatarCategory, Color> = emptyMap(),
    val isSaving: Boolean = false,
    val gender: String = "FEMENINO",
    val viewMode: String = "AVATAR_COMPLETO"
)

/**
 * ViewModel especializado para la creación y personalización de Avatares en Glowink (MVVM + UDF).
 */
class AvatarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AvatarState())
    val uiState: StateFlow<AvatarState> = _uiState.asStateFlow()

    // Catálogo estático de prendas y estilos (Femenino + Masculino)
    val catalog: List<AvatarItem> = listOf(
        // --- PEINADOS (HAIR) ---
        AvatarItem("hair_fem_ondas", AvatarCategory.HAIR, "Ondas Cian-Rosa", R.drawable.hair_fem_ondas, CurrencyType.FREE),
        AvatarItem("hair_fem_trenzas", AvatarCategory.HAIR, "Trenzas Tribales", R.drawable.hair_fem_trenzas, CurrencyType.FREE),
        AvatarItem("hair_fem_asimetrico", AvatarCategory.HAIR, "Corte Asimétrico", R.drawable.hair_fem_asimetrico, CurrencyType.FREE),
        AvatarItem("hair_fem_liso", AvatarCategory.HAIR, "Liso Largo", R.drawable.hair_fem_liso, CurrencyType.GLOWCOINS, 100),
        AvatarItem("hair_fem_coleta", AvatarCategory.HAIR, "Coleta Alta", R.drawable.hair_fem_coleta, CurrencyType.GLOWCOINS, 150),
        AvatarItem("hair_fem_mediano", AvatarCategory.HAIR, "Corte Bob", R.drawable.hair_fem_mediano, CurrencyType.FREE),
        AvatarItem("hair_fem_2coleta", AvatarCategory.HAIR, "Ondas Dobles", R.drawable.hair_fem_2coleta, CurrencyType.GEMS, 8),
        AvatarItem("hair_masc_1", AvatarCategory.HAIR, "Mohawk Estilizado", R.drawable.hair_masc_1, CurrencyType.GLOWCOINS, 200),
        AvatarItem("hair_masc_2", AvatarCategory.HAIR, "Rizos Sueltos", R.drawable.hair_masc_2, CurrencyType.FREE),
        AvatarItem("hair_masc_3", AvatarCategory.HAIR, "Rapado Cyber", R.drawable.hair_masc_3, CurrencyType.GLOWCOINS, 120),
        AvatarItem("hair_masc_4", AvatarCategory.HAIR, "Corte Táctico", R.drawable.hair_masc_4, CurrencyType.GEMS, 5),

        // --- ROPA SUPERIOR (TOP) ---
        AvatarItem("ropa_1_fem", AvatarCategory.TOP, "Conjunto Cyber 1", R.drawable.ropa_1_fem, CurrencyType.FREE),
        AvatarItem("ropa_2_fem", AvatarCategory.TOP, "Chaqueta Holo 2", R.drawable.ropa_2_fem, CurrencyType.GLOWCOINS, 120),
        AvatarItem("ropa_3_fem", AvatarCategory.TOP, "Top Neón 3", R.drawable.ropa_3_fem, CurrencyType.FREE),
        AvatarItem("ropa_4_fem", AvatarCategory.TOP, "Armadura Ciberpunk 4", R.drawable.ropa_4_fem, CurrencyType.GEMS, 10),
        AvatarItem("ropa_1_masc", AvatarCategory.TOP, "Chaqueta Titan 1", R.drawable.ropa_1_masc, CurrencyType.FREE),
        AvatarItem("ropa_2_masc", AvatarCategory.TOP, "Chaleco Neón 2", R.drawable.ropa_2_masc, CurrencyType.GLOWCOINS, 150),
        AvatarItem("ropa_3_masc", AvatarCategory.TOP, "Overol Cyber 3", R.drawable.ropa_3_masc, CurrencyType.FREE),

        // --- PANTALONES (BOTTOM) ---
        AvatarItem("bottom_1", AvatarCategory.BOTTOM, "Pantalón Ajustado", R.drawable.ropa_1_fem, CurrencyType.FREE),
        AvatarItem("bottom_2", AvatarCategory.BOTTOM, "Falda Holo Neón", R.drawable.ropa_3_fem, CurrencyType.GLOWCOINS, 80),
        AvatarItem("bottom_3", AvatarCategory.BOTTOM, "Pantalón Táctico", R.drawable.ropa_2_masc, CurrencyType.GEMS, 5),

        // --- CALZADO (SHOES) ---
        AvatarItem("shoes_1", AvatarCategory.SHOES, "Botas Cyber", R.drawable.botas_fem_1, CurrencyType.FREE),
        AvatarItem("shoes_2", AvatarCategory.SHOES, "Tenis Glow", R.drawable.tenis_1_fem, CurrencyType.GLOWCOINS, 90),

        // --- CUERPO (BODY) ---
        AvatarItem("body_fem_base", AvatarCategory.BODY, "Cuerpo Femenino", R.drawable.avatar_fem_base, CurrencyType.FREE),
        AvatarItem("body_masc_base", AvatarCategory.BODY, "Cuerpo Masculino", R.drawable.avatar_masc_base, CurrencyType.FREE),
        AvatarItem("body_fem_maniqui", AvatarCategory.BODY, "Maniquí Cyber", R.drawable.avatar_base_fem_maniqui, CurrencyType.GLOWCOINS, 150),
        AvatarItem("body_masc_maniqui", AvatarCategory.BODY, "Maniquí Titan", R.drawable.avatar_base_masc_maniqui, CurrencyType.GEMS, 5),

        // --- ROSTRO (FACE) ---
        AvatarItem("face_1", AvatarCategory.FACE, "Rostro Neón", R.drawable.avatar_fem_base, CurrencyType.FREE),
        AvatarItem("face_2", AvatarCategory.FACE, "Maquillaje Cyber", R.drawable.avatar_fem_base, CurrencyType.GLOWCOINS, 50),

        // --- ACCESORIOS (ACCESSORY) ---
        AvatarItem("acc_1", AvatarCategory.ACCESSORY, "Visor Holo", R.drawable.ic_launcher_glowink, CurrencyType.FREE),
        AvatarItem("acc_2", AvatarCategory.ACCESSORY, "Audífonos Glow", R.drawable.ic_glowink_logo, CurrencyType.GLOWCOINS, 150)
    )

    init {
        setupDefaultEquipped()
    }

    private fun setupDefaultEquipped() {
        val defaultItems = mapOf(
            AvatarCategory.BODY to catalog.first { it.id == "body_fem_base" },
            AvatarCategory.FACE to catalog.first { it.id == "face_1" },
            AvatarCategory.HAIR to catalog.first { it.id == "hair_fem_ondas" },
            AvatarCategory.TOP to catalog.first { it.id == "ropa_1_fem" },
            AvatarCategory.BOTTOM to catalog.first { it.id == "bottom_1" },
            AvatarCategory.SHOES to catalog.first { it.id == "shoes_1" }
        )
        _uiState.update { it.copy(equippedItems = defaultItems) }
    }

    fun onCategorySelected(category: AvatarCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onItemEquipped(item: AvatarItem) {
        _uiState.update { state ->
            val updatedEquipped = state.equippedItems.toMutableMap()
            updatedEquipped[item.category] = item
            state.copy(equippedItems = updatedEquipped)
        }
    }

    fun onColorSelected(color: Color) {
        _uiState.update { state ->
            val currentCat = state.selectedCategory
            val updatedColors = state.appliedColors.toMutableMap()
            updatedColors[currentCat] = color
            state.copy(appliedColors = updatedColors)
        }
    }

    fun onResetToDefault() {
        _uiState.update {
            AvatarState(selectedCategory = AvatarCategory.HAIR)
        }
        setupDefaultEquipped()
    }

    fun onGenderSelected(gender: String) {
        _uiState.update { state ->
            val defaultBodyId = if (gender == "MASCULINO") "body_masc_base" else "body_fem_base"
            val defaultTopId = if (gender == "MASCULINO") "ropa_1_masc" else "ropa_1_fem"
            val defaultHairId = if (gender == "MASCULINO") "hair_masc_1" else "hair_fem_ondas"

            val updatedEquipped = state.equippedItems.toMutableMap()
            catalog.find { it.id == defaultBodyId }?.let { updatedEquipped[AvatarCategory.BODY] = it }
            catalog.find { it.id == defaultTopId }?.let { updatedEquipped[AvatarCategory.TOP] = it }
            catalog.find { it.id == defaultHairId }?.let { updatedEquipped[AvatarCategory.HAIR] = it }

            state.copy(gender = gender, equippedItems = updatedEquipped)
        }
    }

    fun onViewModeSelected(viewMode: String) {
        _uiState.update { it.copy(viewMode = viewMode) }
    }

    fun onSaveAvatar(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            delay(2000L) // Simulación de guardado en Firebase/Firestore
            _uiState.update { it.copy(isSaving = false) }
            onSaved()
        }
    }
}
