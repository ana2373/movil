package com.example.kaffacafeteria.ui.gestion

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.ProveedorDto
import kotlinx.coroutines.launch

data class ProveedoresUiState(
    val proveedores: List<ProveedorDto> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProveedoresViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionApi = (application as KaffaApp).container.transactionApi

    var uiState by mutableStateOf(ProveedoresUiState())
        private set

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = transactionApi.getProveedores(perPage = 1000)
                uiState = uiState.copy(proveedores = response.body()?.data ?: emptyList(), isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun create(nombre: String, contacto: String?, telefono: String?, email: String?, direccion: String?) {
        viewModelScope.launch {
            uiState = uiState.copy(isCreating = true, error = null, successMessage = null)
            try {
                transactionApi.createProveedor(
                    ProveedorDto(
                        id = 0, nombre = nombre, contacto = contacto, telefono = telefono,
                        email = email, direccion = direccion, activo = true,
                        created_at = null, updated_at = null
                    )
                )
                uiState = uiState.copy(isCreating = false, successMessage = "Proveedor registrado")
                load()
            } catch (e: Exception) {
                uiState = uiState.copy(isCreating = false, error = e.message)
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, successMessage = null)
    }
}
