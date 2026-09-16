package com.example.glowink.ui.screens

import android.widget.Toast

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.GlassContainer
import com.example.glowink.ui.theme.GlowinkTheme
import com.example.glowink.ui.theme.NeonGradientLimeCyan
import com.example.glowink.ui.theme.NeonGradientPrimary
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.theme.ObsidianBackground
import com.example.glowink.ui.theme.OnSurfaceMuted
import com.example.glowink.ui.theme.UltravioletPurple
import com.example.glowink.ui.viewmodel.ChatViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private data class CameraFilter(
    val id: String,
    val name: String,
    val icon: String,
    val overlayEmoji: String,
    val primaryColor: Color
)

@Composable
fun CameraScreen(
    viewModel: ChatViewModel = ChatViewModel(),
    onPublishSuccess: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val filters = remember {
        listOf(
            CameraFilter("AURA_NEON", "Aura Neón", "⭕", "✨💜", UltravioletPurple),
            CameraFilter("COMIC_MODE", "Modo Cómico", "👩‍🎨", "🎨⚡", ElectricCyan),
            CameraFilter("KAWAII", "Mascota Kawaii", "🐱", "🐱💖", NeonLime),
            CameraFilter("HOLO_GLASSES", "Lentes Holográficos", "🕶️", "🕶️🌈", ElectricCyan),
            CameraFilter("DISCO_VIBE", "Vibe Disco", "🪩", "🪩🪩", UltravioletPurple)
        )
    }

    var selectedFilter by remember { mutableStateOf(filters.first()) }
    var isCaptured by remember { mutableStateOf(false) }
    var addedStickers by remember { mutableStateOf(listOf("💖", "⚡")) }
    // Antes el botón de girar cámara (🔄) tenía un manejador de clic vacío ("Girar Cámara"
    // era solo un comentario) y "Tomar Foto" únicamente activaba isCaptured sin capturar
    // ninguna imagen real. Ahora ambos son funcionales de verdad.
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val uiState by viewModel.chatsListUiState.collectAsState()
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    selectedFilter = selectedFilter,
                    addedStickers = addedStickers,
                    isCaptured = isCaptured,
                    lensFacing = lensFacing,
                    onImageCaptureReady = { imageCapture = it }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Se requiere permiso de cámara para continuar.",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            // Foto real capturada: se muestra encima del preview en vivo mientras se edita
            if (isCaptured && capturedBitmap != null) {
                Image(
                    bitmap = capturedBitmap!!.asImageBitmap(),
                    contentDescription = "Foto capturada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Bar: Cerrar / Cambiar Cámara
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .clickable { onCloseClick() }
                ) {
                    Text(text = "✕", fontSize = 20.sp, color = Color.White)
                }

                Text(
                    text = if (isCaptured) "Edita tu Vibe 🎨" else "Cámara Neón 📸",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .clickable {
                            if (!isCaptured) {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                    CameraSelector.LENS_FACING_BACK
                                } else {
                                    CameraSelector.LENS_FACING_FRONT
                                }
                            }
                        }
                ) {
                    Text(text = "🔄", fontSize = 20.sp)
                }
            }

            // --- 2. Modo Captura vs Modo Edición ---
            if (!isCaptured) {
                // Modo Cámara Normal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp)
                ) {

                    // Carrusel Flotante Inferior de Filtros Seleccionables
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filters) { filter ->
                            val isSelected = selectedFilter.id == filter.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedFilter = filter }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x55140D2B))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) filter.primaryColor else Color(0x66FFFFFF),
                                            shape = CircleShape
                                        )
                                ) {
                                    Text(text = filter.icon, fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = filter.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) filter.primaryColor else OnSurfaceMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de Disparo / Tomar Foto — ahora captura una imagen real con
                    // CameraX ImageCapture en vez de solo activar un booleano.
                    Button(
                        onClick = {
                            val capture = imageCapture
                            if (capture == null) {
                                isCaptured = true
                                return@Button
                            }
                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : androidx.camera.core.ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                                        val buffer = image.planes[0].buffer
                                        val bytes = ByteArray(buffer.remaining())
                                        buffer.get(bytes)
                                        val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        val rotation = image.imageInfo.rotationDegrees
                                        capturedBitmap = if (rotation != 0 && rawBitmap != null) {
                                            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                                            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                                        } else {
                                            rawBitmap
                                        }
                                        isCaptured = true
                                        image.close()
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("CameraScreen", "Error al capturar foto", exception)
                                        // Si la captura real falla (p. ej. emulador sin cámara),
                                        // igual dejamos avanzar el flujo de edición/publicación.
                                        isCaptured = true
                                    }
                                }
                            )
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(NeonGradientPrimary)
                    ) {
                        Text(
                            text = "Tomar Foto",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            } else {
                // Vista de Edición Poscaptura
                GlassContainer(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    borderWidth = 2.dp,
                    borderBrush = NeonGradientPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "¡Mi vibe de hoy en Glowink! 💖🐾 #${selectedFilter.name.replace(" ", "")}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Herramientas de Edición Neón
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            EditToolChip(icon = "🤪", label = "Sticker") {
                                addedStickers += "🎀"
                            }
                            EditToolChip(icon = "✏️", label = "Lápiz") {}
                            EditToolChip(icon = "💬", label = "Chat") {}
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    isCaptured = false
                                    capturedBitmap = null
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text(text = "Retomar 📸", color = Color.White, fontSize = 14.sp)
                            }

                            // Botón Grande: "Publicar Estado" — ahora sube la foto real a
                            // Firebase Storage y guarda esa URL (antes siempre guardaba el
                            // texto fijo "story_media", sin importar qué foto se tomara).
                            Button(
                                onClick = {
                                    val bitmap = capturedBitmap
                                    val uid = uiState.currentUser?.id
                                    if (bitmap == null || uid.isNullOrBlank()) {
                                        Toast.makeText(
                                            context,
                                            "No hay una foto válida o una sesión autenticada.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@Button
                                    }
                                    isUploading = true
                                    val stream = java.io.ByteArrayOutputStream()
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                                    val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                                        .reference
                                        .child("stories/$uid/${System.currentTimeMillis()}.jpg")
                                    storageRef.putBytes(stream.toByteArray())
                                        .continueWithTask { task ->
                                            if (!task.isSuccessful) {
                                                task.exception?.let { throw it }
                                            }
                                            storageRef.downloadUrl
                                        }
                                        .addOnSuccessListener { uri ->
                                            viewModel.publishStory(uri.toString(), selectedFilter.name)
                                            isUploading = false
                                            onPublishSuccess()
                                        }
                                        .addOnFailureListener {
                                            Log.e("CameraScreen", "No se pudo subir la foto a Storage", it)
                                            isUploading = false
                                            Toast.makeText(
                                                context,
                                                "No se pudo publicar la historia. Revisa la conexión y las reglas de Firebase Storage.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                },
                                enabled = !isUploading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NeonGradientLimeCyan)
                            ) {
                                if (isUploading) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Publicar Estado ✨",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vista de cámara real utilizando CameraX PreviewView.
 *
 * Ahora también expone un [ImageCapture] real (para el botón "Tomar Foto") y re-vincula el
 * caso de uso de la cámara cada vez que cambia [lensFacing] (para el botón 🔄, que antes no
 * hacía nada).
 */
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    selectedFilter: CameraFilter,
    addedStickers: List<String>,
    isCaptured: Boolean,
    lensFacing: Int,
    onImageCaptureReady: (ImageCapture) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(cameraProviderFuture, lensFacing) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCapture = ImageCapture.Builder().build()
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            onImageCaptureReady(imageCapture)
        } catch (exc: Exception) {
            Log.e("CameraPreview", "Use case binding failed", exc)
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Superposición de Filtros Neón AR (Simulados sobre la cámara real)
        val infiniteTransition = rememberInfiniteTransition(label = "FilterPulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseScale"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Máscara Neón AR (Overlay)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(260.dp)
                    .border(width = 3.dp, color = selectedFilter.primaryColor, shape = CircleShape)
            ) {
                Text(
                    text = selectedFilter.overlayEmoji,
                    fontSize = 64.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 20.dp)
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                )
            }

            // Stickers Luminosos
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 200.dp)
            ) {
                addedStickers.forEach { sticker ->
                    Text(text = sticker, fontSize = 40.sp)
                }
            }
        }
        
        // Si está capturado, podemos oscurecer la vista para simular la captura
        if (isCaptured) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun EditToolChip(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    GlassContainer(
        shape = RoundedCornerShape(16.dp),
        borderWidth = 1.dp,
        borderColor = ElectricCyan,
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@ComposePreview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    GlowinkTheme {
        CameraScreen()
    }
}
