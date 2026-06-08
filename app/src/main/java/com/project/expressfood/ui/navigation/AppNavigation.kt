// ui/navigation/AppNavigation.kt
package com.project.expressfood.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.expressfood.ui.auth.AuthViewModel
import com.project.expressfood.ui.auth.LoginScreen
import com.project.expressfood.ui.splash.SplashScreen

object Routes {
    const val SPLASH      = "splash"
    const val LOGIN       = "login"
    const val HOME_CLIENT = "home_client"
    const val HOME_ADMIN  = "home_admin"
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                viewModel              = authViewModel,
                onNavigateToLogin      = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToClientHome = {
                    navController.navigate(Routes.HOME_CLIENT) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToAdminHome  = {
                    navController.navigate(Routes.HOME_ADMIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel              = authViewModel,
                onNavigateToClientHome = {
                    navController.navigate(Routes.HOME_CLIENT) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToAdminHome  = {
                    navController.navigate(Routes.HOME_ADMIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME_CLIENT) { Text("🏠 Home Cliente") }
        composable(Routes.HOME_ADMIN)  { Text("🔧 Home Admin")  }
    }
}