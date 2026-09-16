package com.example.glowink.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.R
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.GlassContainer
import com.example.glowink.ui.theme.GlowinkTheme
import com.example.glowink.ui.theme.NeonGradientLimeCyan
import com.example.glowink.ui.theme.NeonGradientPrimary
import com.example.glowink.ui.theme.ObsidianBackground
import com.example.glowink.ui.theme.UltravioletPurple

/**
 * Pantalla de Bienvenida (WelcomeScreen) - Diseño Dark Neon (#0A0714)
 *
 * Muestra el logo oficial de Glowink con animación sutil de pulso neón y ofrece los botones
 * independientes para "Iniciar Sesión" y "Registrarse", sirviendo como el 'startDestination'
 * del flujo de autenticación de la app.
 */
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    WelcomeScreenContent(
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToRegister = onNavigateToRegister
    )
}

/**
 * Componente puramente visual (Stateless) para la pantalla de bienvenida.
 */
@Composable
fun WelcomeScreenContent(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WelcomePulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBackground,
                        Color(0xFF140D2B),
                        ObsidianBackground
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emblema Central Neón con Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(scalePulse)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ElectricCyan.copy(alpha = 0.45f),
                                    UltravioletPurple.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                GlassContainer(
                    shape = CircleShape,
                    modifier = Modifier.size(130.dp),
                    borderWidth = 2.dp,
                    borderBrush = NeonGradientPrimary
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_glowink),
                            contentDescription = "Glowink Logo",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "GLOWINK✨",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Conéctate al Vibe✨\n¡Tu combo te espera!",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Botón: Iniciar Sesión (Degradado Neón)
            Button(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NeonGradientLimeCyan)
            ) {
                Text(
                    text = "Iniciar Sesión 🚀",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón: Registrarse (Borde Neón y fondo Glassmorphism)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x221E1735))
                    .border(1.5.dp, ElectricCyan, RoundedCornerShape(24.dp))
                    .clickable { onNavigateToRegister() }
            ) {
                Text(
                    text = "Crear Cuenta Nueva ✨",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    GlowinkTheme {
        WelcomeScreenContent()
    }
}
