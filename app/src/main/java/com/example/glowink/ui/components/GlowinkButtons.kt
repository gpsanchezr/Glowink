package com.example.glowink.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.theme.*

/**
 * Componentes de Botones y Tarjetas Neón Reutilizables de Glowink.
 * 
 * Garantizan que los textos no se corten, no se superpongan con bordes Neón / Glassmorphism
 * y mantengan una alineación perfecta en cualquier densidad de pantalla.
 */

@Composable
fun GlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    containerColor: Color = NeonLime,
    contentColor: Color = Color.Black,
    borderColor: Color? = null,
    shape: Shape = RoundedCornerShape(14.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    fontSize: TextUnit = 13.sp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        shape = shape,
        contentPadding = contentPadding,
        modifier = modifier
            .defaultMinSize(minWidth = 72.dp, minHeight = 42.dp)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.5.dp, borderColor, shape)
                } else Modifier
            )
            .bounceClick(enabled = enabled, onClick = onClick)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GlowSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    borderColor: Color = ElectricCyan,
    textColor: Color = Color.White,
    backgroundColor: Color = Color(0x221E1735),
    shape: Shape = RoundedCornerShape(14.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    fontSize: TextUnit = 13.sp
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 72.dp, minHeight = 42.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(1.5.dp, borderColor, shape)
            .bounceClick(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = Color(0x6600F0FF),
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    GlassContainer(
        modifier = modifier,
        shape = shape,
        borderWidth = borderWidth,
        borderColor = borderColor,
        borderBrush = borderBrush
    ) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}
