// MainActivity.kt
package com.project.expressfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.project.expressfood.ui.auth.AuthViewModel
import com.project.expressfood.ui.navigation.AppNavigation
import com.project.expressfood.ui.theme.ExpressFoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as ExpressFoodApp).container
        val authViewModel: AuthViewModel by viewModels {
            AuthViewModel.Factory(container.authRepository)
        }

        setContent {
            ExpressFoodTheme {
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}