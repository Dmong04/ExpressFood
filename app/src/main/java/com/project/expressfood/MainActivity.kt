package com.project.expressfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.expressfood.ui.auth.AuthViewModel
import com.project.expressfood.ui.navigation.AppNavGraph
import com.project.expressfood.ui.theme.ExpressFoodTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as ExpressFoodApp).container

        setContent {
            ExpressFoodTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(appContainer.authRepository),
                )
                AppNavGraph(authViewModel = authViewModel)
            }
        }
    }
}
