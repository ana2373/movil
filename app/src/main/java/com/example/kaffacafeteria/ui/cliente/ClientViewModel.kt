package com.example.kaffacafeteria.ui.cliente

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.*
import com.example.kaffacafeteria.domain.model.MedioPago
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.launch

data class ClientCartItem(
    val producto: ProductoDto,
    val cantidad: Int = 1
) {
    val subtotal: Double get() = (producto.precioVenta.toDoubleOrNull() ?: 0.0) * cantidad
}

data class ClientUiState(
    val user: User? = null,
    val productos: List<ProductoDto> = emptyList(),
    val categorias: List<CategoriaDto> = emptyList(),
    val mediosPago: List<MedioPago> = emptyList(),
    val cartItems: List<ClientCartItem> = emptyList(),
    val selectedCategoria: Int? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val createdOrderId: Int? = null,
    val showPaymentSuccess: Boolean = false
) {
    val cartTotal: Double get() = cartItems.sumOf { it.subtotal }
    val cartCount: Int get() = cartItems.sumOf { it.cantidad }
    val filteredProductos: List<ProductoDto>
        get() = productos.filter { p ->
            (selectedCategoria == null || p.categoriaId == selectedCategoria) &&
                    (searchQuery.isBlank() || p.nombre.contains(searchQuery, ignoreCase = true))
        }
}

class ClientViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as KaffaApp).container.authRepository
    private val catalogApi = (application as KaffaApp).container.catalogApi
    private val orderApi = (application as KaffaApp).container.orderApi

    var uiState by mutableStateOf(ClientUiState())
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            when (val userResult = authRepository.getMe()) {
                is Resource.Success -> uiState = uiState.copy(user = userResult.data)
                else -> {}
            }
            loadProductos()
            try {
                val catResponse = catalogApi.getCategorias(perPage = 100)
                if (catResponse.isSuccessful) {
                    uiState = uiState.copy(categorias = catResponse.body()?.data ?: emptyList())
                }
            } catch (_: Exception) {}
            try {
                val mpResponse = catalogApi.getMediosPago(perPage = 100)
                if (mpResponse.isSuccessful) {
                    uiState = uiState.copy(
                        mediosPago = (mpResponse.body()?.data ?: emptyList()).map {
                            MedioPago(it.id, it.nombre, it.esVirtual, it.activo)
                        }
                    )
                }
            } catch (_: Exception) {}
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun loadProductos() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = catalogApi.getProductos(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(productos = response.body()?.data ?: emptyList(), isLoading = false)
                } else {
                    uiState = uiState.copy(isLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectCategoria(id: Int?) { uiState = uiState.copy(selectedCategoria = id) }
    fun updateSearch(q: String) { uiState = uiState.copy(searchQuery = q) }

    fun addToCart(producto: ProductoDto, cantidad: Int = 1) {
        val existing = uiState.cartItems.indexOfFirst { it.producto.id == producto.id }
        if (existing >= 0) {
            val items = uiState.cartItems.toMutableList()
            items[existing] = items[existing].copy(cantidad = items[existing].cantidad + cantidad)
            uiState = uiState.copy(cartItems = items)
        } else {
            uiState = uiState.copy(cartItems = uiState.cartItems + ClientCartItem(producto, cantidad))
        }
    }

    fun updateCantidad(productoId: Int, cantidad: Int) {
        if (cantidad <= 0) { removeFromCart(productoId); return }
        val items = uiState.cartItems.toMutableList()
        val index = items.indexOfFirst { it.producto.id == productoId }
        if (index >= 0) {
            items[index] = items[index].copy(cantidad = cantidad)
            uiState = uiState.copy(cartItems = items)
        }
    }

    fun removeFromCart(productoId: Int) {
        uiState = uiState.copy(cartItems = uiState.cartItems.filter { it.producto.id != productoId })
    }

    fun clearCart() {
        uiState = uiState.copy(cartItems = emptyList(), showPaymentSuccess = false, createdOrderId = null, successMessage = null)
    }

    fun createOrder(cashPaymentMethodId: Int?, transferMethodId: Int?, comprobanteUrl: String?) {
        val user = uiState.user ?: return
        val total = uiState.cartTotal
        if (total <= 0) return

        val detalles = uiState.cartItems.map { item ->
            val precio = item.producto.precioVenta.toDoubleOrNull() ?: 0.0
            PedidoDetalleRequest(
                productoId = item.producto.id,
                cantidad = item.cantidad,
                precioUnitario = precio,
                subtotal = precio * item.cantidad
            )
        }

        // Determinar método de pago
        val medioPagoId = if (comprobanteUrl != null && transferMethodId != null) transferMethodId
            else if (comprobanteUrl == null && cashPaymentMethodId != null) cashPaymentMethodId
            else return

        val pagos = listOf(
            PagoPedidoRequest(
                medioPagoId = medioPagoId,
                monto = total,
                comprobanteUrl = comprobanteUrl?.ifBlank { null }
            )
        )

        val request = PedidoRequest(
            clienteId = user.id,
            total = total,
            propina = 0.0,
            estado = "pendiente",
            cajaId = null,
            detalles = detalles,
            pagos = pagos,
            factura = FacturaRequest(
                numeroFactura = "KAF-${System.currentTimeMillis()}",
                subtotal = total,
                impuestos = 0.0,
                total = total
            )
        )

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val result = orderApi.createPedido(request)
                if (result.isSuccessful) {
                    val order = result.body()
                    uiState = uiState.copy(
                        isLoading = false,
                        cartItems = emptyList(),
                        createdOrderId = order?.id,
                        showPaymentSuccess = true,
                        successMessage = "¡Pedido confirmado!"
                    )
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error al crear pedido: ${result.code()}")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error de conexión")
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, successMessage = null, showPaymentSuccess = false)
    }
}
