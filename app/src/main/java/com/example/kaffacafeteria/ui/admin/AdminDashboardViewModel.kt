package com.example.kaffacafeteria.ui.admin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.api.OrderApi
import com.example.kaffacafeteria.data.remote.api.UserApi
import com.example.kaffacafeteria.data.remote.dto.PedidoDto
import com.example.kaffacafeteria.data.remote.dto.UsuarioFullDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DaySales(
    val label: String,
    val total: Double
)

data class TopProduct(
    val nombre: String,
    val cantidad: Int
)

data class AdminDashboardUiState(
    val ingresosHoy: Double = 0.0,
    val pedidosHoy: Int = 0,
    val clientesRegistrados: Int = 0,
    val ticketPromedio: Double = 0.0,
    val ventas7Dias: List<DaySales> = emptyList(),
    val topProductos: List<TopProduct> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val orderApi: OrderApi = (application as KaffaApp).container.orderApi
    private val userApi: UserApi = (application as KaffaApp).container.userApi

    var uiState by mutableStateOf(AdminDashboardUiState())
        private set

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val pedidosResp = orderApi.getPedidos(perPage = 1000)
                val usuariosResp = userApi.getUsuarios(perPage = 1000)

                val pedidos = pedidosResp.body()?.data ?: emptyList()
                val usuarios = usuariosResp.body()?.data ?: emptyList()

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

                val entregadosHoy = pedidos.filter { p ->
                    p.estado == "entregado" && (p.created_at ?: "").startsWith(today)
                }
                val pedidosHoy = pedidos.count { (it.created_at ?: "").startsWith(today) }
                val ingresosHoy = entregadosHoy.sumOf { it.total.toDoubleOrNull() ?: 0.0 }
                val ticketPromedio = if (pedidosHoy > 0) ingresosHoy / pedidosHoy else 0.0

                val clientes = usuarios.count { u -> u.roles?.any { it.nombre == "cliente" } == true }

                val ventas7 = ventas7Dias(pedidos)

                val top = rankingProductos(pedidos)

                uiState = uiState.copy(
                    ingresosHoy = ingresosHoy,
                    pedidosHoy = pedidosHoy,
                    clientesRegistrados = clientes,
                    ticketPromedio = ticketPromedio,
                    ventas7Dias = ventas7,
                    topProductos = top,
                    isLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun ventas7Dias(pedidos: List<PedidoDto>): List<DaySales> {
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayFmt = SimpleDateFormat("EEE", Locale("es", "CO"))
        val result = mutableListOf<DaySales>()
        for (i in 6 downTo 0) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        for (i in 0 until 7) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dateStr = fmt.format(cal.time)
            val total = pedidos
                .filter { it.estado == "entregado" && (it.created_at ?: "").startsWith(dateStr) }
                .sumOf { it.total.toDoubleOrNull() ?: 0.0 }
            result.add(DaySales(label = dayFmt.format(cal.time), total = total))
        }
        return result
    }

    private fun rankingProductos(pedidos: List<PedidoDto>): List<TopProduct> {
        val counts = LinkedHashMap<String, Int>()
        pedidos.forEach { p ->
            p.detalles?.forEach { d ->
                val nombre = d.producto?.nombre ?: "Producto #${d.productoId}"
                counts[nombre] = (counts[nombre] ?: 0) + (d.cantidad?.toDoubleOrNull()?.toInt() ?: 1)
            }
        }
        return counts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { TopProduct(it.key, it.value) }
    }
}
