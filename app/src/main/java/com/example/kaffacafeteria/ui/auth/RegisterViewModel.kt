package com.example.kaffacafeteria.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.UsuarioCreateRequest
import kotlinx.coroutines.launch

data class RegisterUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false,
    val nombreError: String? = null,
    val correoError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val userApi = (application as KaffaApp).container.userApi

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun updateNombre(value: String) { uiState = uiState.copy(nombre = value, nombreError = null, error = null) }
    fun updateCorreo(value: String) { uiState = uiState.copy(correo = value, correoError = null, error = null) }
    fun updatePassword(value: String) { uiState = uiState.copy(password = value, passwordError = null, error = null) }
    fun updateConfirmPassword(value: String) { uiState = uiState.copy(confirmPassword = value, confirmPasswordError = null, error = null) }

    fun register() {
        val s = uiState
        var hasError = false

        if (s.nombre.isBlank()) { uiState = uiState.copy(nombreError = "El nombre es requerido"); hasError = true }
        if (s.correo.isBlank()) { uiState = uiState.copy(correoError = "El correo es requerido"); hasError = true }
        if (s.password.isBlank()) { uiState = uiState.copy(passwordError = "La contraseña es requerida"); hasError = true }
        if (s.password.length < 6) { uiState = uiState.copy(passwordError = "Mínimo 6 caracteres"); hasError = true }
        if (s.confirmPassword.isBlank()) { uiState = uiState.copy(confirmPasswordError = "Confirma la contraseña"); hasError = true }
        if (s.password != s.confirmPassword) { uiState = uiState.copy(confirmPasswordError = "Las contraseñas no coinciden"); hasError = true }
        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = userApi.createUsuario(
                    UsuarioCreateRequest(
                        nombre = s.nombre,
                        correo = s.correo,
                        password = s.password,
                        passwordConfirmation = s.confirmPassword
                    )
                )
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, isRegistered = true)
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error al registrarse: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error de conexión")
            }
        }
    }
}
