package com.example.kaffacafeteria.ui.profile

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

data class ProfileUiState(
    val user: User? = null,
<<<<<<< HEAD
    val isLoading: Boolean = false,

    // Edición de datos
    val editingNombre: String = "",
    val editingCorreo: String = "",
    val editNombreError: String? = null,
    val editCorreoError: String? = null,
    val isSavingProfile: Boolean = false,
    val isUploadingPhoto: Boolean = false,

    // Cambio de contraseña (solo admin)
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val newPasswordError: String? = null,
    val confirmNewPasswordError: String? = null,
    val isSavingPassword: Boolean = false,

    val successMessage: String? = null,
    val errorMessage: String? = null
=======
    val isLoading: Boolean = false
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as KaffaApp).container.authRepository

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            when (val result = authRepository.getMe()) {
<<<<<<< HEAD
                is Resource.Success -> {
                    val u = result.data
                    uiState = uiState.copy(
                        user = u,
                        isLoading = false,
                        editingNombre = u.nombre,
                        editingCorreo = u.correo
                    )
                }
=======
                is Resource.Success -> uiState = uiState.copy(user = result.data, isLoading = false)
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
                else -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

<<<<<<< HEAD
    val isAdmin: Boolean get() = uiState.user?.isAdmin == true

    fun updateEditingNombre(value: String) {
        uiState = uiState.copy(editingNombre = value, editNombreError = null, successMessage = null, errorMessage = null)
    }

    fun updateEditingCorreo(value: String) {
        uiState = uiState.copy(editingCorreo = value, editCorreoError = null, successMessage = null, errorMessage = null)
    }

    fun updateNewPassword(value: String) {
        uiState = uiState.copy(newPassword = value, newPasswordError = null, successMessage = null, errorMessage = null)
    }

    fun updateConfirmNewPassword(value: String) {
        uiState = uiState.copy(confirmNewPassword = value, confirmNewPasswordError = null, successMessage = null, errorMessage = null)
    }

    fun saveProfile() {
        val nombre = uiState.editingNombre.trim()
        val correo = uiState.editingCorreo.trim()
        var hasError = false

        if (nombre.isBlank()) { uiState = uiState.copy(editNombreError = "El nombre es requerido"); hasError = true }
        if (correo.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            uiState = uiState.copy(editCorreoError = "Ingresa un correo válido"); hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isSavingProfile = true, errorMessage = null, successMessage = null)
            when (val result = authRepository.updateProfile(nombre, correo, password = null)) {
                is Resource.Success -> {
                    uiState = uiState.copy(
                        user = result.data,
                        editingNombre = result.data.nombre,
                        editingCorreo = result.data.correo,
                        isSavingProfile = false,
                        successMessage = "Perfil actualizado correctamente"
                    )
                }
                is Resource.Error -> uiState = uiState.copy(isSavingProfile = false, errorMessage = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun changePassword() {
        val newPass = uiState.newPassword
        val confirm = uiState.confirmNewPassword
        var hasError = false

        if (newPass.isBlank()) {
            uiState = uiState.copy(newPasswordError = "La contraseña es requerida"); hasError = true
        } else {
            val p = newPass
            var passError: String? = null
            if (p.length < 8) passError = "Mínimo 8 caracteres"
            else if (!p.any { it.isUpperCase() }) passError = "Debe incluir mayúsculas"
            else if (!p.any { it.isLowerCase() }) passError = "Debe incluir minúsculas"
            else if (!p.any { it.isDigit() }) passError = "Debe incluir números"
            else if (!p.any { it.isLetterOrDigit() }) passError = "Debe incluir un símbolo"
            if (passError != null) { uiState = uiState.copy(newPasswordError = passError); hasError = true }
        }
        if (confirm.isBlank()) { uiState = uiState.copy(confirmNewPasswordError = "Confirma la contraseña"); hasError = true }
        if (confirm != newPass) { uiState = uiState.copy(confirmNewPasswordError = "Las contraseñas no coinciden"); hasError = true }
        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isSavingPassword = true, errorMessage = null, successMessage = null)
            when (val result = authRepository.updateProfile(uiState.editingNombre, uiState.editingCorreo, password = newPass)) {
                is Resource.Success -> {
                    uiState = uiState.copy(
                        user = result.data,
                        isSavingPassword = false,
                        newPassword = "",
                        confirmNewPassword = "",
                        successMessage = "Contraseña actualizada correctamente"
                    )
                }
                is Resource.Error -> uiState = uiState.copy(isSavingPassword = false, errorMessage = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }

    fun updateError(message: String) {
        uiState = uiState.copy(errorMessage = message)
    }

    fun subirFoto(fotoPath: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isUploadingPhoto = true, errorMessage = null, successMessage = null)
            when (val result = authRepository.subirFoto(fotoPath)) {
                is Resource.Success -> {
                    uiState = uiState.copy(
                        user = result.data,
                        isUploadingPhoto = false,
                        successMessage = "Foto de perfil actualizada"
                    )
                }
                is Resource.Error -> uiState = uiState.copy(isUploadingPhoto = false, errorMessage = result.message)
                is Resource.Loading -> {}
            }
        }
    }

=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
