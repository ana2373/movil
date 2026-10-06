package com.example.kaffacafeteria.ui.admin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.local.ReportArchive
import com.example.kaffacafeteria.data.remote.api.CatalogApi
import com.example.kaffacafeteria.data.remote.api.OrderApi
import com.example.kaffacafeteria.data.remote.dto.InsumoDto
import com.example.kaffacafeteria.data.remote.dto.PedidoDto
import com.example.kaffacafeteria.util.PdfReportBuilder
import com.example.kaffacafeteria.util.fetchAllPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
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
    val insumosBajoStock: Int = 0,
    /** true mientras se arma el PDF (se hace fuera del hilo principal). */
    val isGenerandoPdf: Boolean = false,
    /** Archivo PDF recién generado; la UI ofrece abrirlo o compartirlo. */
    val pdfGenerado: File? = null,
    /** Último PDF archivado de este tipo, para reenviarlo sin regenerar. */
    val ultimoArchivado: ReportArchive.ReporteArchivado? = null,
    /** Ruta del último PDF archivado; es null si el archivo fue borrado. */
    val archivoUltimo: File? = null,
    val mensaje: String? = null
)

class ReportDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val orderApi: OrderApi = (application as KaffaApp).container.orderApi
    private val catalogApi: CatalogApi = (application as KaffaApp).container.catalogApi
    private val archive = ReportArchive(application)

    private var tipoActual: String = ReportArchive.TIPO_DIARIO

    var uiState by mutableStateOf(ReportUiState())
        private set

    fun load(tipo: String) {
        tipoActual = tipo
        // El último PDF archivado se resuelve fuera del hilo principal porque
        // el archivo vive en disco.
        viewModelScope.launch {
            val archivado = withContext(Dispatchers.IO) { archive.ultimoReporte(tipo) }
            val archivo = archivado?.let { withContext(Dispatchers.IO) { archive.archivoDe(it) } }
            uiState = ReportUiState(isLoading = true, ultimoArchivado = archivado, archivoUltimo = archivo)
            try {
                if (tipo == ReportArchive.TIPO_INVENTARIO) {
                    // per_page está limitado a 100 en el backend: se recorren todas
                    // las páginas para no dejar insumos fuera del reporte.
                    val insumos = fetchAllPages { page -> catalogApi.getInsumos(perPage = 100, page = page) }
                    val stock = insumos.map { i ->
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

                val pedidos = fetchAllPages { page -> orderApi.getPedidos(perPage = 100, page = page) }

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

    // ── Generación y archivo de PDF ───────────────────────────────────────────

    /**
     * Genera el PDF del reporte que está en pantalla y lo archiva para que
     * pueda descargarse o compartirse más adelante (y no se pierda al salir
     * de la pantalla).
     *
     * Se ejecuta fuera del hilo principal porque el dibujo del PDF y la
     * escritura en disco pueden tardar.
     */
    fun generarPdf(generadoPor: String) {
        val estado = uiState
        if (estado.isGenerandoPdf || estado.isLoading) return

        viewModelScope.launch {
            uiState = estado.copy(isGenerandoPdf = true, pdfGenerado = null, mensaje = null, error = null)
            val tipo = tipoActual
            val resultado = withContext(Dispatchers.IO) {
                runCatching { construirYArchivar(tipo, estado, generadoPor) }
            }
            resultado
                .onSuccess { archivo ->
                    uiState = uiState.copy(
                        isGenerandoPdf = false,
                        pdfGenerado = archivo.first,
                        ultimoArchivado = archivo.second,
                        archivoUltimo = archivo.first,
                        mensaje = "Reporte guardado. Puedes abrirlo, compartirlo o descargarlo cuando quieras."
                    )
                }
                .onFailure { e ->
                    uiState = uiState.copy(
                        isGenerandoPdf = false,
                        error = e.message ?: "No se pudo generar el reporte"
                    )
                }
        }
    }

    /** Reenvía el último PDF archivado de este tipo, sin recalcular nada. */
    fun reabrirUltimo() {
        val archivo = uiState.archivoUltimo
        if (archivo != null && archivo.exists()) {
            uiState = uiState.copy(pdfGenerado = archivo)
        } else {
            uiState = uiState.copy(mensaje = "El archivo anterior ya no está disponible. Genera el reporte de nuevo.")
        }
    }

    fun limpiarMensaje() {
        uiState = uiState.copy(mensaje = null, pdfGenerado = null)
    }

    private fun construirYArchivar(
        tipo: String,
        estado: ReportUiState,
        generadoPor: String
    ): Pair<File, ReportArchive.ReporteArchivado> {
        val titulo = ReportArchive.etiquetaTipo(tipo)
        val archivo = archive.nuevoArchivo("reporte_$tipo")

        val kpis: List<Pair<String, String>>
        val secciones: List<PdfReportBuilder.Seccion>

        if (tipo == ReportArchive.TIPO_INVENTARIO) {
            kpis = listOf(
                "Insumos" to PdfReportBuilder.int(estado.insumos.size),
                "Bajo stock" to PdfReportBuilder.int(estado.insumosBajoStock)
            )
            secciones = listOf(
                PdfReportBuilder.Seccion(
                    titulo = "Estado del inventario",
                    headers = listOf("Insumo", "Unidad", "Stock actual", "Stock mínimo", "Estado"),
                    filas = estado.insumos.map { item ->
                        val bajo = item.minimo != null && item.stock < item.minimo
                        listOf(
                            item.insumo.nombre,
                            item.insumo.unidad_medida ?: "-",
                            "%.1f".format(item.stock),
                            item.minimo?.let { "%.1f".format(it) } ?: "-",
                            if (bajo) "BAJO STOCK" else "Normal"
                        )
                    },
                    anchos = listOf(3f, 1.2f, 1.3f, 1.3f, 1.4f)
                )
            )
        } else {
            kpis = listOf(
                "Ventas" to PdfReportBuilder.money(estado.totalVentas),
                "Pedidos" to PdfReportBuilder.int(estado.totalPedidos),
                "Ticket promedio" to PdfReportBuilder.money(estado.ticketPromedio)
            )
            secciones = buildList {
                add(
                    PdfReportBuilder.Seccion(
                        titulo = "Ventas por día",
                        headers = listOf("Día", "Ventas"),
                        filas = estado.ventasPorDia.map { listOf(it.label, PdfReportBuilder.money(it.total)) },
                        anchos = listOf(1f, 1.4f)
                    )
                )
                add(
                    PdfReportBuilder.Seccion(
                        titulo = "Productos más vendidos",
                        headers = listOf("Producto", "Unidades"),
                        filas = estado.topProductos.map { listOf(it.nombre, PdfReportBuilder.int(it.cantidad)) },
                        anchos = listOf(3f, 1f)
                    )
                )
            }
        }

        val ok = PdfReportBuilder.generar(
            context = getApplication(),
            destino = archivo,
            titulo = titulo,
            subtitulo = periodoLegible(tipo),
            generadoPor = generadoPor,
            kpis = kpis,
            secciones = secciones
        ) ?: throw IllegalStateException("No se pudo crear el archivo PDF")

        return ok to archive.registrar(
            titulo = titulo,
            tipo = tipo,
            periodo = periodoLegible(tipo),
            archivo = ok,
            generadoPor = generadoPor
        )
    }

    private fun periodoLegible(tipo: String): String {
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))
        return when (tipo) {
            ReportArchive.TIPO_DIARIO -> "Hoy · ${fmt.format(Calendar.getInstance().time)}"
            ReportArchive.TIPO_SEMANAL -> "Últimos 7 días · ${fmt.format(
                Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }.time
            )} a ${fmt.format(Calendar.getInstance().time)}"
            ReportArchive.TIPO_MENSUAL -> "Mes en curso · ${SimpleDateFormat("MMMM yyyy", Locale("es", "CO")).format(Calendar.getInstance().time)}"
            ReportArchive.TIPO_INVENTARIO -> "Inventario actual"
            else -> "Sin periodo"
        }
    }
}