package com.example.glowink.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Iconografía vectorial propia dibujada a mano en Canvas — sin depender de la librería
 * "material-icons-extended" (que este proyecto no incluye) ni de emojis del sistema, que se
 * ven distinto en cada fabricante de teléfono y rompen la ambientación cyberpunk del resto
 * de la app.
 *
 * Reemplaza el ícono de puerta de madera 🚪 que se usaba antes para "cerrar sesión" (un
 * emoji que no encajaba con el resto del sistema de diseño neón) por un símbolo de
 * "encendido/apagado" minimalista, el estándar universal para cerrar sesión / salir.
 */
@Composable
fun NeonLogoutIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF6B81)
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val strokeWidth = size.minDimension * 0.14f
        val radius = size.minDimension * 0.34f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawArc(
            color = color,
            startAngle = -55f,
            sweepAngle = 290f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawLine(
            color = color,
            start = Offset(center.x, center.y - radius * 1.45f),
            end = Offset(center.x, center.y - radius * 0.1f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
