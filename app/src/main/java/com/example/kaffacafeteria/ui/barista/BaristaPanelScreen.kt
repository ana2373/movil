package com.example.kaffacafeteria.ui.barista

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.kaffacafeteria.ui.theme.CoffeeBrown
import com.example.kaffacafeteria.ui.theme.PrimaryGreen
import com.example.kaffacafeteria.ui.theme.CafeOscuro
import com.example.kaffacafeteria.ui.theme.Negro
import com.example.kaffacafeteria.ui.theme.Terracotta
import com.example.kaffacafeteria.ui.theme.VerdeClaro
import com.example.kaffacafeteria.ui.theme.isDarkModeActive
import com.example.kaffacafeteria.ui.theme.ink
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaristaPanelScreen(
    onBack: () -> Unit,
    onNavigateToInsumos: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToCaja: () -> Unit = {},
    onExportHistorial: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: BaristaPanelViewModel = createViewModel { BaristaPanelViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showExitDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    LaunchedEffect(Unit) {
        viewModel.loadPedidos()
        viewModel.loadInsumos()
        viewModel.startAutoRefresh()
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2500)
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
            // ── Resumen de gestión de pedidos ──
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = ink(CafeOscuro).copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = ink(CafeOscuro))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gestión de pedidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    }
                }
            }

            state.successMessage?.let { msg ->
                Snackbar(modifier = Modifier.fillMaxWidth(), containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary) {
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
                    onEntregar = { viewModel.marcarListo(it) }
                )
            }

            // ── Módulos barista ──
            Text("Módulos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                BaristaModuleCard(icon = Icons.Default.Inventory, title = "Inventario", color = PrimaryGreen, modifier = Modifier.weight(1f), onClick = onNavigateToInsumos)
                BaristaModuleCard(icon = Icons.Default.Assignment, title = "Reportar inventario", color = CafeOscuro, modifier = Modifier.weight(1f), onClick = { showReportDialog = true })
                BaristaModuleCard(icon = Icons.Default.Chat, title = "Chat", color = Negro, modifier = Modifier.weight(1f), onClick = onNavigateToChat)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                BaristaModuleCard(icon = Icons.Default.PointOfSale, title = "Caja de cobro", color = Terracotta, modifier = Modifier.weight(1f), onClick = onNavigateToCaja)
            }
        }
    }

    if (showReportDialog) {
        ReportInventoryDialog(
            insumos = state.insumos,
            isReporting = state.isReporting,
            onDismiss = { showReportDialog = false },
            onConfirm = { insumoId, descripcion, cantidad, motivo ->
                viewModel.reportInventory(insumoId, descripcion, cantidad, motivo)
                showReportDialog = false
            }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportInventoryDialog(
    insumos: List<com.example.kaffacafeteria.data.remote.dto.InsumoDto>,
    isReporting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int?, String, Double, String?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedInsumoId by remember { mutableStateOf<Int?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var insumoExpanded by remember { mutableStateOf(false) }
    val motivos = listOf("Consumo", "Vencido", "Dañado", "No vendido", "Derrame", "Otro")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar al inventario", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Este reporte queda registrado y el administrador podrá verlo en el módulo de Mermas.", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                if (insumos.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = insumoExpanded, onExpandedChange = { insumoExpanded = it }) {
                        OutlinedTextField(value = insumos.find { it.id == selectedInsumoId }?.nombre ?: "Seleccionar insumo", onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = insumoExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                        ExposedDropdownMenu(expanded = insumoExpanded, onDismissRequest = { insumoExpanded = false }) {
                            insumos.forEach { insumo -> DropdownMenuItem(text = { Text(insumo.nombre) }, onClick = { selectedInsumoId = insumo.id; insumoExpanded = false }) }
                        }
                    }
                } else {
                    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción del insumo") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                }
                OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad usada o perdida") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                Column {
                    Text("Motivo:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        motivos.forEach { m ->
                            FilterChip(
                                selected = motivo == m,
                                onClick = { motivo = m },
                                label = { Text(m) },
                                modifier = Modifier.padding(end = 8.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colorScheme.secondary, selectedLabelColor = colorScheme.onSecondary)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val insumo = insumos.find { it.id == selectedInsumoId }
                onConfirm(selectedInsumoId, insumo?.nombre ?: descripcion, cantidad.toDoubleOrNull() ?: 0.0, motivo.ifBlank { null })
            },
                enabled = (descripcion.isNotBlank() || selectedInsumoId != null) && cantidad.toDoubleOrNull() != null && !isReporting,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                if (isReporting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                else Text("Enviar reporte", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun BaristaModuleCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val dark = isDarkModeActive()
    val accent = if (dark && (color == CafeOscuro || color == Negro || color == Terracotta || color == CoffeeBrown || color == PrimaryGreen)) VerdeClaro else color
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.12f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(26.dp)) }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun KanbanBoard(
    pedidos: List<PedidoDto>,
    updating: Int?,
    onPreparar: (Int) -> Unit,
    onEntregar: (Int) -> Unit
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
            color = CafeOscuro,
            modifier = Modifier.width(280.dp),
            pedidos = pendientes,
            updating = updating,
            onPreparar = onPreparar,
            onEntregar = onEntregar
        )
        KanbanColumn(
            title = "En preparación",
            count = enPreparacion.size,
            color = PrimaryGreen,
            modifier = Modifier.width(280.dp),
            pedidos = enPreparacion,
            updating = updating,
            onPreparar = onPreparar,
            onEntregar = onEntregar
        )
        KanbanColumn(
            title = "Terminados",
            count = terminados.size,
            color = Negro,
            modifier = Modifier.width(280.dp),
            pedidos = terminados,
            updating = updating,
            onPreparar = onPreparar,
            onEntregar = onEntregar
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
    onEntregar: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val dark = isDarkModeActive()
    val accent = when {
        !dark -> color
        color == Negro -> Color.White
        else -> VerdeClaro
    }
    val columnColor = if (dark && (color == CafeOscuro || color == Negro)) accent.copy(alpha = 0.12f) else color.copy(alpha = 0.08f)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = columnColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = accent) { Text("$count", modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium) }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Black, color = accent)
            }
            if (pedidos.isEmpty()) {
                Text("Sin órdenes", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                pedidos.forEach { pedido ->
                    KanbanOrderMini(
                        pedido = pedido,
                        updating = updating == pedido.id,
                        onPreparar = { onPreparar(pedido.id) },
                        onEntregar = { onEntregar(pedido.id) }
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
    onEntregar: () -> Unit
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
                "pendiente", "pagado" -> Button(onClick = onPreparar, enabled = !updating, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    if (updating) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                    else Text("Preparando", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                "en_preparacion" -> Button(onClick = onEntregar, enabled = !updating, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    if (updating) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                    else Text("Entregado", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                "entregado" -> Text("Entregado ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VerdeClaro)
                "cancelado" -> Text("Cancelado", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}