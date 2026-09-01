package com.example.kaffacafeteria.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    pedidoId: Int,
    onBack: () -> Unit,
    viewModel: OrderViewModel = createViewModel { OrderViewModel(it) }
) {
    val state = viewModel.detailState
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(pedidoId) { viewModel.loadPedidoDetail(pedidoId) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Pedido #$pedidoId") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(message = state.error!!, onRetry = { viewModel.loadPedidoDetail(pedidoId) })
            state.pedido == null -> LoadingIndicator()
            else -> {
                val pedido = state.pedido!!
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estado", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                                StatusBadge(pedido.estado)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Cliente", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                                Text(pedido.cliente?.nombre ?: "N/A", fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Fecha", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                                Text(pedido.created_at ?: "", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                                Text("$${"%.2f".format(pedido.total.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.Bold, color = colorScheme.secondary)
                            }
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Productos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colorScheme.outlineVariant)
                            pedido.detalles?.forEach { det ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${det.producto?.nombre ?: "Producto"} x${det.cantidad}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    Text("$${"%.2f".format(det.subtotal.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.SemiBold, color = colorScheme.secondary)
                                }
                            }
                        }
                    }

                    pedido.pagos?.let { pagos ->
                        if (pagos.isNotEmpty()) {
                            Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Pagos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colorScheme.outlineVariant)
                                    pagos.forEach { pago ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(pago.medio_pago?.nombre ?: "Método", style = MaterialTheme.typography.bodyMedium)
                                            Text("$${"%.2f".format(pago.monto.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.SemiBold, color = colorScheme.secondary)
                                        }
                                        if (!pago.comprobanteUrl.isNullOrBlank()) {
                                            Text("Comprobante: ${pago.comprobanteUrl}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val currentStatus = pedido.estado
                    val nextStatuses = when (currentStatus) {
                        "pendiente" -> listOf("pagado")
                        "pagado" -> listOf("en_preparacion")
                        "en_preparacion" -> listOf("entregado")
                        else -> emptyList()
                    }

                    if (nextStatuses.isNotEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Acciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colorScheme.outlineVariant)
                                nextStatuses.forEach { status ->
                                    Button(
                                        onClick = { viewModel.updateEstado(pedido.id, status) },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary),
                                        enabled = !state.statusUpdateLoading
                                    ) {
                                        if (state.statusUpdateLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                                        else Text("Marcar como ${status.replace("_", " ").replaceFirstChar { c -> c.uppercase() }}", color = colorScheme.onSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
