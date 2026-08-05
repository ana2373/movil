package com.example.kaffacafeteria.ui.gestion

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.GastoDto
import com.example.kaffacafeteria.data.remote.dto.MermaDto
import kotlinx.coroutines.launch

data class GestionUiState(
    val gastos: List<GastoDto> = emptyList(),
    val mermas: List<MermaDto> = emptyList(),
    val isLoadingGastos: Boolean = false,
    val isLoadingMermas: Boolean = false,
    val gastosError: String? = null,
    val mermasError: String? = null,
    val isCreatingGasto: Boolean = false,
    val isCreatingMerma: Boolean = false
)

class GestionViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionApi = (application as KaffaApp).container.transactionApi

    var uiState by mutableStateOf(GestionUiState())
        private set

    fun loadGastos() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingGastos = true, gastosError = null)
            try {
                val response = transactionApi.getGastos()
                if (response.isSuccessful) {
                    uiState = uiState.copy(gastos = response.body()?.data ?: emptyList(), isLoadingGastos = false)
                } else {
                    uiState = uiState.copy(isLoadingGastos = false, gastosError = "Error al cargar gastos")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingGastos = false, gastosError = e.message)
            }
        }
    }

    fun loadMermas() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingMermas = true, mermasError = null)
            try {
                val response = transactionApi.getMermas()
                if (response.isSuccessful) {
                    uiState = uiState.copy(mermas = response.body()?.data ?: emptyList(), isLoadingMermas = false)
                } else {
                    uiState = uiState.copy(isLoadingMermas = false, mermasError = "Error al cargar mermas")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingMermas = false, mermasError = e.message)
            }
        }
    }

    fun createGasto(descripcion: String, monto: Double) {
        viewModelScope.launch {
            uiState = uiState.copy(isCreatingGasto = true)
            try {
                transactionApi.createGasto(GastoDto(id = 0, descripcion = descripcion, monto = monto.toString(), categoria = null, cajaId = null, created_at = null, updated_at = null))
                uiState = uiState.copy(isCreatingGasto = false)
                loadGastos()
            } catch (e: Exception) {
                uiState = uiState.copy(isCreatingGasto = false, gastosError = e.message)
            }
        }
    }

    fun createMerma(descripcion: String, monto: Double) {
        viewModelScope.launch {
            uiState = uiState.copy(isCreatingMerma = true)
            try {
                transactionApi.createMerma(MermaDto(id = 0, descripcion = descripcion, cantidad = monto.toString(), insumoId = null, insumo = null, motivo = null, created_at = null, updated_at = null))
                uiState = uiState.copy(isCreatingMerma = false)
                loadMermas()
            } catch (e: Exception) {
                uiState = uiState.copy(isCreatingMerma = false, mermasError = e.message)
            }
        }
    }
}
