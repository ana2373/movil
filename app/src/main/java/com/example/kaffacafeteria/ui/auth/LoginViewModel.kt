package com.example.kaffacafeteria.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val correoError: String? = null,
    val passwordError: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as KaffaApp).container.authRepository

    var uiState by mutableStateOf(LoginUiState())
        private set

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                val meResult = authRepository.getMe()
                if (meResult is Resource.Success) {
                    uiState = uiState.copy(isLoggedIn = true, user = meResult.data)
                }
            }
        }
    }

    fun updateCorreo(value: String) {
        uiState = uiState.copy(correo = value, correoError = null, error = null)
    }

    fun updatePassword(value: String) {
        uiState = uiState.copy(password = value, passwordError = null, error = null)
    }

    fun login() {
        val correo = uiState.correo.trim()
        val password = uiState.password

        var hasError = false
        if (correo.isBlank()) {
            uiState = uiState.copy(correoError = "El correo es requerido")
            hasError = true
        }
        if (password.isBlank()) {
            uiState = uiState.copy(passwordError = "La contraseña es requerida")
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            when (val result = authRepository.login(correo, password)) {
                is Resource.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        user = result.data,
                        isLoggedIn = true
                    )
                }
                is Resource.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            uiState = LoginUiState()
        }
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
