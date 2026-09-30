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
 * Estados del flujo de recuperación de contraseña.
 *
 * Paso 1: el usuario escribe su correo y la app pide el enlace.
 * Paso 2: abre el enlace (app o web) y se guarda la contraseña nueva.
 *
 * El backend responde siempre igual para no revelar qué correos existen, así
 * que el mensaje de éxito se muestra tanto si el correo está registrado como
 * si no.
 */
data class ForgotPasswordUiState(
    val correo: String = "",
    val isLoading: Boolean = false,
    val correoError: String? = null,
    val error: String? = null,

    // Paso 1
    val enlaceEnviado: Boolean = false,
    val mensajeEnvio: String? = null,

    // Paso 2 (precargado desde el deep link o escrito a mano)
    val token: String = "",
    val nuevaPassword: String = "",
    val confirmarPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmarVisible: Boolean = false,
    val passwordError: String? = null,
    val confirmarError: String? = null,
    val esEnviadoExternamente: Boolean = false,
    val resetExitoso: Boolean = false,
    val mensajeReset: String? = null
)

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as KaffaApp).container.authRepository

    var uiState by mutableStateOf(ForgotPasswordUiState())
        private set

    /**
     * Precarga los datos que llegan por el deep link `kaffa://reset-password`.
     *
     * @param token  token de un solo uso del correo
     * @param correo correo del usuario
     */
    fun precargarDesdeDeepLink(token: String?, correo: String?) {
        if (token.isNullOrBlank()) return
        uiState = uiState.copy(
            token = token,
            correo = correo.orEmpty().ifBlank { uiState.correo },
            esEnviadoExternamente = true
        )
    }

    fun updateCorreo(value: String) { uiState = uiState.copy(correo = value, correoError = null, error = null) }
    fun updateToken(value: String) { uiState = uiState.copy(token = value, error = null) }
    fun updatePassword(value: String) { uiState = uiState.copy(nuevaPassword = value, passwordError = null, error = null) }
    fun updateConfirmar(value: String) { uiState = uiState.copy(confirmarPassword = value, confirmarError = null, error = null) }
    fun togglePasswordVisible() { uiState = uiState.copy(passwordVisible = !uiState.passwordVisible) }
    fun toggleConfirmarVisible() { uiState = uiState.copy(confirmarVisible = !uiState.confirmarVisible) }

    /** Paso 1: solicita el enlace de restablecimiento. */
    fun solicitarEnlace() {
        val correo = uiState.correo.trim()
        if (correo.isBlank()) {
            uiState = uiState.copy(correoError = "El correo es requerido")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            uiState = uiState.copy(correoError = "El correo no tiene un formato válido")
            return
        }
        if (uiState.isLoading) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            when (val result = authRepository.forgotPassword(correo)) {
                is Resource.Success -> uiState = uiState.copy(
                    isLoading = false,
                    enlaceEnviado = true,
                    mensajeEnvio = result.data
                )
                is Resource.Error -> uiState = uiState.copy(
                    isLoading = false,
                    error = result.message
                )
                is Resource.Loading -> {}
            }
        }
    }

    /** Paso 2: guarda la nueva contraseña. */
    fun restablecer() {
        val s = uiState
        var hasError = false

        if (s.token.isBlank()) {
            uiState = uiState.copy(error = "El enlace es inválido o expiró. Solicita uno nuevo.")
            hasError = true
        }
        if (s.correo.isBlank()) {
            uiState = uiState.copy(correoError = "El correo es requerido")
            hasError = true
        }

        val passError = when {
            s.nuevaPassword.isBlank() -> "La contraseña es requerida"
            s.nuevaPassword.length < 8 -> "Mínimo 8 caracteres"
            !s.nuevaPassword.any { it.isUpperCase() } -> "Debe incluir mayúsculas"
            !s.nuevaPassword.any { it.isLowerCase() } -> "Debe incluir minúsculas"
            !s.nuevaPassword.any { it.isDigit() } -> "Debe incluir números"
            !s.nuevaPassword.any { !it.isLetterOrDigit() } -> "Debe incluir un símbolo"
            else -> null
        }
        if (passError != null) {
            uiState = uiState.copy(passwordError = passError)
            hasError = true
        }

        if (s.confirmarPassword.isBlank()) {
            uiState = uiState.copy(confirmarError = "Confirma la contraseña")
            hasError = true
        } else if (s.nuevaPassword != s.confirmarPassword) {
            uiState = uiState.copy(confirmarError = "Las contraseñas no coinciden")
            hasError = true
        }

        if (hasError || s.isLoading) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            when (val result = authRepository.resetPassword(s.token.trim(), s.correo.trim(), s.nuevaPassword)) {
                is Resource.Success -> uiState = uiState.copy(
                    isLoading = false,
                    resetExitoso = true,
                    mensajeReset = result.data,
                    nuevaPassword = "",
                    confirmarPassword = ""
                )
                is Resource.Error -> uiState = uiState.copy(
                    isLoading = false,
                    error = result.message
                )
                is Resource.Loading -> {}
            }
        }
    }

    /** Reintenta el paso 1 desde la pantalla de "nueva contraseña". */
    fun volverASolicitarEnlace() {
        uiState = ForgotPasswordUiState(correo = uiState.correo)
    }
}
