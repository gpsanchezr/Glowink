package com.example.glowink.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.glowink.ui.theme.GlowinkTheme
import com.example.glowink.ui.viewmodel.ChatViewModel

/**
 * Pantalla de Registro de Cuenta (RegisterScreen)
 *
 * Delegado de compatibilidad con State Hoisting que invoca la pantalla [RegistrationScreen].
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAvatarCreation: () -> Unit = {},
    viewModel: ChatViewModel
) {
    RegistrationScreen(
        onBackClick = onNavigateToLogin,
        onNavigateToLogin = onNavigateToLogin,
        onRegisterSuccess = onNavigateToAvatarCreation,
        viewModel = viewModel
    )
}

/**
 * Componente puramente visual (Stateless) para la pantalla de registro.
 */
@Composable
fun RegisterScreenContent(
    isLoading: Boolean = false,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAvatarCreation: () -> Unit = {}
) {
    RegistrationScreenContent(
        isLoading = isLoading,
        onBackClick = onNavigateToLogin,
        onNavigateToLogin = onNavigateToLogin,
        onRegisterClick = { _, _, _ -> onNavigateToAvatarCreation() }
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    GlowinkTheme {
        RegisterScreenContent(
            isLoading = false
        )
    }
}
