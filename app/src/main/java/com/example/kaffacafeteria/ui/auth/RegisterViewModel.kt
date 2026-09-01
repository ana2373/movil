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

data class RegisterUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false,
    val user: User? = null,
    val nombreError: String? = null,
    val correoError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as KaffaApp).container.authRepository

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
        if (s.password.isBlank()) {
            uiState = uiState.copy(passwordError = "La contraseña es requerida"); hasError = true
        } else {
            val p = s.password
            var passError: String? = null
            if (p.length < 8) passError = "Mínimo 8 caracteres"
            else if (!p.any { it.isUpperCase() }) passError = "Debe incluir mayúsculas"
            else if (!p.any { it.isLowerCase() }) passError = "Debe incluir minúsculas"
            else if (!p.any { it.isDigit() }) passError = "Debe incluir números"
            else if (!p.any { !it.isLetterOrDigit() }) passError = "Debe incluir un símbolo"
            if (passError != null) { uiState = uiState.copy(passwordError = passError); hasError = true }
        }
        if (s.confirmPassword.isBlank()) { uiState = uiState.copy(confirmPasswordError = "Confirma la contraseña"); hasError = true }
        if (s.password != s.confirmPassword) { uiState = uiState.copy(confirmPasswordError = "Las contraseñas no coinciden"); hasError = true }
        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            when (val result = authRepository.register(s.nombre.trim(), s.correo.trim(), s.password)) {
                is Resource.Success -> {
                    uiState = uiState.copy(isLoading = false, isRegistered = true, user = result.data)
                }
                is Resource.Error -> {
                    uiState = uiState.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}
