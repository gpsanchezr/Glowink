package com.example.glowink.ui.avatar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.glowink.data.Avatar3DConfig
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.NeonLime

/**
 * Renderizador de Avatar 100% Vectorial y Paramétrico para Glowink.
 *
 * Elimina por completo las imágenes estáticas (PNGs) y construye dinámicamente
 * la anatomía, facciones, ropa, peinado y accesorios mediante geometría matemática y
 * primitivas de dibujado en Compose Canvas (DrawScope).
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
    val hairPrimary = parseColorSafely(config.colorPeloPrimario, Color(0xFF9D4EDD))
    val hairSecondary = parseColorSafely(config.colorPeloSecundario, Color(0xFF00F0FF))
    val eyeColor = parseColorSafely(config.colorOjos, Color(0xFF39FF14))
    val clothingPrimary = parseColorSafely(config.colorRopaPrimario, Color(0xFF1E1735))
    val clothingSecondary = parseColorSafely(config.colorRopaSecundario, Color(0xFF00F0FF))

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
                    listOf(
                        skinColor.copy(alpha = 0.35f),
                        clothingPrimary.copy(alpha = 0.6f),
                        Color(0xFF070514)
                    )
                )
            )
            .border(
                2.dp,
                Brush.sweepGradient(listOf(ElectricCyan, NeonLime, hairPrimary, ElectricCyan)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            val transformScale = if (isHeadshot) 1.7f else 1.0f
            val transformOffsetY = if (isHeadshot) h * 0.16f else 0f

            withTransform({
                scale(transformScale, transformScale, pivot = Offset(w * 0.5f, h * 0.42f))
                translate(left = 0f, top = transformOffsetY)
            }) {
                val isFeminine = config.siluetaBase.equals("FEMENINO", ignoreCase = true)

                // CAPA 0: Aura Neón (si está activa)
                if (config.accesorioId.equals("AURA_NEON", ignoreCase = true)) {
                    drawAuraEffect(w, h, hairSecondary)
                }

                // CAPA 1: Base / Cuerpo (Cuello, Hombros y Torso)
                drawBodyAndNeck(w, h, isFeminine, skinColor, clothingPrimary, clothingSecondary)

                // CAPA 2: Rostro y Cabeza (Estructura de la cabeza, Orejas y Facciones)
                drawHeadAndEars(w, h, isFeminine, skinColor)
                drawFaceFeatures(w, h, eyeColor, hairPrimary, config.estiloOjos)

                // CAPA 3: Ropa y Detalle de Vestimenta
                drawClothingDetails(w, h, config.estiloRopa, clothingSecondary)

                // CAPA 4: Cabello (Anclado a la parte superior de la cabeza)
                drawHair(w, h, config.estiloPelo, hairPrimary, hairSecondary)

                // CAPA 5: Accesorios (Gafas, Visor VR, Gorra, Audífonos)
                drawAccessories(w, h, config.accesorioId, clothingSecondary)
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

// ============================================================================
// FUNCIONES DE DIBUJADO VECTORIAL MATEMÁTICO (DRAW SCOPE)
// ============================================================================

private fun DrawScope.drawAuraEffect(w: Float, h: Float, auraColor: Color) {
    val center = Offset(w * 0.5f, h * 0.42f)
    drawCircle(
        color = auraColor.copy(alpha = 0.35f),
        radius = w * 0.48f,
        center = center
    )
    drawCircle(
        color = auraColor.copy(alpha = 0.2f),
        radius = w * 0.56f,
        center = center,
        style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
    )
}

private fun DrawScope.drawBodyAndNeck(
    w: Float,
    h: Float,
    isFeminine: Boolean,
    skinColor: Color,
    clothingPrimary: Color,
    clothingSecondary: Color
) {
    // Cuello
    val neckWidth = w * (if (isFeminine) 0.16f else 0.20f)
    val neckTop = h * 0.50f
    val neckBottom = h * 0.65f

    drawRoundRect(
        color = skinColor,
        topLeft = Offset(w * 0.5f - neckWidth / 2f, neckTop),
        size = Size(neckWidth, neckBottom - neckTop),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Sombra sutil bajo la barbilla
    drawArc(
        color = Color.Black.copy(alpha = 0.15f),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.5f - neckWidth / 2f, neckTop),
        size = Size(neckWidth, neckWidth * 0.5f)
    )

    // Torso / Hombros
    val shoulderLeft = w * (if (isFeminine) 0.14f else 0.08f)
    val shoulderRight = w * (if (isFeminine) 0.86f else 0.92f)
    val shoulderTop = h * 0.64f

    val torsoPath = Path().apply {
        moveTo(w * 0.5f - neckWidth / 2f - 4f, neckBottom)
        quadraticTo(w * 0.30f, shoulderTop, shoulderLeft, shoulderTop + h * 0.08f)
        lineTo(shoulderLeft, h)
        lineTo(shoulderRight, h)
        lineTo(shoulderRight, shoulderTop + h * 0.08f)
        quadraticTo(w * 0.70f, shoulderTop, w * 0.5f + neckWidth / 2f + 4f, neckBottom)
        close()
    }

    drawPath(path = torsoPath, color = clothingPrimary)

    // Silueta de escote/cuello de la prenda base
    val collarPath = Path().apply {
        moveTo(w * 0.5f - neckWidth / 2f - 2f, neckBottom)
        quadraticTo(w * 0.5f, neckBottom + h * 0.06f, w * 0.5f + neckWidth / 2f + 2f, neckBottom)
    }
    drawPath(path = collarPath, color = clothingSecondary, style = Stroke(width = 3.5f))
}

private fun DrawScope.drawHeadAndEars(
    w: Float,
    h: Float,
    isFeminine: Boolean,
    skinColor: Color
) {
    val headW = w * (if (isFeminine) 0.38f else 0.42f)
    val headH = h * (if (isFeminine) 0.46f else 0.48f)
    val headTop = h * 0.16f
    val headLeft = w * 0.5f - headW / 2f

    // Orejas
    val earRadiusX = w * 0.035f
    val earRadiusY = h * 0.045f
    val earY = h * 0.38f

    drawOval(
        color = skinColor,
        topLeft = Offset(headLeft - earRadiusX, earY - earRadiusY),
        size = Size(earRadiusX * 2.2f, earRadiusY * 2f)
    )
    drawOval(
        color = skinColor,
        topLeft = Offset(headLeft + headW - earRadiusX * 1.2f, earY - earRadiusY),
        size = Size(earRadiusX * 2.2f, earRadiusY * 2f)
    )

    // Base de la Cabeza (Forma OVAL suave con mentón)
    val headPath = Path().apply {
        moveTo(w * 0.5f - headW / 2f, headTop + headH * 0.4f)
        quadraticTo(w * 0.5f - headW / 2f, headTop, w * 0.5f, headTop)
        quadraticTo(w * 0.5f + headW / 2f, headTop, w * 0.5f + headW / 2f, headTop + headH * 0.4f)
        quadraticTo(w * 0.5f + headW / 2f, headTop + headH * 0.85f, w * 0.5f, headTop + headH)
        quadraticTo(w * 0.5f - headW / 2f, headTop + headH * 0.85f, w * 0.5f - headW / 2f, headTop + headH * 0.4f)
        close()
    }

    drawPath(path = headPath, color = skinColor)

    // Sombra lateral ligera para relieve 2D
    drawPath(
        path = headPath,
        color = Color.Black.copy(alpha = 0.06f),
        style = Stroke(width = 6f)
    )
}

private fun DrawScope.drawFaceFeatures(
    w: Float,
    h: Float,
    eyeColor: Color,
    hairColor: Color,
    eyeStyle: String
) {
    val eyeY = h * 0.36f
    val eyeSpacing = w * 0.11f
    val leftEyeX = w * 0.5f - eyeSpacing
    val rightEyeX = w * 0.5f + eyeSpacing

    val eyeW = w * 0.085f
    val eyeH = h * 0.055f

    // DIBUJO DE OJOS SEGÚN ESTILO
    listOf(leftEyeX, rightEyeX).forEach { eyeX ->
        // Esclerótica (blanco)
        drawOval(
            color = Color.White,
            topLeft = Offset(eyeX - eyeW / 2f, eyeY - eyeH / 2f),
            size = Size(eyeW, eyeH)
        )

        // Iris Neón
        val irisRadius = eyeW * 0.35f
        drawCircle(
            color = eyeColor,
            radius = irisRadius,
            center = Offset(eyeX, eyeY)
        )

        // Pupila Negra
        drawCircle(
            color = Color(0xFF100A20),
            radius = irisRadius * 0.5f,
            center = Offset(eyeX, eyeY)
        )

        // Brillo blanco (Glint)
        drawCircle(
            color = Color.White,
            radius = irisRadius * 0.28f,
            center = Offset(eyeX - irisRadius * 0.3f, eyeY - irisRadius * 0.3f)
        )

        // Delineado según estilo
        val lashPath = Path().apply {
            when (eyeStyle.uppercase()) {
                "GATUNOS" -> {
                    moveTo(eyeX - eyeW * 0.6f, eyeY)
                    quadraticTo(eyeX, eyeY - eyeH * 0.8f, eyeX + eyeW * 0.7f, eyeY - eyeH * 0.5f)
                }
                "SOÑADORES" -> {
                    moveTo(eyeX - eyeW * 0.5f, eyeY)
                    quadraticTo(eyeX, eyeY - eyeH * 0.7f, eyeX + eyeW * 0.5f, eyeY)
                }
                "DECIDIDOS" -> {
                    moveTo(eyeX - eyeW * 0.55f, eyeY - eyeH * 0.2f)
                    lineTo(eyeX + eyeW * 0.55f, eyeY - eyeH * 0.6f)
                }
                else -> { // REDONDOS
                    moveTo(eyeX - eyeW * 0.5f, eyeY)
                    quadraticTo(eyeX, eyeY - eyeH * 0.65f, eyeX + eyeW * 0.5f, eyeY)
                }
            }
        }
        drawPath(path = lashPath, color = Color(0xFF120C24), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

        if (eyeStyle.equals("SOÑADORES", ignoreCase = true)) {
            drawCircle(color = Color.White, radius = 2.5f, center = Offset(eyeX + irisRadius * 0.4f, eyeY + irisRadius * 0.4f))
        }
    }

    // CEJAS
    val browY = eyeY - h * 0.042f
    val browColor = hairColor.copy(alpha = 0.9f)

    // Ceja Izquierda
    val leftBrow = Path().apply {
        moveTo(leftEyeX - eyeW * 0.55f, browY + 2f)
        quadraticTo(leftEyeX, browY - 6f, leftEyeX + eyeW * 0.5f, browY)
    }
    drawPath(path = leftBrow, color = browColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

    // Ceja Derecha
    val rightBrow = Path().apply {
        moveTo(rightEyeX - eyeW * 0.5f, browY)
        quadraticTo(rightEyeX, browY - 6f, rightEyeX + eyeW * 0.55f, browY + 2f)
    }
    drawPath(path = rightBrow, color = browColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

    // NARIZ
    val noseY = h * 0.43f
    val nosePath = Path().apply {
        moveTo(w * 0.49f, noseY - h * 0.02f)
        lineTo(w * 0.51f, noseY)
        quadraticTo(w * 0.50f, noseY + h * 0.015f, w * 0.485f, noseY + h * 0.01f)
    }
    drawPath(path = nosePath, color = Color.Black.copy(alpha = 0.25f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))

    // BOCA
    val mouthY = h * 0.51f
    val mouthW = w * 0.10f

    val mouthPath = Path().apply {
        moveTo(w * 0.5f - mouthW / 2f, mouthY)
        quadraticTo(w * 0.5f, mouthY + h * 0.025f, w * 0.5f + mouthW / 2f, mouthY)
    }
    drawPath(path = mouthPath, color = Color(0xFFC84B68), style = Stroke(width = 3.8f, cap = StrokeCap.Round))
}

private fun DrawScope.drawClothingDetails(
    w: Float,
    h: Float,
    style: String,
    secondaryColor: Color
) {
    val chestY = h * 0.68f

    when (style.uppercase()) {
        "HOODIE_NEON", "ROPA_1_FEM", "ROPA_1_MASC" -> {
            // Capucha / Capucha doblada alrededor del cuello
            drawArc(
                color = secondaryColor,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.35f, chestY - h * 0.03f),
                size = Size(w * 0.30f, h * 0.08f),
                style = Stroke(width = 5f)
            )
            // Cordones del Hoodie
            drawLine(secondaryColor, Offset(w * 0.45f, chestY), Offset(w * 0.44f, chestY + h * 0.10f), strokeWidth = 3f)
            drawLine(secondaryColor, Offset(w * 0.55f, chestY), Offset(w * 0.56f, chestY + h * 0.10f), strokeWidth = 3f)
            drawCircle(secondaryColor, radius = 3.5f, center = Offset(w * 0.44f, chestY + h * 0.10f))
            drawCircle(secondaryColor, radius = 3.5f, center = Offset(w * 0.56f, chestY + h * 0.10f))
        }

        "CHAQUETA_CIRCUITO", "ROPA_2_FEM", "ROPA_2_MASC" -> {
            // Cierre central y trazos de circuitos
            drawLine(secondaryColor, Offset(w * 0.5f, chestY - h * 0.02f), Offset(w * 0.5f, h), strokeWidth = 4f)

            // Líneas de circuito neón
            val circuit1 = Path().apply {
                moveTo(w * 0.32f, chestY + h * 0.02f)
                lineTo(w * 0.42f, chestY + h * 0.06f)
                lineTo(w * 0.42f, chestY + h * 0.15f)
            }
            drawPath(circuit1, color = secondaryColor, style = Stroke(width = 2.5f))
            drawCircle(secondaryColor, radius = 4f, center = Offset(w * 0.32f, chestY + h * 0.02f))

            val circuit2 = Path().apply {
                moveTo(w * 0.68f, chestY + h * 0.02f)
                lineTo(w * 0.58f, chestY + h * 0.06f)
                lineTo(w * 0.58f, chestY + h * 0.15f)
            }
            drawPath(circuit2, color = secondaryColor, style = Stroke(width = 2.5f))
            drawCircle(secondaryColor, radius = 4f, center = Offset(w * 0.68f, chestY + h * 0.02f))
        }

        "OVEROL_CYBER", "ROPA_3_FEM", "ROPA_3_MASC" -> {
            // Tirantes del Overol
            drawRect(secondaryColor, topLeft = Offset(w * 0.32f, chestY - h * 0.03f), size = Size(w * 0.08f, h * 0.25f))
            drawRect(secondaryColor, topLeft = Offset(w * 0.60f, chestY - h * 0.03f), size = Size(w * 0.08f, h * 0.25f))
            // Hebillas metálicas
            drawRect(Color.White, topLeft = Offset(w * 0.31f, chestY + h * 0.06f), size = Size(w * 0.10f, h * 0.025f), style = Stroke(width = 2.5f))
            drawRect(Color.White, topLeft = Offset(w * 0.59f, chestY + h * 0.06f), size = Size(w * 0.10f, h * 0.025f), style = Stroke(width = 2.5f))
        }

        else -> { // CAMISETA_GLOW o BOMBER_HOLO
            // Emblema Glow brillante en el pecho
            val centerLogo = Offset(w * 0.5f, chestY + h * 0.08f)
            drawCircle(secondaryColor.copy(alpha = 0.3f), radius = w * 0.09f, center = centerLogo)
            drawCircle(secondaryColor, radius = w * 0.06f, center = centerLogo, style = Stroke(width = 3.5f))
            drawCircle(NeonLime, radius = w * 0.025f, center = centerLogo)
        }
    }
}

private fun DrawScope.drawHair(
    w: Float,
    h: Float,
    style: String,
    primaryColor: Color,
    secondaryColor: Color
) {
    val headCenterX = w * 0.5f
    val headTopY = h * 0.16f

    when (style.uppercase()) {
        "CORTO_CLASICO", "HAIR_MASC_1", "HAIR_MASC_2", "HAIR_MASC_3" -> {
            // Peinado corto masculino con flequillo lateral
            val hairPath = Path().apply {
                moveTo(headCenterX - w * 0.22f, headTopY + h * 0.18f)
                quadraticTo(headCenterX - w * 0.24f, headTopY - h * 0.04f, headCenterX, headTopY - h * 0.04f)
                quadraticTo(headCenterX + w * 0.24f, headTopY - h * 0.04f, headCenterX + w * 0.22f, headTopY + h * 0.18f)
                quadraticTo(headCenterX + w * 0.10f, headTopY + h * 0.08f, headCenterX - w * 0.05f, headTopY + h * 0.12f)
                quadraticTo(headCenterX - w * 0.15f, headTopY + h * 0.14f, headCenterX - w * 0.22f, headTopY + h * 0.18f)
                close()
            }
            drawPath(path = hairPath, color = primaryColor)
            // Mecha de brillo secundario
            drawArc(
                color = secondaryColor,
                startAngle = 200f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(headCenterX - w * 0.18f, headTopY - h * 0.02f),
                size = Size(w * 0.36f, h * 0.12f),
                style = Stroke(width = 4f)
            )
        }

        "MOHAWK_NEON", "HAIR_MASC_4" -> {
            // Cresta Mohicano Neón
            val mohawkPath = Path().apply {
                moveTo(headCenterX - w * 0.06f, headTopY + h * 0.05f)
                lineTo(headCenterX - w * 0.08f, headTopY - h * 0.10f)
                lineTo(headCenterX, headTopY - h * 0.14f)
                lineTo(headCenterX + w * 0.08f, headTopY - h * 0.10f)
                lineTo(headCenterX + w * 0.06f, headTopY + h * 0.05f)
                close()
            }
            drawPath(path = mohawkPath, color = primaryColor)
            // Puntas luminosas secundarias
            val tipsPath = Path().apply {
                moveTo(headCenterX - w * 0.05f, headTopY - h * 0.08f)
                lineTo(headCenterX, headTopY - h * 0.15f)
                lineTo(headCenterX + w * 0.05f, headTopY - h * 0.08f)
            }
            drawPath(path = tipsPath, color = secondaryColor, style = Stroke(width = 4f))
        }

        "LARGO_ONDULADO", "PURPLE_BOB", "ONDAS_NEON", "HAIR_FEM_ONDAS" -> {
            // Cabello largo con ondas envolventes
            val hairPath = Path().apply {
                moveTo(headCenterX - w * 0.24f, headTopY + h * 0.35f)
                quadraticTo(headCenterX - w * 0.26f, headTopY - h * 0.05f, headCenterX, headTopY - h * 0.05f)
                quadraticTo(headCenterX + w * 0.26f, headTopY - h * 0.05f, headCenterX + w * 0.24f, headTopY + h * 0.35f)
                quadraticTo(headCenterX + w * 0.28f, headTopY + h * 0.50f, headCenterX + w * 0.20f, headTopY + h * 0.60f)
                lineTo(headCenterX + w * 0.15f, headTopY + h * 0.45f)
                quadraticTo(headCenterX, headTopY + h * 0.12f, headCenterX - w * 0.15f, headTopY + h * 0.45f)
                lineTo(headCenterX - w * 0.20f, headTopY + h * 0.60f)
                quadraticTo(headCenterX - w * 0.28f, headTopY + h * 0.50f, headCenterX - w * 0.24f, headTopY + h * 0.35f)
                close()
            }
            drawPath(path = hairPath, color = primaryColor)

            // Reflejos en ondas neón
            drawLine(secondaryColor, Offset(headCenterX - w * 0.20f, headTopY + h * 0.25f), Offset(headCenterX - w * 0.22f, headTopY + h * 0.48f), strokeWidth = 3.5f)
            drawLine(secondaryColor, Offset(headCenterX + w * 0.20f, headTopY + h * 0.25f), Offset(headCenterX + w * 0.22f, headTopY + h * 0.48f), strokeWidth = 3.5f)
        }

        "BOB_PIXIE", "HAIR_FEM_ASIMETRICO", "HAIR_FEM_LISO" -> {
            // Bob asimétrico moderno
            val hairPath = Path().apply {
                moveTo(headCenterX - w * 0.24f, headTopY + h * 0.30f)
                quadraticTo(headCenterX - w * 0.25f, headTopY - h * 0.05f, headCenterX, headTopY - h * 0.05f)
                quadraticTo(headCenterX + w * 0.25f, headTopY - h * 0.05f, headCenterX + w * 0.24f, headTopY + h * 0.22f)
                lineTo(headCenterX + w * 0.12f, headTopY + h * 0.10f)
                quadraticTo(headCenterX - w * 0.05f, headTopY + h * 0.14f, headCenterX - w * 0.24f, headTopY + h * 0.30f)
                close()
            }
            drawPath(path = hairPath, color = primaryColor)
        }

        "TRENZAS_GLOW", "HAIR_FEM_TRENZAS", "HAIR_FEM_2COLETA" -> {
            // Corona base
            drawArc(
                color = primaryColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(headCenterX - w * 0.22f, headTopY - h * 0.04f),
                size = Size(w * 0.44f, h * 0.25f)
            )
            // Trenza Izquierda
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(headCenterX - w * 0.25f, headTopY + h * 0.18f),
                size = Size(w * 0.07f, h * 0.35f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Trenza Derecha
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(headCenterX + w * 0.18f, headTopY + h * 0.18f),
                size = Size(w * 0.07f, h * 0.35f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Ligaduras neón
            drawCircle(secondaryColor, radius = 5f, center = Offset(headCenterX - w * 0.215f, headTopY + h * 0.50f))
            drawCircle(secondaryColor, radius = 5f, center = Offset(headCenterX + w * 0.215f, headTopY + h * 0.50f))
        }

        else -> { // RAPADO
            // Sombra sutil de rapado muy corto
            drawArc(
                color = primaryColor.copy(alpha = 0.45f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(headCenterX - w * 0.20f, headTopY - h * 0.02f),
                size = Size(w * 0.40f, h * 0.20f)
            )
        }
    }
}

private fun DrawScope.drawAccessories(
    w: Float,
    h: Float,
    accesorioId: String,
    accentColor: Color
) {
    val headCenterX = w * 0.5f
    val eyeY = h * 0.36f

    when (accesorioId.uppercase()) {
        "NEON_GLASSES" -> {
            // Gafas Píxel / Neón Futuristas
            val glassY = eyeY - h * 0.015f
            val glassW = w * 0.16f
            val glassH = h * 0.065f

            // Lente Izquierdo
            drawRoundRect(
                color = Color(0xDD120826),
                topLeft = Offset(headCenterX - glassW - 3f, glassY - glassH / 2f),
                size = Size(glassW, glassH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(headCenterX - glassW - 3f, glassY - glassH / 2f),
                size = Size(glassW, glassH),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 3f)
            )

            // Lente Derecho
            drawRoundRect(
                color = Color(0xDD120826),
                topLeft = Offset(headCenterX + 3f, glassY - glassH / 2f),
                size = Size(glassW, glassH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(headCenterX + 3f, glassY - glassH / 2f),
                size = Size(glassW, glassH),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 3f)
            )

            // Puente
            drawLine(accentColor, Offset(headCenterX - 3f, glassY), Offset(headCenterX + 3f, glassY), strokeWidth = 3f)
        }

        "VR_HEADSET" -> {
            // Visor VR Cyber
            val visorY = eyeY - h * 0.02f
            val visorW = w * 0.38f
            val visorH = h * 0.10f

            drawRoundRect(
                color = Color(0xFF140D2B),
                topLeft = Offset(headCenterX - visorW / 2f, visorY - visorH / 2f),
                size = Size(visorW, visorH),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = ElectricCyan,
                topLeft = Offset(headCenterX - visorW / 2f, visorY - visorH / 2f),
                size = Size(visorW, visorH),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 3.5f)
            )
            // Franja Neón Central
            drawLine(
                NeonLime,
                Offset(headCenterX - visorW * 0.38f, visorY),
                Offset(headCenterX + visorW * 0.38f, visorY),
                strokeWidth = 4f
            )
        }

        "GORRA_CYBER" -> {
            // Gorra Cyberpunk
            val capTopY = h * 0.14f
            val capPath = Path().apply {
                moveTo(headCenterX - w * 0.22f, capTopY + h * 0.10f)
                quadraticTo(headCenterX, capTopY - h * 0.04f, headCenterX + w * 0.22f, capTopY + h * 0.10f)
                close()
            }
            drawPath(capPath, color = Color(0xFF1B1238))
            // Visera de la gorra
            val brimPath = Path().apply {
                moveTo(headCenterX - w * 0.24f, capTopY + h * 0.10f)
                quadraticTo(headCenterX, capTopY + h * 0.16f, headCenterX + w * 0.24f, capTopY + h * 0.10f)
            }
            drawPath(brimPath, color = accentColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
        }

        "AUDIFONOS_GLOW" -> {
            // Audífonos Neón Over-Ear
            val bandTopY = h * 0.14f
            drawArc(
                color = accentColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(headCenterX - w * 0.24f, bandTopY),
                size = Size(w * 0.48f, h * 0.20f),
                style = Stroke(width = 4.5f)
            )
            // Copas laterales
            val cupY = eyeY + h * 0.02f
            drawCircle(accentColor, radius = w * 0.06f, center = Offset(headCenterX - w * 0.22f, cupY))
            drawCircle(Color.White, radius = w * 0.03f, center = Offset(headCenterX - w * 0.22f, cupY))
            drawCircle(accentColor, radius = w * 0.06f, center = Offset(headCenterX + w * 0.22f, cupY))
            drawCircle(Color.White, radius = w * 0.03f, center = Offset(headCenterX + w * 0.22f, cupY))
        }
    }
}

private fun parseColorSafely(hexString: String, default: Color): Color {
    if (hexString.isBlank()) return default
    return try {
        val cleanHex = hexString.trim()
        val formatted = if (cleanHex.startsWith("#")) cleanHex else "#$cleanHex"
        Color(formatted.toColorInt())
    } catch (_: Exception) {
        default
    }
}
