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
import org.json.JSONObject
import retrofit2.Response

data class CashRegisterUiState(
    val cajaActual: CajaDto? = null,
    val historial: List<CajaDto> = emptyList(),
    val movimientos: List<MovimientoCajaDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
) {
    val isOpen: Boolean get() = cajaActual?.estaAbierta == true
    val totalIngresos: Double get() = cajaActual?.totalIngresos ?: 0.0
    val totalEgresos: Double get() = cajaActual?.totalEgresos ?: 0.0
    val saldoActual: Double get() = cajaActual?.saldoActual ?: 0.0
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
                val response = cashRegisterApi.getCajas(perPage = 100)
                if (response.isSuccessful) {
                    val cajas = response.body()?.data ?: emptyList()
                    val abierta = cajas.firstOrNull { it.estaAbierta }
                    uiState = uiState.copy(
                        cajaActual = abierta,
                        historial = cajas,
                        movimientos = abierta?.movimientos ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    uiState = uiState.copy(isLoading = false, error = errorFrom(response, "Error al cargar cajas"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun openCashRegister(montoAperturaFisico: Double) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = cashRegisterApi.createCaja(
                    CajaRequest(montoAperturaFisico = montoAperturaFisico)
                )
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, success = "Caja abierta")
                    loadStatus()
                } else {
                    uiState = uiState.copy(isLoading = false, error = errorFrom(response, "Error al abrir caja"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error al abrir caja")
            }
        }
    }

    fun closeCashRegister(montoCierreFisico: Double) {
        val cajaId = uiState.cajaActual?.id ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = cashRegisterApi.cerrarCaja(
                    cajaId,
                    CajaCierreRequest(montoCierreFisico = montoCierreFisico)
                )
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, success = "Caja cerrada")
                    loadStatus()
                } else {
                    uiState = uiState.copy(isLoading = false, error = errorFrom(response, "Error al cerrar caja"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error al cerrar caja")
            }
        }
    }

    fun clearMessages() { uiState = uiState.copy(error = null, success = null) }

    private fun errorFrom(response: Response<*>, fallback: String): String {
        val code = response.code()
        val body = runCatching { response.errorBody()?.string() }.getOrNull()
        val serverMsg = body?.let { raw ->
            runCatching {
                val json = JSONObject(raw)
                when {
                    json.has("message") -> {
                        val m = json.optString("message").takeIf { it.isNotBlank() && it != "Server Error" }
                        m ?: json.optString("exception").takeIf { it.isNotBlank() }
                    }
                    json.has("exception") -> json.optString("exception").takeIf { it.isNotBlank() }
                    else -> null
                }?.take(600)
            }.getOrNull() ?: body.takeIf { it.isNotBlank() }?.take(600)
        }
        return if (!serverMsg.isNullOrBlank()) "$fallback (código $code): $serverMsg"
        else "$fallback (código $code)"
    }
}
