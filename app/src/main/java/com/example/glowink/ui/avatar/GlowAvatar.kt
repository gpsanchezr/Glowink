package com.example.glowink.ui.avatar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.glowink.R
import com.example.glowink.data.Avatar3DConfig
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.NeonLime

/**
 * Componente de Avatar 100% basado en el "Avatar Glow" del usuario (MVP Strategy).
 *
 * Utiliza [BoxWithConstraints] para fijar proporciones relativas y evitar deformaciones.
 * Si [isHeadshot] es `true`, escala y desplaza el avatar para mostrar SOLO de los hombros hacia arriba (busto).
 */
@Composable
fun GlowAvatar(
    config: Avatar3DConfig,
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
    size: Dp = 100.dp,
    animate: Boolean = true,
    isHeadshot: Boolean = true
) {
    val skinColor = parseColorSafely(config.colorPiel, Color(0xFFF4C2A1))

    val infiniteTransition = rememberInfiniteTransition(label = "GlowAvatarPulse")
    val alphaAnim = if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0.88f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "AvatarAlpha"
        ).value
    } else {
        1f
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .alpha(alphaAnim)
            .background(
                Brush.radialGradient(
                    listOf(skinColor.copy(alpha = 0.35f), Color(0xFF140D2A), Color(0xFF070514))
                )
            )
            .border(
                2.dp,
                Brush.sweepGradient(listOf(ElectricCyan, NeonLime, ElectricCyan)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val h = this.maxHeight
            val scaleFactor = if (isHeadshot) 2.2f else 1.0f
            val offsetY = if (isHeadshot) h * 0.36f else 0.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scaleFactor)
                    .offset(y = offsetY),
                contentAlignment = Alignment.Center
            ) {
                // Capa 1: Base Maniquí
                Image(
                    painter = painterResource(
                        id = if (config.siluetaBase.equals("MASCULINO", ignoreCase = true))
                            R.drawable.avatar_base_masc_maniqui
                        else
                            R.drawable.avatar_base_fem_maniqui
                    ),
                    contentDescription = "Base del avatar",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Capa 2: Ropa
                clothingResource(config.estiloRopa)?.let { resId ->
                    Image(
                        painter = painterResource(resId),
                        contentDescription = "Ropa del avatar",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Capa 3: Cabello
                hairResource(config.estiloPelo)?.let { resId ->
                    Image(
                        painter = painterResource(resId),
                        contentDescription = "Cabello del avatar",
                        contentScale = ContentScale.Fit,
                        colorFilter = if (config.colorPeloPrimario.isNotBlank() &&
                            !config.colorPeloPrimario.equals("#FFFFFF", ignoreCase = true)
                        ) {
                            ColorFilter.tint(
                                parseColorSafely(config.colorPeloPrimario, Color.White),
                                blendMode = BlendMode.Modulate
                            )
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Contenedor con marco neón y soporte para headshot.
 */
@Composable
fun GlowAvatarFrame(
    config: Avatar3DConfig,
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
    size: Dp = 100.dp,
    ringBrush: Brush = Brush.sweepGradient(listOf(ElectricCyan, NeonLime, ElectricCyan)),
    ringWidth: Dp = 3.dp,
    backgroundColor: Color = Color(0xFF1E1735),
    isHeadshot: Boolean = true
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size + (ringWidth * 2))
            .clip(CircleShape)
            .background(ringBrush)
            .padding(ringWidth)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        GlowAvatar(
            config = config,
            profileImageUrl = profileImageUrl,
            size = size,
            isHeadshot = isHeadshot
        )
    }
}

private fun hairResource(style: String): Int? = when (style.lowercase()) {
    "purple_bob", "ondas_neon", "hair_fem_ondas" -> R.drawable.hair_fem_ondas
    "hair_fem_trenzas" -> R.drawable.hair_fem_trenzas
    "hair_fem_asimetrico" -> R.drawable.hair_fem_asimetrico
    "hair_fem_liso" -> R.drawable.hair_fem_liso
    "hair_fem_coleta" -> R.drawable.hair_fem_coleta
    "hair_fem_mediano" -> R.drawable.hair_fem_mediano
    "hair_fem_2coleta" -> R.drawable.hair_fem_2coleta
    "hair_masc_1" -> R.drawable.hair_masc_1
    "hair_masc_2" -> R.drawable.hair_masc_2
    "hair_masc_3" -> R.drawable.hair_masc_3
    "hair_masc_4" -> R.drawable.hair_masc_4
    else -> if (style.contains("MASC", true)) R.drawable.hair_masc_1 else R.drawable.hair_fem_ondas
}

private fun clothingResource(style: String): Int? = when (style.lowercase()) {
    "hoodie_neon", "ropa_1_fem" -> R.drawable.ropa_1_fem
    "ropa_2_fem" -> R.drawable.ropa_2_fem
    "ropa_3_fem" -> R.drawable.ropa_3_fem
    "ropa_4_fem" -> R.drawable.ropa_4_fem
    "ropa_1_masc" -> R.drawable.ropa_1_masc
    "ropa_2_masc" -> R.drawable.ropa_2_masc
    "ropa_3_masc" -> R.drawable.ropa_3_masc
    else -> if (style.contains("MASC", true)) R.drawable.ropa_1_masc else R.drawable.ropa_1_fem
}

private fun parseColorSafely(hexString: String, default: Color): Color {
    return try {
        if (hexString.startsWith("#")) {
            Color(hexString.toColorInt())
        } else {
            default
        }
    } catch (_: Exception) {
        default
    }
}
