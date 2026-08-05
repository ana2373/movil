package com.example.kaffacafeteria.ui.barista

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.*
import kotlinx.coroutines.launch

data class CashRegisterUiState(
    val caja: CajaDto? = null,
    val movimientos: List<MovimientoCajaDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
) {
    val isOpen: Boolean get() = caja?.estado == "abierta"
}

class CashRegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val cashRegisterApi = (application as KaffaApp).container.cashRegisterApi

    var uiState by mutableStateOf(CashRegisterUiState())
        private set

    init { loadStatus() }

    fun loadStatus() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = cashRegisterApi.getCajas(estado = "abierta")
                if (response.isSuccessful) {
                    val cajas = response.body()?.data ?: emptyList()
                    val caja = cajas.firstOrNull()
                    uiState = uiState.copy(caja = caja, isLoading = false)
                    if (caja != null) loadMovements(caja.id)
                } else {
                    uiState = uiState.copy(isLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    private suspend fun loadMovements(cajaId: Int) {
        try {
            val response = cashRegisterApi.getMovimientos(cajaId = cajaId)
            if (response.isSuccessful) {
                uiState = uiState.copy(movimientos = response.body()?.data ?: emptyList())
            }
        } catch (_: Exception) {}
    }

    fun openCashRegister(montoInicial: Double) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = cashRegisterApi.createCaja(CajaRequest(montoInicial = montoInicial))
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, success = "Caja abierta")
                    loadStatus()
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error al abrir caja")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun closeCashRegister(montoFinal: Double) {
        val cajaId = uiState.caja?.id ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = cashRegisterApi.cerrarCaja(cajaId, CajaCierreRequest(montoFinal = montoFinal))
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, success = "Caja cerrada")
                    loadStatus()
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error al cerrar caja")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearMessages() { uiState = uiState.copy(error = null, success = null) }
}
