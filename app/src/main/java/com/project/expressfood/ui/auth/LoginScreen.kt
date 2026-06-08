// ui/auth/LoginScreen.kt
package com.project.expressfood.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.expressfood.domain.model.UserRole

@Composable
fun LoginScreen(
    viewModel             : AuthViewModel,
    onNavigateToClientHome: () -> Unit,
    onNavigateToAdminHome : () -> Unit,
) {
    val authState  by viewModel.authState.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val isSigningIn by viewModel.isSigningIn.collectAsState()
    val context = LocalContext.current

    // Reaccionar cuando Firebase confirma la sesión
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                if (state.user.role == UserRole.ADMIN) onNavigateToAdminHome()
                else onNavigateToClientHome()
            }
            else -> Unit
        }
    }

    Column(
        modifier              = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text("🍔 ExpressFood", style = MaterialTheme.typography.displaySmall)

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "Solo cuentas @gmail.com",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick  = { viewModel.signInWithGoogle(context) },
            enabled  = !isSigningIn,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSigningIn) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(8.dp))
            }
            Text("Continuar con Google")
        }

        // Error puntual del botón
        loginError?.let { error ->
            Spacer(Modifier.height(16.dp))
            Text(
                text      = error,
                color     = MaterialTheme.colorScheme.error,
                style     = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(48.dp))

        Text(
            text      = "Sin conexión algunas funciones no estarán disponibles",
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
        )
    }
}