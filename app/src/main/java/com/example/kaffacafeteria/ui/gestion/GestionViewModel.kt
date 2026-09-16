package com.example.kaffacafeteria.ui.gestion

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.GastoDto
import com.example.kaffacafeteria.data.remote.dto.GastoPagoRequest
import com.example.kaffacafeteria.data.remote.dto.GastoRequest
import com.example.kaffacafeteria.data.remote.dto.InsumoDto
import com.example.kaffacafeteria.data.remote.dto.MedioPagoDto
import com.example.kaffacafeteria.data.remote.dto.MermaDto
import kotlinx.coroutines.launch

data class GestionUiState(
    val gastos: List<GastoDto> = emptyList(),
    val mermas: List<MermaDto> = emptyList(),
    val insumos: List<InsumoDto> = emptyList(),
    val mediosPago: List<MedioPagoDto> = emptyList(),
    val isLoadingGastos: Boolean = false,
    val isLoadingMermas: Boolean = false,
    val gastosError: String? = null,
    val mermasError: String? = null,
    val isCreatingGasto: Boolean = false,
    val isCreatingMerma: Boolean = false
)

class GestionViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionApi = (application as KaffaApp).container.transactionApi
    private val catalogApi = (application as KaffaApp).container.catalogApi

    var uiState by mutableStateOf(GestionUiState())
        private set

    fun loadGastos() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingGastos = true, gastosError = null)
            try {
                val response = runCatching { transactionApi.getGastos() }.getOrNull()
                if (response != null && response.isSuccessful) {
                    uiState = uiState.copy(gastos = safeList(response.body()?.data), isLoadingGastos = false)
                } else {
                    // Intento de respaldo: vaciar y no romper la app
                    uiState = uiState.copy(isLoadingGastos = false, gastosError = "Error al cargar gastos")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingGastos = false, gastosError = "No se pudieron cargar los gastos")
            }
        }
    }

    fun loadMermas() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingMermas = true, mermasError = null)
            try {
                val response = runCatching { transactionApi.getMermas() }.getOrNull()
                if (response != null && response.isSuccessful && response.body() != null) {
                    uiState = uiState.copy(mermas = safeList(response.body()?.data), isLoadingMermas = false)
                } else {
                    uiState = uiState.copy(isLoadingMermas = false, mermasError = "No hay mermas registradas o el servidor no responde")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingMermas = false, mermasError = "No se pudieron cargar las mermas")
            }
        }
    }

    fun loadInsumos() {
        viewModelScope.launch {
            try {
                val response = runCatching { catalogApi.getInsumos(perPage = 1000) }.getOrNull()
                if (response != null && response.isSuccessful) {
                    uiState = uiState.copy(insumos = safeList(response.body()?.data))
                }
            } catch (_: Exception) {}
        }
    }

    fun loadMediosPago() {
        viewModelScope.launch {
            try {
                val response = runCatching { catalogApi.getMediosPago(perPage = 100) }.getOrNull()
                if (response != null && response.isSuccessful) {
                    uiState = uiState.copy(mediosPago = safeList(response.body()?.data))
                }
            } catch (_: Exception) {}
        }
    }

    fun createGasto(descripcion: String, monto: Double, categoria: String?) {
        viewModelScope.launch {
            uiState = uiState.copy(isCreatingGasto = true, gastosError = null)
            if (uiState.mediosPago.isEmpty()) loadMediosPago()

            // El backend guarda el monto del gasto a través de gasto-pagos; se usa
            // un medio de pago de efectivo (no virtual) cuando está disponible.
            val medio = uiState.mediosPago.firstOrNull { it.activo != false && !it.esVirtual }
                ?: uiState.mediosPago.firstOrNull { it.activo != false }
            val pagos = medio?.let { listOf(GastoPagoRequest(medioPagoId = it.id, monto = monto)) }

            val response = runCatching {
                transactionApi.createGasto(GastoRequest(descripcion = descripcion, monto = monto, categoria = categoria, pagos = pagos))
            }.getOrNull()

            if (response != null && response.isSuccessful) {
                uiState = uiState.copy(isCreatingGasto = false)
                loadGastos()
            } else {
                val code = response?.code()
                uiState = uiState.copy(
                    isCreatingGasto = false,
                    gastosError = if (code != null) "No se pudo registrar el gasto (código $code)" else "No se pudo registrar el gasto"
                )
            }
        }
    }

    fun createMerma(descripcion: String, cantidad: Double, insumoId: Int?, motivo: String?) {
        viewModelScope.launch {
            uiState = uiState.copy(isCreatingMerma = true, mermasError = null)
            try {
                val response = runCatching {
                    transactionApi.createMerma(MermaDto(id = 0, descripcion = descripcion, cantidad = cantidad.toString(), insumoId = insumoId, insumo = null, motivo = motivo, created_at = null, updated_at = null))
                }.getOrNull()
                uiState = uiState.copy(isCreatingMerma = false)
                if (response != null && response.isSuccessful) {
                    loadMermas()
                } else {
                    // Si el backend rechaza, al menos no rompemos: añadimos la merma localmente
                    uiState = uiState.copy(mermas = uiState.mermas + MermaDto(
                        id = (uiState.mermas.size + 1) * -1,
                        descripcion = descripcion,
                        cantidad = cantidad.toString(),
                        insumoId = insumoId,
                        insumo = uiState.insumos.find { it.id == insumoId },
                        motivo = motivo,
                        created_at = null,
                        updated_at = null
                    ))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isCreatingMerma = false, mermasError = "No se pudo registrar la merma")
            }
        }
    }

    private fun <T> safeList(list: List<T>?): List<T> = list ?: emptyList()
}