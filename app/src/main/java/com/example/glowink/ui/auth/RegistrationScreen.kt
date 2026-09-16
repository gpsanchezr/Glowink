package com.example.glowink.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowink.R
import com.example.glowink.ui.theme.ElectricCyan
import com.example.glowink.ui.theme.GlassContainer
import com.example.glowink.ui.theme.NeonLime
import com.example.glowink.ui.theme.ObsidianBackground
import com.example.glowink.ui.theme.OnSurfaceMuted
import com.example.glowink.ui.theme.UltravioletPurple
import com.example.glowink.ui.theme.glassmorphic
import com.example.glowink.ui.viewmodel.ChatViewModel

/**
 * Pantalla de Registro / Login Aislada ("RegistrationScreen.kt")
 * Estética Dark Neon (#0A0714) Premium para Videojuegos.
 */
@Composable
fun RegistrationScreen(
    onBackClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    viewModel: ChatViewModel? = if (LocalInspectionMode.current) null else androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val isLoading = viewModel?.authLoading?.collectAsState()?.value ?: false

    RegistrationScreenContent(
        isLoading = isLoading,
        onBackClick = onBackClick,
        onNavigateToLogin = onNavigateToLogin,
        onRegisterClick = { username, email, password ->
            val cleanUsername = username.trim()
            val cleanEmail = email.trim()
            val cleanPassword = password.trim()
            if (viewModel != null) {
                viewModel.signUpWithEmail(
                    username = cleanUsername,
                    email = cleanEmail,
                    pass = cleanPassword,
                    onSuccess = onRegisterSuccess,
                    onError = { error ->
                        Toast.makeText(context, "No se pudo crear la cuenta: $error", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                Toast.makeText(context, "Modo Vista Previa: No hay conexión a Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

/**
 * Componente visual sin estado (Stateless) para el formulario de registro de cuenta.
 */
@Composable
fun RegistrationScreenContent(
    isLoading: Boolean = false,
    onBackClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onRegisterClick: (username: String, email: String, pass: String) -> Unit = { _, _, _ -> }
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val cleanUsername = username.trim()
    val cleanEmail = email.trim()
    val cleanPassword = password.trim()
    val cleanConfirmPassword = confirmPassword.trim()

    val hasMinLength = cleanPassword.length >= 6
    val hasDigit = cleanPassword.any { it.isDigit() }
    val hasUppercase = cleanPassword.any { it.isUpperCase() }
    val passwordsMatch = cleanPassword.isNotEmpty() && cleanPassword == cleanConfirmPassword
    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()
    val canProceed = cleanUsername.length >= 3 && emailValid && hasMinLength && hasDigit && hasUppercase && passwordsMatch

    Scaffold(
        containerColor = ObsidianBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianBackground,
                            Color(0xFF140D2A),
                            ObsidianBackground
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x221E1735))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape)
                            .clickable { onBackClick() }
                            .align(Alignment.CenterStart)
                    ) {
                        Text(
                            text = "❮",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        GlassContainer(
                            shape = CircleShape,
                            modifier = Modifier.size(70.dp),
                            borderWidth = 1.5.dp,
                            borderColor = ElectricCyan
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_glowink),
                                    contentDescription = "GLOWINK Logo",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "GLOWINK✨",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color.White
                        )

                        Text(
                            text = "Conéctate al Vibe ✨",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Crear tu Cuenta",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Únete a la comunidad y empieza a brillar",
                        fontSize = 13.5.sp,
                        color = OnSurfaceMuted
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(
                            shape = RoundedCornerShape(24.dp),
                            borderWidth = 1.dp,
                            borderColor = Color(0x33FFFFFF)
                        )
                        .padding(20.dp)
                ) {
                    Column {

                        // Campo 1: Nombre de usuario
                        Text(
                            text = "Nombre de usuario",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = UltravioletPurple
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it.take(30) },
                            placeholder = { Text("Tu nombre en Glowink", color = OnSurfaceMuted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Nombre de usuario",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            maxLines = 1,
                            textStyle = TextStyle(fontSize = 15.sp, color = Color.White),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                autoCorrectEnabled = false
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0x221E1735),
                                unfocusedContainerColor = Color(0x181E1735),
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = Color(0x4400F0FF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Campo 2: Correo Electrónico
                        Text(
                            text = "Correo Electrónico",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = UltravioletPurple
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("giseella@example.com", color = OnSurfaceMuted) },
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
                                unfocusedBorderColor = Color(0x4400F0FF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Campo 3: Contraseña
                        Text(
                            text = "Contraseña",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = UltravioletPurple
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
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
                                        .clickable { passwordVisible = !passwordVisible }
                                        .padding(horizontal = 10.dp)
                                )
                            },
                            singleLine = true,
                            maxLines = 1,
                            textStyle = TextStyle(fontSize = 15.sp, color = Color.White),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0x221E1735),
                                unfocusedContainerColor = Color(0x181E1735),
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = Color(0x4439FF14),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Campo 4: Confirmar Contraseña
                        Text(
                            text = "Confirmar Contraseña",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = UltravioletPurple
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("••••••••", color = OnSurfaceMuted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Confirmar Contraseña",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                Text(
                                    text = if (confirmPasswordVisible) "🙈" else "👁",
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .clickable { confirmPasswordVisible = !confirmPasswordVisible }
                                        .padding(horizontal = 10.dp)
                                )
                            },
                            singleLine = true,
                            maxLines = 1,
                            textStyle = TextStyle(fontSize = 15.sp, color = Color.White),
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0x221E1735),
                                unfocusedContainerColor = Color(0x181E1735),
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = Color(0x4400F0FF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. VALIDACIONES DE REQUISITOS (CHECKLIST VERDE REACTIVO)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ValidationRequirementItem("Mínimo 6 caracteres", hasMinLength)
                            ValidationRequirementItem("Al menos un número (0-9)", hasDigit)
                            ValidationRequirementItem("Al menos una mayúscula (A-Z)", hasUppercase)
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // 5. BOTÓN PRINCIPAL
                        Button(
                            onClick = {
                                if (canProceed && !isLoading) {
                                    onRegisterClick(cleanUsername, cleanEmail, cleanPassword)
                                } else if (!isLoading) {
                                    val reason = when {
                                        cleanUsername.length < 3 -> "El nombre de usuario debe tener al menos 3 caracteres"
                                        cleanEmail.isBlank() || !emailValid -> "Ingresa un correo electrónico válido"
                                        !hasMinLength -> "La contraseña debe tener al menos 6 caracteres"
                                        !hasDigit -> "Falta incluir al menos un número en la contraseña"
                                        !hasUppercase -> "Falta incluir al menos una letra mayúscula"
                                        !passwordsMatch -> "Las contraseñas no coinciden"
                                        else -> "Completa correctamente los datos del formulario"
                                    }
                                    Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (canProceed) Brush.horizontalGradient(
                                        listOf(UltravioletPurple, ElectricCyan, NeonLime)
                                    )
                                    else Brush.horizontalGradient(
                                        listOf(Color(0xFF2A2438), Color(0xFF2A2438))
                                    )
                                )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("👤", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Registrarse ➔",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canProceed) Color.Black else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.clickable { onNavigateToLogin() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? ",
                        color = Color.White,
                        fontSize = 13.5.sp
                    )
                    Text(
                        text = "Iniciar sesión >",
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ValidationRequirementItem(text: String, isSatisfied: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Text(
            text = if (isSatisfied) "✓" else "○",
            color = if (isSatisfied) NeonLime else OnSurfaceMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.width(18.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isSatisfied) NeonLime else OnSurfaceMuted
        )
    }
}
