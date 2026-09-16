package com.example.glowink.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.R
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.GlassContainer
import com.example.glowink.ui.theme.GlowinkTheme
import com.example.glowink.ui.theme.NeonGradientLimeCyan
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.theme.ObsidianBackground
import com.example.glowink.ui.theme.OnSurfaceMuted
import com.example.glowink.ui.theme.UltravioletPurple
import com.example.glowink.ui.theme.glassmorphic
import com.example.glowink.ui.viewmodel.ChatViewModel

/**
 * Pantalla de Iniciar Sesión (LoginScreen) - Stateful Composable
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    viewModel: ChatViewModel? = if (LocalInspectionMode.current) null else ChatViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = viewModel?.authLoading?.collectAsState()?.value ?: false
    val context = LocalContext.current

    LoginScreenContent(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        passwordVisible = passwordVisible,
        onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
        isLoading = isLoading,
        onLoginClick = {
            val cleanEmail = email.trim()
            val cleanPassword = password.trim()
            when {
                cleanEmail.isBlank() || cleanPassword.isBlank() -> {
                    Toast.makeText(context, "Por favor completa el email y la contraseña", Toast.LENGTH_SHORT).show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> {
                    Toast.makeText(context, "Ingresa un email válido", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    viewModel?.signInWithEmail(
                        email = cleanEmail,
                        pass = cleanPassword,
                        onSuccess = onLoginSuccess,
                        onError = { err ->
                            Toast.makeText(context, "Error de acceso: $err", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        },
        onNavigateToRegister = onNavigateToRegister
    )
}

/**
 * Contenido visual de Iniciar Sesión (LoginScreenContent) - Stateless Composable
 */
@Composable
fun LoginScreenContent(
    email: String = "",
    onEmailChange: (String) -> Unit = {},
    password: String = "",
    onPasswordChange: (String) -> Unit = {},
    passwordVisible: Boolean = false,
    onPasswordVisibleToggle: () -> Unit = {},
    isLoading: Boolean = false,
    onLoginClick: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBackground,
                        Color(0xFF160E30),
                        ObsidianBackground
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            GlassContainer(
                shape = CircleShape,
                modifier = Modifier.size(86.dp),
                borderWidth = 1.5.dp,
                borderColor = ElectricCyan
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (isPreview) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(UltravioletPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 24.sp)
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_glowink),
                            contentDescription = "Glowink Logo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Conéctate al Squad",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ingresa con tu correo de jugador para empezar",
                fontSize = 14.sp,
                color = ElectricCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(24.dp),
                        borderWidth = 1.dp,
                        borderColor = ElectricCyan.copy(alpha = 0.6f)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Correo Electrónico",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = UltravioletPurple
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        placeholder = { Text("tu.tag@glowink.com", color = OnSurfaceMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Correo Electrónico",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        maxLines = 1,
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.White),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Contraseña Secreta",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = UltravioletPurple
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        placeholder = { Text("••••••••", color = OnSurfaceMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Contraseña",
                                tint = NeonLime,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "🙈" else "👁",
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .clickable { onPasswordVisibleToggle() }
                                    .padding(horizontal = 12.dp)
                            )
                        },
                        singleLine = true,
                        maxLines = 1,
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.White),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x221E1735),
                            unfocusedContainerColor = Color(0x181E1735),
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = Color(0x5539FF14),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    Button(
                        onClick = onLoginClick,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(NeonGradientLimeCyan)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                        } else {
                            Text(
                                text = "Iniciar sesión →",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.clickable { onNavigateToRegister() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "¿No tienes cuenta? ", color = Color.White, fontSize = 14.sp)
                Text(text = "Regístrate aquí >", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    GlowinkTheme {
        LoginScreenContent(
            email = "proplayer@glowink.com",
            password = "password123",
            passwordVisible = false,
            isLoading = false
        )
    }
}
