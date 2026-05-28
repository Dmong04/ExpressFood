package com.project.expressfood.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.expressfood.domain.model.UserRole
import com.project.expressfood.ui.admin.AdminHomeScreen
import com.project.expressfood.ui.auth.AuthState
import com.project.expressfood.ui.auth.AuthViewModel
import com.project.expressfood.ui.auth.LoginScreen
import com.project.expressfood.ui.client.ClientHomeScreen

/**
 * Grafo de navegación principal.
 * Observa el AuthState y redirige al destino correcto según el rol del usuario.
 */
@Composable
fun AppNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    // Navegación basada en el estado de autenticación
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                val destination = if (state.user.role == UserRole.ADMIN) {
                    Screen.AdminHome.route
                } else {
                    Screen.ClientHome.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> { /* Loading / Error — no navegar */ }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = Screen.Login.route,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(viewModel = authViewModel)
        }
        composable(Screen.ClientHome.route) {
            ClientHomeScreen(viewModel = authViewModel)
        }
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(viewModel = authViewModel)
        }
    }
}
