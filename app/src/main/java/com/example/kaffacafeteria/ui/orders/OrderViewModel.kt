package com.example.kaffacafeteria.ui.orders

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.*
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.launch

data class OrderListUiState(
    val pedidos: List<PedidoDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: String? = null,
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val user: User? = null
)

data class OrderDetailUiState(
    val pedido: PedidoDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusUpdateLoading: Boolean = false,
    val statusUpdateError: String? = null,
    val statusUpdateSuccess: String? = null
)

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val orderRepository = (application as KaffaApp).container.orderRepository
    private val orderApi = (application as KaffaApp).container.orderApi
    private val authRepository = (application as KaffaApp).container.authRepository

    var listState by mutableStateOf(OrderListUiState())
        private set
    var detailState by mutableStateOf(OrderDetailUiState())
        private set

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            when (val result = authRepository.getMe()) {
                is Resource.Success -> listState = listState.copy(user = result.data)
                else -> {}
            }
        }
    }

    fun loadPedidos(filter: String? = null, page: Int = 1) {
        viewModelScope.launch {
            listState = listState.copy(isLoading = true, selectedFilter = filter)
            when (val result = orderRepository.getPedidos(
                perPage = 15,
                estado = filter
            )) {
                is Resource.Success -> {
                    listState = listState.copy(
                        pedidos = result.data.data,
                        currentPage = result.data.meta.currentPage,
                        lastPage = result.data.meta.lastPage,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    listState = listState.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadPedidoDetail(id: Int) {
        viewModelScope.launch {
            detailState = OrderDetailUiState(isLoading = true)
            when (val result = orderRepository.getPedido(id)) {
                is Resource.Success -> {
                    detailState = OrderDetailUiState(pedido = result.data)
                }
                is Resource.Error -> {
                    detailState = OrderDetailUiState(error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateEstado(pedidoId: Int, nuevoEstado: String) {
        viewModelScope.launch {
            detailState = detailState.copy(statusUpdateLoading = true, statusUpdateError = null)
            val request = PedidoUpdateRequest(estado = nuevoEstado)
            when (val result = orderRepository.updatePedido(pedidoId, request)) {
                is Resource.Success -> {
                    detailState = detailState.copy(
                        pedido = result.data,
                        statusUpdateLoading = false,
                        statusUpdateSuccess = "Estado actualizado a '$nuevoEstado'"
                    )
                    loadPedidos(filter = listState.selectedFilter)
                }
                is Resource.Error -> {
                    detailState = detailState.copy(
                        statusUpdateLoading = false,
                        statusUpdateError = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun assignBarista(pedidoId: Int, baristaId: Int) {
        viewModelScope.launch {
            detailState = detailState.copy(statusUpdateLoading = true, statusUpdateError = null)
            val request = PedidoUpdateRequest(baristaId = baristaId)
            when (val result = orderRepository.updatePedido(pedidoId, request)) {
                is Resource.Success -> {
                    detailState = detailState.copy(
                        pedido = result.data,
                        statusUpdateLoading = false,
                        statusUpdateSuccess = "Barista asignado"
                    )
                }
                is Resource.Error -> {
                    detailState = detailState.copy(
                        statusUpdateLoading = false,
                        statusUpdateError = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearDetailMessages() {
        detailState = detailState.copy(
            statusUpdateError = null,
            statusUpdateSuccess = null
        )
    }

    fun clearListMessages() {
        listState = listState.copy(error = null)
    }
}
