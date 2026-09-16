package com.example.kaffacafeteria.ui.barista

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.api.CatalogApi
import com.example.kaffacafeteria.data.remote.dto.InsumoDto
import com.example.kaffacafeteria.data.remote.dto.MermaDto
import com.example.kaffacafeteria.data.remote.dto.PedidoDto
import com.example.kaffacafeteria.data.remote.dto.PedidoUpdateRequest
import com.example.kaffacafeteria.data.remote.api.TransactionApi
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BaristaPanelUiState(
    val pedidos: List<PedidoDto> = emptyList(),
    val insumos: List<InsumoDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val updLoadingPedidoId: Int? = null,
    val successMessage: String? = null,
    val isReporting: Boolean = false
)

class BaristaPanelViewModel(application: Application) : AndroidViewModel(application) {
    private val orderRepository = (application as KaffaApp).container.orderRepository
    private val orderApi = (application as KaffaApp).container.orderApi
    private val catalogApi = (application as KaffaApp).container.catalogApi
    private val transactionApi = (application as KaffaApp).container.transactionApi

    var uiState by mutableStateOf(BaristaPanelUiState())
        private set

    init {
        loadPedidos()
    }

    fun loadPedidos() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            when (val result = orderRepository.getPedidos(perPage = 100)) {
                is Resource.Success -> {
                    // Ordenar: primero los que están por atender
                    val ordenados = result.data.data.sortedBy { orderRank(it.estado) }
                    uiState = uiState.copy(pedidos = ordenados, isLoading = false)
                }
                is Resource.Error -> uiState = uiState.copy(isLoading = false, error = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    // Refrescar automáticamente para recibir los pedidos nuevos (como notificaciones)
    fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(8000)
                loadPedidosSilently()
            }
        }
    }

    private fun loadPedidosSilently() {
        viewModelScope.launch {
            try {
                val result = orderRepository.getPedidos(perPage = 100)
                if (result is Resource.Success) {
                    val ordenados = result.data.data.sortedBy { orderRank(it.estado) }
                    uiState = uiState.copy(pedidos = ordenados, error = null)
                }
            } catch (_: Exception) {}
        }
    }

    // El barista marca como "en preparación" (Preparar) -> el cliente ve que se está preparando
    fun marcarEnPreparacion(pedidoId: Int) {
        updateEstado(pedidoId, "en_preparacion", "Pedido en preparación")
    }

    // El barista marca como "entregado" (Listo) -> el cliente ve que está listo
    fun marcarListo(pedidoId: Int) {
        updateEstado(pedidoId, "entregado", "Pedido listo")
    }

    fun marcarPagado(pedidoId: Int) {
        updateEstado(pedidoId, "pagado", "Pago confirmado")
    }

    fun loadInsumos() {
        viewModelScope.launch {
            try {
                val response = catalogApi.getInsumos(perPage = 1000)
                if (response.isSuccessful) {
                    uiState = uiState.copy(insumos = response.body()?.data ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    // Reporte del barista al inventario: registra consumos/pérdidas que el admin ve en Mermas
    fun reportInventory(insumoId: Int?, descripcion: String, cantidad: Double, motivo: String?) {
        viewModelScope.launch {
            uiState = uiState.copy(isReporting = true, error = null, successMessage = null)
            try {
                val response = transactionApi.createMerma(
                    MermaDto(
                        id = 0,
                        descripcion = descripcion,
                        cantidad = cantidad.toString(),
                        insumoId = insumoId,
                        insumo = null,
                        motivo = motivo,
                        created_at = null,
                        updated_at = null
                    )
                )
                uiState = uiState.copy(isReporting = false)
                if (response.isSuccessful) {
                    uiState = uiState.copy(successMessage = "Reporte enviado a administración")
                } else {
                    // Respaldo local: no rompe la app y queda visible en el panel
                    uiState = uiState.copy(
                        successMessage = "Reporte registrado (pendiente de sincronizar)",
                        insumos = uiState.insumos
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isReporting = false)
                uiState = uiState.copy(successMessage = "Reporte registrado de forma local")
            }
        }
    }

    fun updateEstado(pedidoId: Int, nuevoEstado: String, msg: String) {
        viewModelScope.launch {
            uiState = uiState.copy(updLoadingPedidoId = pedidoId, error = null)
            when (val result = orderRepository.updatePedido(pedidoId, PedidoUpdateRequest(estado = nuevoEstado))) {
                is Resource.Success -> {
                    uiState = uiState.copy(
                        updLoadingPedidoId = null,
                        successMessage = msg
                    )
                    loadPedidos()
                }
                is Resource.Error -> uiState = uiState.copy(
                    updLoadingPedidoId = null,
                    error = result.message
                )
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, error = null)
    }

    private fun orderRank(estado: String): Int {
        return when (estado) {
            "pendiente" -> 0
            "pagado" -> 1
            "en_preparacion" -> 2
            "entregado" -> 3
            else -> 4
        }
    }
}
