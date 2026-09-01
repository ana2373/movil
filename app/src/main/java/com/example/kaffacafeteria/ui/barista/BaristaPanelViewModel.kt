package com.example.kaffacafeteria.ui.barista

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.PedidoDto
import com.example.kaffacafeteria.data.remote.dto.PedidoUpdateRequest
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BaristaPanelUiState(
    val pedidos: List<PedidoDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val updLoadingPedidoId: Int? = null,
    val successMessage: String? = null
)

class BaristaPanelViewModel(application: Application) : AndroidViewModel(application) {
    private val orderRepository = (application as KaffaApp).container.orderRepository
    private val orderApi = (application as KaffaApp).container.orderApi

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

    private fun updateEstado(pedidoId: Int, nuevoEstado: String, msg: String) {
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
