package com.example.kaffacafeteria.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.launch

/**
 * Estado del formulario de registro.
 *
 * `pendienteVerificacion` representa el flujo real del backend: el registro
 * crea la cuenta y manda un correo de verificación, pero NO devuelve token.
 * La app debe mostrar ese estado en lugar de intentar iniciar sesión.
 */
data class RegisterUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val error: String? = null,
    /** Cuenta creada: hay que revisar el correo antes de poder entrar. */
    val pendienteVerificacion: Boolean = false,
    val correoRegistrado: String = "",
    val mensajeVerificacion: String? = null,
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

    /**
     * Valida contra la misma política del backend
     * (mínimo 8, mayúscula, minúscula, número y símbolo) para no gastar un
     * viaje de red con datos que el servidor rechazaría.
     */
    private fun validar(): Boolean {
        val s = uiState
        var hasError = false

        if (s.nombre.isBlank()) {
            uiState = uiState.copy(nombreError = "El nombre es requerido"); hasError = true
        }
        if (s.correo.isBlank()) {
            uiState = uiState.copy(correoError = "El correo es requerido"); hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.correo.trim()).matches()) {
            uiState = uiState.copy(correoError = "El correo no tiene un formato válido"); hasError = true
        }

        if (s.password.isBlank()) {
            uiState = uiState.copy(passwordError = "La contraseña es requerida"); hasError = true
        } else {
            val p = s.password
            val passError: String? = when {
                p.length < 8 -> "Mínimo 8 caracteres"
                !p.any { it.isUpperCase() } -> "Debe incluir mayúsculas"
                !p.any { it.isLowerCase() } -> "Debe incluir minúsculas"
                !p.any { it.isDigit() } -> "Debe incluir números"
                !p.any { !it.isLetterOrDigit() } -> "Debe incluir un símbolo"
                else -> null
            }
            if (passError != null) {
                uiState = uiState.copy(passwordError = passError); hasError = true
            }
        }

        if (s.confirmPassword.isBlank()) {
            uiState = uiState.copy(confirmPasswordError = "Confirma la contraseña"); hasError = true
        } else if (s.password != s.confirmPassword) {
            uiState = uiState.copy(confirmPasswordError = "Las contraseñas no coinciden"); hasError = true
        }

        return !hasError
    }

    fun register() {
        if (uiState.isLoading) return
        if (!validar()) return
        val s = uiState

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            when (val result = authRepository.register(s.nombre.trim(), s.correo.trim(), s.password)) {
                is Resource.Success -> {
                    // Sin sesión: el usuario debe verificar su correo primero.
                    uiState = uiState.copy(
                        isLoading = false,
                        pendienteVerificacion = true,
                        correoRegistrado = result.data.correo ?: s.correo.trim(),
                        mensajeVerificacion = result.data.mensaje,
                        password = "",
                        confirmPassword = ""
                    )
                }
                is Resource.Error -> {
                    uiState = uiState.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    /** Reenvía el enlace de verificación al correo recién registrado. */
    fun reenviarVerificacion() {
        val correo = uiState.correoRegistrado
        if (correo.isBlank() || uiState.isResending) return

        viewModelScope.launch {
            uiState = uiState.copy(isResending = true, error = null)
            when (val result = authRepository.reenviarVerificacion(correo)) {
                is Resource.Success ->
                    uiState = uiState.copy(isResending = false, mensajeVerificacion = result.data)
                is Resource.Error ->
                    uiState = uiState.copy(isResending = false, error = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    /** Vuelve al formulario tras la pantalla de confirmación. */
    fun volverAlFormulario() {
        uiState = RegisterUiState()
    }

    /** Permite corregir el correo y reintentar el registro. */
    fun editarCorreo() {
        uiState = uiState.copy(
            pendienteVerificacion = false,
            correoRegistrado = "",
            mensajeVerificacion = null,
            error = null
        )
    }
}
