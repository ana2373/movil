package com.example.kaffacafeteria.ui.gestion

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.*
import kotlinx.coroutines.launch

data class ComprasUiState(
    val compras: List<CompraDto> = emptyList(),
    val proveedores: List<ProveedorDto> = emptyList(),
    val insumos: List<InsumoDto> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ComprasViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionApi = (application as KaffaApp).container.transactionApi
    private val catalogApi = (application as KaffaApp).container.catalogApi

    var uiState by mutableStateOf(ComprasUiState())
        private set

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val comprasResp = transactionApi.getCompras(perPage = 1000)
                val proveedoresResp = transactionApi.getProveedores(perPage = 1000)
                val insumosResp = catalogApi.getInsumos(perPage = 1000)
                uiState = uiState.copy(
                    compras = comprasResp.body()?.data ?: emptyList(),
                    proveedores = proveedoresResp.body()?.data ?: emptyList(),
                    insumos = insumosResp.body()?.data ?: emptyList(),
                    isLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun create(
        proveedorId: Int,
        numeroFactura: String?,
        detalles: List<CompraDetalleDto>
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isCreating = true, error = null, successMessage = null)
            try {
                val total = detalles.sumOf { it.subtotal?.toDoubleOrNull() ?: 0.0 }
                val factura = numeroFactura?.ifBlank { "F-${System.currentTimeMillis()}" }
                val response = transactionApi.createCompra(
                    CompraDto(
                        id = 0,
                        proveedorId = proveedorId,
                        numeroFactura = factura,
                        total = total.toString(),
                        detalles = detalles
                    )
                )
                uiState = uiState.copy(isCreating = false)
                if (response.isSuccessful) {
                    uiState = uiState.copy(successMessage = "Compra registrada")
                    load()
                } else {
                    uiState = uiState.copy(error = "Error al registrar compra (${response.code()})")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isCreating = false, error = e.message)
            }
        }
    }
}