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
    val passwordError: String? = null,
    /**
     * El backend rechaza el acceso mientras el correo no esté verificado
     * (código EMAIL_NOT_VERIFIED). Se muestra un botón para reenviar el enlace.
     */
    val emailSinVerificar: Boolean = false,
    val isResending: Boolean = false,
    val mensajeVerificacion: String? = null
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
        uiState = uiState.copy(correo = value, correoError = null, error = null, emailSinVerificar = false, mensajeVerificacion = null)
    }

    fun updatePassword(value: String) {
        uiState = uiState.copy(password = value, passwordError = null, error = null, emailSinVerificar = false)
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
            uiState = uiState.copy(isLoading = true, error = null, emailSinVerificar = false, mensajeVerificacion = null)
            when (val result = authRepository.login(correo, password)) {
                is Resource.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        user = result.data,
                        isLoggedIn = true
                    )
                }
                is Resource.Error -> {
                    // El backend impide el acceso hasta verificar el correo y
                    // lo indica con el código EMAIL_NOT_VERIFIED.
                    val sinVerificar = result.codigoApi == "EMAIL_NOT_VERIFIED"
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message,
                        emailSinVerificar = sinVerificar
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    /** Reenvía el correo de verificación tras un intento de acceso fallido. */
    fun reenviarVerificacion() {
        val correo = uiState.correo.trim()
        if (correo.isBlank() || uiState.isResending) return

        viewModelScope.launch {
            uiState = uiState.copy(isResending = true, error = null)
            when (val result = authRepository.reenviarVerificacion(correo)) {
                is Resource.Success -> uiState = uiState.copy(
                    isResending = false,
                    mensajeVerificacion = result.data
                )
                is Resource.Error -> uiState = uiState.copy(
                    isResending = false,
                    error = result.message
                )
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
