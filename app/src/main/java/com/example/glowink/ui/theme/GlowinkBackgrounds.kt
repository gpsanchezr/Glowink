package com.example.glowink.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Sistema de Fondos Dinámicos Neón para Glowink.
 */
@Composable
fun GlowinkMainBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0A21),
                        Color(0xFF1B1038),
                        Color(0xFF0C071A)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Cristales de fondo sutiles
            val crystalPath = Path().apply {
                moveTo(width * 0.1f, height * 0.8f)
                lineTo(width * 0.3f, height * 0.75f)
                lineTo(width * 0.25f, height * 0.9f)
                close()
            }
            drawPath(path = crystalPath, color = UltravioletPurple.copy(alpha = 0.05f))

            // Circuitos neón
            val circuitPath = Path().apply {
                moveTo(width, height * 0.1f)
                lineTo(width * 0.8f, height * 0.15f)
                lineTo(width * 0.75f, height * 0.3f)
                lineTo(width * 0.5f, height * 0.35f)
            }
            drawPath(
                path = circuitPath,
                color = ElectricCyan.copy(alpha = 0.08f),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
            
            // Partículas de luz
            drawCircle(color = NeonLime.copy(alpha = 0.05f), radius = 100f, center = Offset(width * 0.2f, height * 0.4f))
            drawCircle(color = UltravioletPurple.copy(alpha = 0.05f), radius = 150f, center = Offset(width * 0.8f, height * 0.7f))
        }
    }
}

@Composable
fun GlowinkArenaBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1B1038), Color(0xFF0A071A)),
                    center = Offset.Zero
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Rejilla de perspectiva
            val gridColor = ElectricCyan.copy(alpha = 0.05f)
            for (i in 0..10) {
                val x = w * (i / 10f)
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                val y = h * (i / 10f)
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }
        }
    }
}
