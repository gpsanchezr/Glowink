package com.example.glowink.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modificador base de Glassmorphism (NITIDO) con soporte para Color o Brush.
 *
 * `backgroundBrush` permite reemplazar el degradado blanco translúcido genérico por uno
 * propio (por ejemplo, para diferenciar visualmente las burbujas de chat propias de las
 * ajenas). Si se deja en `null`, se usa el degradado blanco genérico de siempre.
 */
fun Modifier.glassmorphic(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color(0x6600F0FF),
    borderBrush: Brush? = null,
    backgroundBrush: Brush? = null
): Modifier = this
    .clip(shape)
    .background(
        backgroundBrush ?: Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    )
    .then(
        if (borderBrush != null) {
            Modifier.border(borderWidth, borderBrush, shape)
        } else {
            Modifier.border(borderWidth, borderColor, shape)
        }
    )

/**
 * Modificadores para Chat.
 */
fun Modifier.glassmorphicChatBubbleSelf(
    shape: Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
): Modifier = this.glassmorphic(shape = shape, borderColor = Color(0xFF39FF14), backgroundBrush = GlassBubbleSelfGradient)

fun Modifier.glassmorphicChatBubbleOther(
    shape: Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
): Modifier = this.glassmorphic(shape = shape, borderColor = Color(0xFF00F0FF), backgroundBrush = GlassBubbleOtherGradient)

/**
 * Contenedor Glassmorphic restaurado con soporte para todos los parámetros
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color(0x6600F0FF),
    borderBrush: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glassmorphic(
            shape = shape, 
            borderWidth = borderWidth, 
            borderColor = borderColor,
            borderBrush = borderBrush
        ),
        content = content
    )
}

/**
 * Modificador personalizado para feedback táctil de escala (Microanimación Bounce).
 * Aplica un Modifier.scale(if (isPressed) 0.96f else 1f) con animación fluida.
 */
@Composable
fun Modifier.bounceClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "bounceScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}
