// ui/splash/SplashScreen.kt
package com.project.expressfood.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.project.expressfood.domain.model.UserRole
import com.project.expressfood.ui.auth.AuthState
import com.project.expressfood.ui.auth.AuthViewModel

@Composable
fun SplashScreen(
    viewModel             : AuthViewModel,
    onNavigateToLogin     : () -> Unit,
    onNavigateToClientHome: () -> Unit,
    onNavigateToAdminHome : () -> Unit,
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Loading         -> Unit  // espera
            is AuthState.Unauthenticated -> onNavigateToLogin()
            is AuthState.Authenticated   -> {
                if (state.user.role == UserRole.ADMIN) onNavigateToAdminHome()
                else onNavigateToClientHome()
            }
            is AuthState.Error           -> onNavigateToLogin()
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}