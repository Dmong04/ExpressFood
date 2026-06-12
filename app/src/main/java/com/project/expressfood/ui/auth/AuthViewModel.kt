package com.project.expressfood.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.expressfood.data.repository.AuthRepository
import com.project.expressfood.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    /** Escucha cambios en Firebase Auth y actualiza el StateFlow. */
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { firebaseUser ->
                _authState.value = if (firebaseUser != null) {
                    try {
                        val user = authRepository.getOrCreateUser(firebaseUser)
                        AuthState.Authenticated(user)
                    } catch (e: Exception) {
                        AuthState.Error(e.message ?: "Error al cargar perfil")
                    }
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    fun signOut() = authRepository.signOut()


    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _loginError.value  = null
            try {
                authRepository.signInWithGoogle(context)
                // No necesitas hacer nada aquí:
                // observeAuthState() detecta el cambio de Firebase y emite AuthState.Authenticated
            } catch (e: Exception) {
                _loginError.value = e.message ?: "Error al iniciar sesión"
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    fun clearLoginError() { _loginError.value = null }

    /** Factory para inyección manual de dependencias (sin Hilt). */
    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(authRepository) as T
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
