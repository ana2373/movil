package com.example.kaffacafeteria.ui.admin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.api.CatalogApi
import com.example.kaffacafeteria.data.remote.api.OrderApi
import com.example.kaffacafeteria.data.remote.dto.InsumoDto
import com.example.kaffacafeteria.data.remote.dto.PedidoDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class InsumoStock(
    val insumo: InsumoDto,
    val stock: Double,
    val minimo: Double?
)

data class ReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalVentas: Double = 0.0,
    val totalPedidos: Int = 0,
    val ticketPromedio: Double = 0.0,
    val ventasPorDia: List<DaySales> = emptyList(),
    val topProductos: List<TopProduct> = emptyList(),
    val insumos: List<InsumoStock> = emptyList(),
    val insumosBajoStock: Int = 0
)

class ReportDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val orderApi: OrderApi = (application as KaffaApp).container.orderApi
    private val catalogApi: CatalogApi = (application as KaffaApp).container.catalogApi

    var uiState by mutableStateOf(ReportUiState())
        private set

    fun load(tipo: String) {
        viewModelScope.launch {
            uiState = ReportUiState(isLoading = true)
            try {
                if (tipo == "inventario") {
                    val insumosResp = catalogApi.getInsumos(perPage = 1000)
                    val stock = (insumosResp.body()?.data ?: emptyList()).map { i ->
                        InsumoStock(
                            insumo = i,
                            stock = i.stock_actual?.toDoubleOrNull() ?: 0.0,
                            minimo = i.stock_minimo?.toDoubleOrNull()
                        )
                    }
                    uiState = uiState.copy(
                        isLoading = false,
                        insumos = stock,
                        insumosBajoStock = stock.count { it.minimo != null && it.stock < it.minimo }
                    )
                    return@launch
                }

                val pedidosResp = orderApi.getPedidos(perPage = 1000)
                val pedidos = pedidosResp.body()?.data ?: emptyList()

                val entregados = entregadosEnPeriodo(pedidos, tipo)
                val totalVentas = entregados.sumOf { it.total.toDoubleOrNull() ?: 0.0 }
                val totalPedidos = pedidosEnPeriodo(pedidos, tipo)
                val ticketPromedio = if (totalPedidos > 0) totalVentas / totalPedidos else 0.0

                uiState = uiState.copy(
                    isLoading = false,
                    totalVentas = totalVentas,
                    totalPedidos = totalPedidos,
                    ticketPromedio = ticketPromedio,
                    ventasPorDia = ventasPorDia(entregados, tipo),
                    topProductos = rankingProductos(entregados)
                )
            } catch (e: Exception) {
                uiState = ReportUiState(isLoading = false, error = e.message)
            }
        }
    }

    private fun entregadosEnPeriodo(pedidos: List<PedidoDto>, tipo: String): List<PedidoDto> =
        pedidos.filter { it.estado == "entregado" && enPeriodo(it, tipo) }

    private fun pedidosEnPeriodo(pedidos: List<PedidoDto>, tipo: String): Int =
        pedidos.count { enPeriodo(it, tipo) }

    private fun enPeriodo(p: PedidoDto, tipo: String): Boolean {
        val fecha = p.created_at ?: return false
        val datePart = fecha.take(10)
        return when (tipo) {
            "diario" -> datePart == hoy()
            "semanal" -> {
                val date = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(datePart) }.getOrNull() ?: return false
                val inicio = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }.time
                !date.before(inicio)
            }
            "mensual" -> {
                val parts = datePart.split("-")
                if (parts.size != 3) return false
                val cal = Calendar.getInstance()
                parts[0].toIntOrNull() == cal.get(Calendar.YEAR) && parts[1].toIntOrNull() == (cal.get(Calendar.MONTH) + 1)
            }
            else -> false
        }
    }

    private fun ventasPorDia(entregados: List<PedidoDto>, tipo: String): List<DaySales> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return when (tipo) {
            "diario" -> listOf(DaySales("Hoy", entregados.sumOf { it.total.toDoubleOrNull() ?: 0.0 }))
            "semanal" -> {
                val dayFmt = SimpleDateFormat("EEE", Locale("es", "CO"))
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
                (0 until 7).map { _ ->
                    val dateStr = fmt.format(cal.time)
                    val total = entregados.filter { (it.created_at ?: "").startsWith(dateStr) }
                        .sumOf { it.total.toDoubleOrNull() ?: 0.0 }
                    val label = dayFmt.format(cal.time)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    DaySales(label, total)
                }
            }
            "mensual" -> {
                val cal = Calendar.getInstance()
                val mesInicio = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val hoyDia = cal.get(Calendar.DAY_OF_MONTH)
                (1..hoyDia).mapNotNull { day ->
                    cal.set(Calendar.DAY_OF_MONTH, day)
                    val dateStr = fmt.format(cal.time)
                    val total = entregados.filter { (it.created_at ?: "").startsWith(dateStr) }
                        .sumOf { it.total.toDoubleOrNull() ?: 0.0 }
                    DaySales(day.toString(), total)
                }
            }
            else -> emptyList()
        }
    }

    private fun rankingProductos(entregados: List<PedidoDto>): List<TopProduct> {
        val counts = LinkedHashMap<String, Int>()
        entregados.forEach { p ->
            p.detalles?.forEach { d ->
                val cantidad = d.cantidad?.toDoubleOrNull()?.toInt() ?: 1
                val nombre = d.producto?.nombre ?: "Producto #${d.productoId}"
                counts[nombre] = (counts[nombre] ?: 0) + cantidad
            }
        }
        return counts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { TopProduct(it.key, it.value) }
    }

    private fun hoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
}