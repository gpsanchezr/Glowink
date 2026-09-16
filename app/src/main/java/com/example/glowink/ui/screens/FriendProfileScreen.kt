package com.example.glowink.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.ui.avatar.GlowAvatarFrame
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.GlassContainer
import com.example.glowink.ui.theme.NeonGradientPrimary
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.theme.ObsidianBackground
import com.example.glowink.ui.theme.OnSurfaceMuted
import com.example.glowink.ui.theme.UltravioletPurple
import com.example.glowink.ui.viewmodel.ChatViewModel

/**
 * Pantalla de Perfil de Amigo ("FriendProfileScreen.kt")
 * Estética Cyberpunk Neón (#151026).
 *
 * REGLAS CUMPLIDAS:
 * 1. Cabecera Neón con Foto de perfil real fija del amigo.
 * 2. Lógica de Nombre: Si hay apodo personalizado, muestra el apodo en texto neón cian/magenta
 *    y debajo el nombre de usuario original en magenta pequeño.
 * 3. Campo de texto neón para guardar un apodo personalizado en Firestore.
 * 4. Opción "Añadir a Favoritos" con Switch Neón e ícono de estrella cian.
 */
@Composable
fun FriendProfileScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.chatDetailUiState.collectAsState()
    val friend = uiState.selectedFriend
    val context = LocalContext.current

    var customNicknameInput by remember(friend?.customNickname) {
        mutableStateOf(friend?.customNickname.orEmpty())
    }
    var isFavorite by remember(friend?.esFavorito) {
        mutableStateOf(friend?.esFavorito ?: false)
    }

    val friendName = friend?.username.orEmpty().ifBlank { "Jugador" }
    val nickname = friend?.customNickname.orEmpty().trim()

    Scaffold(
        containerColor = ObsidianBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianBackground,
                            Color(0xFF1A0F35),
                            ObsidianBackground
                        )
                    )
                )
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. App Bar Superior: Flecha Atrás + Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ElectricCyan,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBackClick() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "PERFIL DEL CLAN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = UltravioletPurple
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Cabecera con Foto Real Fija y Anillo Neón
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(150.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(ElectricCyan.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )

                GlowAvatarFrame(
                    config = friend?.avatarConfig ?: com.example.glowink.data.Avatar3DConfig(),
                    profileImageUrl = friend?.profileImageUrl?.ifBlank { friend.avatarUrl },
                    size = 126.dp,
                    ringBrush = NeonGradientPrimary,
                    ringWidth = 3.dp,
                    backgroundColor = Color(0xFF1A1430)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lógica de Nombre Neón
            if (nickname.isNotBlank()) {
                Text(
                    text = nickname,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "@$friendName",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = UltravioletPurple
                )
            } else {
                Text(
                    text = friendName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = friend?.email.orEmpty().ifBlank { "Conectado al Clan Glowink ✨" },
                fontSize = 13.sp,
                color = OnSurfaceMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Configuración Neón (GlassContainer)
            GlassContainer(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                borderColor = ElectricCyan.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "CONFIGURACIÓN DEL CONTACTO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // A) Campo para Apodo Personalizado
                    Text(
                        text = "Apodo Personalizado",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = UltravioletPurple
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customNicknameInput,
                        onValueChange = { customNicknameInput = it },
                        placeholder = { Text("Escribe un apodo especial...", color = OnSurfaceMuted) },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                            autoCorrectEnabled = false
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x221E1735),
                            unfocusedContainerColor = Color(0x181E1735),
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Color(0x5500F0FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            friend?.id?.let { friendId ->
                                viewModel.updateFriendCustomNickname(friendId, customNicknameInput)
                                Toast.makeText(context, "Apodo guardado correctamente ✨", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Guardar Apodo", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // B) Interruptor de Favoritos
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) NeonLime else ElectricCyan.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Añadir a Favoritos",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Aparecerá en la pestaña de Favoritos",
                                    fontSize = 11.5.sp,
                                    color = OnSurfaceMuted
                                )
                            }
                        }

                        Switch(
                            checked = isFavorite,
                            onCheckedChange = { checked ->
                                isFavorite = checked
                                friend?.id?.let { friendId ->
                                    viewModel.toggleFavoriteFriend(friendId, checked)
                                    val msg = if (checked) "Añadido a Favoritos ⭐️" else "Removido de Favoritos"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = NeonLime,
                                uncheckedThumbColor = ElectricCyan,
                                uncheckedTrackColor = Color(0x331E1735)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Estadísticas del Amigo
            GlassContainer(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                borderColor = NeonLime.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ESTADÍSTICAS DEL SQUAD",
                        fontSize = 12.sp,
                        color = OnSurfaceMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProfileStatItem("Nivel", "Nivel ${friend?.nivel ?: 1}", ElectricCyan)
                        ProfileStatItem("Racha", "⚡ ${friend?.rachaVictorias ?: 0}", NeonLime)
                        ProfileStatItem("GlowCoins", "🪙 ${friend?.glowCoins ?: 0}", Color(0xFFFFD24C))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = OnSurfaceMuted)
        Text(text = value, fontSize = 15.sp, color = color, fontWeight = FontWeight.Bold)
    }
}
