package com.example.kaffacafeteria.ui.barista

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.example.kaffacafeteria.data.remote.dto.PedidoDto
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaristaPanelScreen(
    onBack: () -> Unit,
    onNavigateToPOS: () -> Unit = {},
    onNavigateToCaja: () -> Unit = {},
    onNavigateToInsumos: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onExportHistorial: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: BaristaPanelViewModel = createViewModel { BaristaPanelViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    LaunchedEffect(Unit) {
        viewModel.loadPedidos()
        viewModel.startAutoRefresh()
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessages()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Panel Barista") },
            actions = {
                IconButton(onClick = onExportHistorial) { Icon(Icons.Default.Download, contentDescription = "Exportar historial", tint = colorScheme.onPrimary) }
                IconButton(onClick = { viewModel.loadPedidos() }) { Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = colorScheme.onPrimary) }
                IconButton(onClick = onNavigateToProfile) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = colorScheme.onPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ── Resumen turno + acciones rápidas ──
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = colorScheme.secondary.copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = colorScheme.secondary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Turno activo", style = MaterialTheme.typography.labelMedium, color = colorScheme.onSurfaceVariant)
                        Text("Cobro y barra", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    }
                    Button(
                        onClick = onNavigateToPOS,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar pedido", fontWeight = FontWeight.Bold)
                    }
                }
            }

            state.successMessage?.let { msg ->
                Snackbar(modifier = Modifier.fillMaxWidth(), containerColor = colorScheme.primaryContainer, contentColor = colorScheme.onPrimaryContainer) {
                    Text(msg, fontWeight = FontWeight.SemiBold)
                }
            }
            state.error?.let { err ->
                Text(err, modifier = Modifier.fillMaxWidth(), color = colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }

            // ── Tablero Kanban de órdenes ──
            Text("Órdenes en tiempo real", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = colorScheme.onSurfaceVariant)

            if (state.isLoading && state.pedidos.isEmpty()) {
                LoadingIndicator()
            } else {
                KanbanBoard(
                    pedidos = state.pedidos,
                    updating = state.updLoadingPedidoId,
                    onPreparar = { viewModel.marcarEnPreparacion(it) },
                    onListo = { viewModel.marcarListo(it) },
                    onPagado = { viewModel.marcarPagado(it) }
                )
            }

            // ── Módulos barista ──
            Text("Módulos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                BaristaModuleCard(icon = Icons.Default.Inventory, title = "Inventario", color = Color(0xFF7C3AED), modifier = Modifier.weight(1f), onClick = onNavigateToInsumos)
                BaristaModuleCard(icon = Icons.Default.PointOfSale, title = "Caja", color = Color(0xFF0891B2), modifier = Modifier.weight(1f), onClick = onNavigateToCaja)
                BaristaModuleCard(icon = Icons.Default.Chat, title = "Chat", color = Color(0xFF0D9488), modifier = Modifier.weight(1f), onClick = onNavigateToChat)
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Black) },
            text = { Text("Estás en el Panel del Barista. ¿Deseas salir y cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = { showExitDialog = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
                ) {
                    Text("Cerrar sesión", color = colorScheme.onPrimary)
                }
            },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("Quedarme") } }
        )
    }
}

@Composable
private fun BaristaModuleCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.12f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp)) }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun KanbanBoard(
    pedidos: List<PedidoDto>,
    updating: Int?,
    onPreparar: (Int) -> Unit,
    onListo: (Int) -> Unit,
    onPagado: (Int) -> Unit
) {
    val pendientes = pedidos.filter { it.estado == "pendiente" || it.estado == "pagado" }
    val enPreparacion = pedidos.filter { it.estado == "en_preparacion" }
    val terminados = pedidos.filter { it.estado == "entregado" || it.estado == "cancelado" }

    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KanbanColumn(
            title = "Por atender",
            count = pendientes.size,
            color = Color(0xFFF59E0B),
            modifier = Modifier.width(280.dp),
            pedidos = pendientes,
            updating = updating,
            onPreparar = onPreparar,
            onListo = onListo,
            onPagado = onPagado
        )
        KanbanColumn(
            title = "En preparación",
            count = enPreparacion.size,
            color = Color(0xFF2563EB),
            modifier = Modifier.width(280.dp),
            pedidos = enPreparacion,
            updating = updating,
            onPreparar = onPreparar,
            onListo = onListo,
            onPagado = onPagado
        )
        KanbanColumn(
            title = "Terminados",
            count = terminados.size,
            color = Color(0xFF16A34A),
            modifier = Modifier.width(280.dp),
            pedidos = terminados,
            updating = updating,
            onPreparar = onPreparar,
            onListo = onListo,
            onPagado = onPagado
        )
    }
}

@Composable
private fun KanbanColumn(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    pedidos: List<PedidoDto>,
    updating: Int?,
    onPreparar: (Int) -> Unit,
    onListo: (Int) -> Unit,
    onPagado: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color) { Text("$count", modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium) }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Black, color = color)
            }
            if (pedidos.isEmpty()) {
                Text("Sin órdenes", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                pedidos.forEach { pedido ->
                    KanbanOrderMini(
                        pedido = pedido,
                        updating = updating == pedido.id,
                        onPreparar = { onPreparar(pedido.id) },
                        onListo = { onListo(pedido.id) },
                        onPagado = { onPagado(pedido.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KanbanOrderMini(
    pedido: PedidoDto,
    updating: Boolean,
    onPreparar: () -> Unit,
    onListo: () -> Unit,
    onPagado: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pedido #${pedido.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("$${"%.2f".format(pedido.total.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.Black, color = colorScheme.secondary)
            }
            Text(pedido.cliente?.nombre ?: "Cliente", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            pedido.detalles?.let { detalles ->
                Spacer(modifier = Modifier.height(6.dp))
                detalles.take(3).forEach { det ->
                    Text("${det.cantidad}x ${det.producto?.nombre ?: "Producto"}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            when (pedido.estado) {
                "pendiente", "pagado" -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPreparar, enabled = !updating, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        if (updating) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                        else Text("Preparar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    if (pedido.estado == "pendiente") {
                        IconButton(onClick = onPagado, enabled = !updating, colors = IconButtonDefaults.iconButtonColors(contentColor = colorScheme.primary)) {
                            Icon(Icons.Default.Payments, contentDescription = "Confirmar pago")
                        }
                    }
                }
                "en_preparacion" -> Button(onClick = onListo, enabled = !updating, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    if (updating) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                    else Text("Listo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                "entregado" -> Text("Entregado ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                "cancelado" -> Text("Cancelado", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
