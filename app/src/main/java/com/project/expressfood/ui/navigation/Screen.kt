package com.project.expressfood.ui.navigation

/** Rutas de navegación de la app. */
sealed class Screen(val route: String) {
    data object Login      : Screen("login")
    data object ClientHome : Screen("client_home")
    data object AdminHome  : Screen("admin_home")
}
