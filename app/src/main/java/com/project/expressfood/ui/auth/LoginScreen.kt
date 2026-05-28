package com.project.expressfood.ui.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.project.expressfood.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val authState by viewModel.authState.collectAsState()
    val context   = LocalContext.current
    val scope     = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Mostrar error del ViewModel
    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            errorMessage = (authState as AuthState.Error).message
        }
    }

    val isLoading = authState is AuthState.Loading

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Hero ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.42f)
                .background(MaterialTheme.colorScheme.primary),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "🍔", fontSize = 72.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "ExpressFood",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White,
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Tu comida, más rápido 🚀",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.85f),
                ),
            )
        }

        // ── Login card ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.58f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 32.dp)
                .padding(top = 36.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = "¡Bienvenido!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Inicia sesión para ver el menú\ny realizar tus pedidos",
                style     = MaterialTheme.typography.bodyMedium.copy(
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign  = TextAlign.Center,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))

            // ── Botón Google ─────────────────────────────────────────────
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    scope.launch {
                        try {
                            val serverClientId = context.getString(R.string.default_web_client_id)
                            val idToken = getGoogleIdToken(context, serverClientId)
                            if (idToken != null) {
                                viewModel.handleGoogleIdToken(idToken)
                            }
                        } catch (e: GetCredentialException) {
                            errorMessage = "Error al obtener credenciales. Intenta de nuevo."
                        } catch (e: Exception) {
                            errorMessage = "Error inesperado: ${e.message}"
                        }
                    }
                },
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(12.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Google "G" estilizado
                        Text(
                            text  = "G",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color(0xFF4285F4),
                            ),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text  = "Continuar con Google",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                }
            }

            // ── Error ────────────────────────────────────────────────────
            if (errorMessage != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text      = errorMessage!!,
                    style     = MaterialTheme.typography.bodySmall.copy(
                        color     = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    ),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text  = "© 2026 ExpressFood — Proyecto Académico",
                style = MaterialTheme.typography.labelSmall.copy(
                    color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

// ─── Helper: obtener ID token de Google vía Credential Manager ────────────────

private suspend fun getGoogleIdToken(context: Context, serverClientId: String): String? {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(serverClientId)
        .setFilterByAuthorizedAccounts(false)   // Mostrar todos los accounts del dispositivo
        .setAutoSelectEnabled(false)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result     = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            null
        }
    } catch (e: GetCredentialCancellationException) {
        null  // El usuario canceló el selector — no es un error
    }
    // Otras GetCredentialException se propagan al caller
}
