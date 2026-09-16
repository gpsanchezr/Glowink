package com.example.glowink.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.ObsidianBackground
import com.example.glowink.ui.theme.OnSurfaceMuted

/**
 * "Visor 3D": muestra un modelo .glb (por ejemplo, exportado de Tripo3D) usando el
 * componente web `<model-viewer>` de Google dentro de un WebView. Es una pieza de
 * presentación independiente del editor de avatar 2D (ver ui/avatar/) — ver
 * `assets/models/LEEME.md` para una explicación completa de por qué no están conectados.
 *
 * La cámara arranca en `camera-orbit="0deg 90deg ..."`, es decir, mirando estrictamente de
 * frente, tal como pedía el requerimiento original; el usuario puede arrastrar para rotarlo
 * libremente desde ahí.
 */
private const val MODEL_FILE_NAME = "avatar_showcase.glb"

@Composable
fun Avatar3DViewerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val modelExists = remember {
        try {
            context.assets.open("models/$MODEL_FILE_NAME").use { true }
        } catch (e: Exception) {
            false
        }
    }

    Scaffold(containerColor = ObsidianBackground) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = Color.White, fontSize = 20.sp)
                }
                Text(
                    "VISOR 3D",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }

            if (modelExists) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx -> buildModelViewerWebView(ctx) }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧊", fontSize = 48.sp)
                        Text(
                            "Todavía no hay un modelo 3D cargado",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 14.dp)
                        )
                        Text(
                            "Exporta un .glb desde Tripo3D y colócalo en\napp/src/main/assets/models/$MODEL_FILE_NAME",
                            color = OnSurfaceMuted,
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            "(ver LEEME.md en esa misma carpeta)",
                            color = ElectricCyan,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun buildModelViewerWebView(context: android.content.Context): WebView {
    return WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        setBackgroundColor(android.graphics.Color.parseColor("#0A0714"))
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <script type="module" src="https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js"></script>
              <style>
                html, body { margin:0; padding:0; height:100%; background:#0A0714; }
                model-viewer { width:100%; height:100%; --poster-color: transparent; }
              </style>
            </head>
            <body>
              <model-viewer
                src="models/$MODEL_FILE_NAME"
                camera-controls
                camera-orbit="0deg 90deg 2.2m"
                min-camera-orbit="auto 60deg auto"
                max-camera-orbit="auto 120deg auto"
                shadow-intensity="1"
                exposure="1.1"
                interaction-prompt="none"
                auto-rotate
                auto-rotate-delay="4000"
                rotation-per-second="18deg">
              </model-viewer>
            </body>
            </html>
        """.trimIndent()
        loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
    }
}
