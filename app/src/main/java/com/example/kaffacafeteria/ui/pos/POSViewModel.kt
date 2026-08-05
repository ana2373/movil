package com.example.kaffacafeteria.ui.pos

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.api.CatalogApi
import com.example.kaffacafeteria.data.remote.api.OrderApi
import com.example.kaffacafeteria.data.remote.dto.*
import com.example.kaffacafeteria.domain.model.MedioPago
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.launch

data class CartItem(
    val producto: ProductoDto,
    val cantidad: Int = 1
) {
    val subtotal: Double get() = (producto.precioVenta.toDoubleOrNull() ?: 0.0) * cantidad
}

data class POSUiState(
    val user: User? = null,
    val productos: List<ProductoDto> = emptyList(),
    val categorias: List<CategoriaDto> = emptyList(),
    val mediosPago: List<MedioPago> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val selectedCategoria: Int? = null,
    val isLoading: Boolean = false,
    val isLoadingProductos: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val showPaymentDialog: Boolean = false,
    val selectedMedioPagoId: Int? = null,
    val comprobanteUrl: String = "",
    val showPaymentSuccess: Boolean = false,
    val createdOrderId: Int? = null,
    val currentPage: Int = 1,
    val lastPage: Int = 1
) {
    val cartTotal: Double get() = cartItems.sumOf { it.subtotal }
    val cartCount: Int get() = cartItems.size
    val filteredProductos: List<ProductoDto>
        get() = productos.filter { p ->
            (selectedCategoria == null || p.categoriaId == selectedCategoria) &&
                    (searchQuery.isBlank() || p.nombre.contains(searchQuery, ignoreCase = true))
        }
}

class POSViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as KaffaApp).container.authRepository
    private val catalogApi = (application as KaffaApp).container.catalogApi
    private val orderApi = (application as KaffaApp).container.orderApi

    var uiState by mutableStateOf(POSUiState())
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
                val mpResponse = catalogApi.getMediosPago()
                if (mpResponse.isSuccessful) {
                    uiState = uiState.copy(
                        mediosPago = (mpResponse.body() ?: emptyList()).map {
                            MedioPago(it.id, it.nombre, it.esVirtual, it.activo)
                        }
                    )
                }
            } catch (_: Exception) {}
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun loadProductos(page: Int = 1) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingProductos = true)
            try {
                val response = catalogApi.getProductos(perPage = 50)
                if (response.isSuccessful) {
                    val data = response.body()!!
                    uiState = uiState.copy(
                        productos = data.data,
                        currentPage = data.meta.currentPage,
                        lastPage = data.meta.lastPage,
                        isLoadingProductos = false
                    )
                }
            } catch (_: Exception) {
                uiState = uiState.copy(isLoadingProductos = false)
            }
        }
    }

    fun selectCategoria(categoriaId: Int?) {
        uiState = uiState.copy(selectedCategoria = categoriaId)
    }

    fun updateSearch(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun addToCart(producto: ProductoDto) {
        val existing = uiState.cartItems.indexOfFirst { it.producto.id == producto.id }
        if (existing >= 0) {
            val items = uiState.cartItems.toMutableList()
            items[existing] = items[existing].copy(cantidad = items[existing].cantidad + 1)
            uiState = uiState.copy(cartItems = items)
        } else {
            uiState = uiState.copy(cartItems = uiState.cartItems + CartItem(producto))
        }
    }

    fun updateCantidad(productoId: Int, cantidad: Int) {
        if (cantidad <= 0) {
            removeFromCart(productoId)
            return
        }
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
        uiState = uiState.copy(cartItems = emptyList(), showPaymentDialog = false, showPaymentSuccess = false)
    }

    fun showPayment() {
        uiState = uiState.copy(showPaymentDialog = true)
    }

    fun hidePayment() {
        uiState = uiState.copy(showPaymentDialog = false)
    }

    fun selectMedioPago(id: Int) {
        uiState = uiState.copy(selectedMedioPagoId = id)
    }

    fun updateComprobante(url: String) {
        uiState = uiState.copy(comprobanteUrl = url)
    }

    fun createOrder() {
        val medioPagoId = uiState.selectedMedioPagoId ?: return
        val medioPago = uiState.mediosPago.find { it.id == medioPagoId } ?: return

        if (medioPago.esVirtual && uiState.comprobanteUrl.isBlank()) {
            uiState = uiState.copy(error = "El comprobante es requerido para pagos virtuales")
            return
        }

        val total = uiState.cartTotal
        val detalles = uiState.cartItems.map { item ->
            val precio = item.producto.precioVenta.toDoubleOrNull() ?: 0.0
            PedidoDetalleRequest(
                productoId = item.producto.id,
                cantidad = item.cantidad,
                precioUnitario = precio,
                subtotal = precio * item.cantidad
            )
        }

        val pagos = listOf(
            PagoPedidoRequest(
                medioPagoId = medioPagoId,
                monto = total,
                comprobanteUrl = uiState.comprobanteUrl.ifBlank { null }
            )
        )

        val request = PedidoRequest(
            clienteId = uiState.user?.id ?: return,
            total = total,
            propina = 0.0,
            estado = "pendiente",
            cajaId = null,
            detalles = detalles,
            pagos = pagos,
            factura = FacturaRequest(
                numeroFactura = "POS-${System.currentTimeMillis()}",
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
                        showPaymentDialog = false,
                        showPaymentSuccess = true,
                        createdOrderId = order?.id,
                        cartItems = emptyList()
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
