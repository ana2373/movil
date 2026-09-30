package com.example.kaffacafeteria.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.data.local.ReportArchive
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.PrimaryGreen
import com.example.kaffacafeteria.ui.theme.SecondaryGreen
import com.example.kaffacafeteria.ui.theme.ink
import com.example.kaffacafeteria.util.PdfFileActions
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    tipo: String,
    onBack: () -> Unit,
    generadoPor: String = "Kaffa Cafeteria",
    viewModel: ReportDetailViewModel = createViewModel { ReportDetailViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val pdf = state.pdfGenerado

    LaunchedEffect(tipo) { viewModel.load(tipo) }

    // Avisos de éxito (PDF guardado, archivo no disponible...).
    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbarHost.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    val title = when (tipo) {
        ReportArchive.TIPO_DIARIO -> "Reporte Diario"
        ReportArchive.TIPO_SEMANAL -> "Reporte Semanal"
        ReportArchive.TIPO_MENSUAL -> "Reporte Mensual"
        ReportArchive.TIPO_INVENTARIO -> "Inventario"
        else -> "Reporte"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
                actions = {
                    IconButton(
                        onClick = { viewModel.generarPdf(generadoPor) },
                        enabled = !state.isLoading && !state.isGenerandoPdf
                    ) {
                        if (state.isGenerandoPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Generar PDF", tint = colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
            )

            when {
                state.isLoading -> LoadingIndicator()
                state.error != null -> ErrorMessage(message = state.error!!, onRetry = { viewModel.load(tipo) })
                tipo == ReportArchive.TIPO_INVENTARIO -> InventarioSection(state = state, colorScheme = colorScheme)
                else -> VentasSection(state = state, colorScheme = colorScheme)
            }

            // Acciones sobre el PDF: una vez generado queda archivado, así que
            // puede abrirse, compartirse o descargarse las veces que haga falta.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.ultimoArchivado == null && pdf == null) {                        Button(
                            onClick = { viewModel.generarPdf(generadoPor) },
                            enabled = !state.isGenerandoPdf,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Generar y guardar PDF")
                        }
                        Text(
                            "El PDF queda guardado en la aplicación y podrás descargarlo o compartirlo cuando quieras.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    } else {
                        val archivo = pdf ?: state.archivoUltimo
                        Text(
                            "Último archivo: ${state.ultimoArchivado?.fechaGenerado ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    archivo?.let { PdfFileActions.abrir(context, it) }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Abrir")
                            }
                            OutlinedButton(
                                onClick = {
                                    archivo?.let { PdfFileActions.compartir(context, it, title) }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Compartir")
                            }
                            FilledTonalButton(
                                onClick = { viewModel.generarPdf(generadoPor) },
                                enabled = !state.isGenerandoPdf,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Regenerar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VentasSection(state: ReportUiState, colorScheme: androidx.compose.material3.ColorScheme) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportKpi(
                label = "Ventas",
                value = "$" + "%.0f".format(state.totalVentas),
                icon = Icons.Default.ShoppingCart,
                color = PrimaryGreen,
                modifier = Modifier.weight(1f)
            )
            ReportKpi(
                label = "Pedidos",
                value = state.totalPedidos.toString(),
                icon = Icons.Default.ReceiptLong,
                color = SecondaryGreen,
                modifier = Modifier.weight(1f)
            )
            ReportKpi(
                label = "Ticket promedio",
                value = "$" + "%.0f".format(state.ticketPromedio),
                icon = Icons.Default.Star,
                color = colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Ventas por día", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                SevenDayChart(state.ventasPorDia)
            }
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Productos más vendidos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                if (state.topProductos.isEmpty()) {
                    Text("Sin ventas en el periodo", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                } else {
                    state.topProductos.forEachIndexed { index, prod ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = PrimaryGreen.copy(alpha = 0.12f)) {
                                Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", fontWeight = FontWeight.Black, color = ink(PrimaryGreen))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(prod.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text("${prod.cantidad} uds", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ink(SecondaryGreen))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventarioSection(state: ReportUiState, colorScheme: androidx.compose.material3.ColorScheme) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportKpi(
                label = "Insumos",
                value = state.insumos.size.toString(),
                icon = Icons.Default.Inventory,
                color = PrimaryGreen,
                modifier = Modifier.weight(1f)
            )
            ReportKpi(
                label = "Bajo stock",
                value = state.insumosBajoStock.toString(),
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Estado del inventario", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                if (state.insumos.isEmpty()) {
                    EmptyState("No hay insumos registrados")
                } else {
                    state.insumos.forEach { item ->
                        val bajo = item.minimo != null && item.stock < item.minimo
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.insumo.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    listOfNotNull(
                                        item.insumo.unidad_medida,
                                        item.minimo?.let { "mín. ${"%.1f".format(it)}" }
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "${"%.1f".format(item.stock)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = if (bajo) MaterialTheme.colorScheme.error else ink(PrimaryGreen)
                            )
                        }
                        if (bajo) {
                            Text(
                                "Stock por debajo del mínimo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportKpi(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val accent = if (color == MaterialTheme.colorScheme.error) color else ink(color)
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = accent.copy(alpha = 0.15f), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp)) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = accent, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}