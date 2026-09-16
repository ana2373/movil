package com.example.kaffacafeteria.ui.gestion

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.InsumoDto
import com.example.kaffacafeteria.data.remote.dto.InsumoRequest
import kotlinx.coroutines.launch

data class InsumosUiState(
    val insumos: List<InsumoDto> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class InsumosViewModel(application: Application) : AndroidViewModel(application) {
    private val catalogApi = (application as KaffaApp).container.catalogApi

    var uiState by mutableStateOf(InsumosUiState())
        private set

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = catalogApi.getInsumos(perPage = 1000)
                uiState = uiState.copy(insumos = response.body()?.data ?: emptyList(), isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun create(nombre: String, stock: Double?, unidadMedida: String?) {
        viewModelScope.launch {
            uiState = uiState.copy(isCreating = true, error = null, successMessage = null)
            try {
                val response = catalogApi.createInsumo(InsumoRequest(nombre = nombre, stockActual = stock, unidadMedida = unidadMedida))
                if (response.isSuccessful) {
                    uiState = uiState.copy(isCreating = false, successMessage = "Insumo creado")
                    load()
                } else {
                    uiState = uiState.copy(isCreating = false, error = "Error al crear insumo")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isCreating = false, error = e.message)
            }
        }
    }

    fun update(id: Int, nombre: String, stock: Double?, unidadMedida: String?) {
        viewModelScope.launch {
            uiState = uiState.copy(isCreating = true, error = null, successMessage = null)
            try {
                val response = catalogApi.updateInsumo(id, InsumoRequest(nombre = nombre, stockActual = stock, unidadMedida = unidadMedida))
                if (response.isSuccessful) {
                    uiState = uiState.copy(isCreating = false, successMessage = "Insumo actualizado")
                    load()
                } else {
                    uiState = uiState.copy(isCreating = false, error = "Error al actualizar insumo")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isCreating = false, error = e.message)
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            try {
                catalogApi.deleteInsumo(id)
                load()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, successMessage = null)
    }
}