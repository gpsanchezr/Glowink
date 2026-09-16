package com.example.glowink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.glowink.ui.navigation.GlowinkAppNavigation
import com.example.glowink.ui.theme.GlowinkTheme

/**
 * Punto de entrada único de Glowink. Todo el flujo de pantallas vive en
 * [GlowinkAppNavigation] (ver ui/navigation/GlowinkNavigation.kt).
 *
 * Nota de historial: esta clase antes compartía archivo con "GlowinkShowcaseScreen", una
 * pantalla de muestra que solo mostraba la paleta de colores del sistema de diseño y que
 * estaba conectada (por error) a la pestaña "Descubrir" de la barra inferior — el usuario
 * tocaba "Descubrir" esperando explorar comunidades y en cambio veía una ficha técnica de
 * colores. Se movió esa pestaña a [com.example.glowink.ui.screens.DiscoverScreen], que sí
 * muestra contenido real, y se retiró la pantalla de muestra de la app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlowinkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlowinkAppNavigation()
                }
            }
        }
    }
}
