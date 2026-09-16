package com.example.kaffacafeteria.ui.gestion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.data.remote.dto.CompraDetalleDto
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprasScreen(
    onBack: () -> Unit,
    viewModel: ComprasViewModel = createViewModel { ComprasViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Compras a Proveedores") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Nueva compra", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        val total = state.compras.sumOf { it.total?.toDoubleOrNull() ?: 0.0 }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Total comprado", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text("$${"%.2f".format(total)}", fontWeight = FontWeight.Black, color = ink(SecondaryGreen))
            }
        }

        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(state.error!!, viewModel::load)
            state.compras.isEmpty() -> EmptyState("No hay compras registradas")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.compras, key = { it.id }) { compra ->
                    CompraCard(compra = compra, colorScheme = colorScheme)
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCompraDialog(
            proveedores = state.proveedores,
            insumos = state.insumos,
            isLoading = state.isCreating,
            onDismiss = { showCreateDialog = false },
            onConfirm = { proveedorId, numeroFactura, detalles ->
                viewModel.create(proveedorId, numeroFactura, detalles)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CompraCard(
    compra: com.example.kaffacafeteria.data.remote.dto.CompraDto,
    colorScheme: ColorScheme
) {
    var expanded by remember { mutableStateOf(false) }

    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = ink(SecondaryGreen).copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = ink(SecondaryGreen), modifier = Modifier.size(24.dp)) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(compra.proveedor?.nombre ?: compra.proveedorId?.let { "Proveedor #$it" } ?: "Proveedor sin identificar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOfNotNull(
                            compra.numeroFactura?.let { "Factura $it" },
                            compra.created_at?.take(10)
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant
                    )
                }
                Text("$${"%.2f".format(compra.total?.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.Bold, color = ink(SecondaryGreen))
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = colorScheme.onSurfaceVariant)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text("Productos comprados:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    val detalles = compra.detalles ?: emptyList()
                    if (detalles.isEmpty()) {
                        Text("Sin detalles registrados", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    } else {
                        detalles.forEach { detalle ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(detalle.insumo?.nombre ?: "Insumo #${detalle.insumoId}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${detalle.cantidad ?: "—"} × $${"%.2f".format(detalle.precioUnitario?.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                }
                                Text("$${"%.2f".format(detalle.subtotal?.toDoubleOrNull() ?: (detalle.cantidad?.toDoubleOrNull() ?: 0.0) * (detalle.precioUnitario?.toDoubleOrNull() ?: 0.0))}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    compra.pagos?.let { pagos ->
                        if (pagos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                            Text("Pagos:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurfaceVariant)
                            pagos.forEach { pago ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    Text(pago.medio_pago?.nombre ?: "Pago #${pago.medioPagoId}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text("$${"%.2f".format(pago.monto.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    compra.proveedor?.convenio?.let { convenio ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Handshake, contentDescription = null, tint = ink(SecondaryGreen), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Convenio: $convenio", style = MaterialTheme.typography.bodySmall, color = ink(SecondaryGreen))
                        }
                    }
                    compra.proveedor?.let { proveedor ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(listOfNotNull(proveedor.telefono, proveedor.email).joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCompraDialog(
    proveedores: List<com.example.kaffacafeteria.data.remote.dto.ProveedorDto>,
    insumos: List<com.example.kaffacafeteria.data.remote.dto.InsumoDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, String?, List<CompraDetalleDto>) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedProveedorId by remember { mutableStateOf<Int?>(null) }
    var numeroFactura by remember { mutableStateOf("") }
    var proveedorExpanded by remember { mutableStateOf(false) }

    var selectedInsumoId by remember { mutableStateOf<Int?>(null) }
    var cantidad by remember { mutableStateOf("") }
    var precioUnitario by remember { mutableStateOf("") }
    var insumoExpanded by remember { mutableStateOf(false) }
    var detalles by remember { mutableStateOf<List<CompraDetalleDto>>(emptyList()) }

    fun calcularSubtotal(): Double {
        val cant = cantidad.toDoubleOrNull() ?: 0.0
        val precio = precioUnitario.toDoubleOrNull() ?: 0.0
        return cant * precio
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Compra", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = proveedorExpanded, onExpandedChange = { proveedorExpanded = it }) {
                    OutlinedTextField(value = proveedores.find { it.id == selectedProveedorId }?.nombre ?: "Seleccionar proveedor", onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = proveedorExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    ExposedDropdownMenu(expanded = proveedorExpanded, onDismissRequest = { proveedorExpanded = false }) {
                        proveedores.forEach { p -> DropdownMenuItem(text = { Text(p.nombre) }, onClick = { selectedProveedorId = p.id; proveedorExpanded = false }) }
                    }
                }
                OutlinedTextField(value = numeroFactura, onValueChange = { numeroFactura = it }, label = { Text("N° factura (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))

                HorizontalDivider()
                Text("Agregar producto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (insumos.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = insumoExpanded, onExpandedChange = { insumoExpanded = it }) {
                        OutlinedTextField(value = insumos.find { it.id == selectedInsumoId }?.nombre ?: "Seleccionar insumo/producto", onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = insumoExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                        ExposedDropdownMenu(expanded = insumoExpanded, onDismissRequest = { insumoExpanded = false }) {
                            insumos.forEach { i -> DropdownMenuItem(text = { Text(i.nombre) }, onClick = { selectedInsumoId = i.id; insumoExpanded = false }) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                        OutlinedTextField(value = precioUnitario, onValueChange = { precioUnitario = it }, label = { Text("Precio unit. $") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    }
                    Button(onClick = {
                        if (selectedInsumoId != null && cantidad.toDoubleOrNull() != null && precioUnitario.toDoubleOrNull() != null) {
                            detalles = detalles + CompraDetalleDto(
                                id = null,
                                insumoId = selectedInsumoId,
                                cantidad = cantidad,
                                precioUnitario = precioUnitario,
                                subtotal = calcularSubtotal().toString(),
                                insumo = insumos.find { it.id == selectedInsumoId }
                            )
                            selectedInsumoId = null; cantidad = ""; precioUnitario = ""
                        }
                    }, enabled = selectedInsumoId != null && cantidad.toDoubleOrNull() != null && precioUnitario.toDoubleOrNull() != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) { Text("Agregar producto", color = colorScheme.onSecondary) }
                } else {
                    Text("No hay insumos registrados", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                }

                if (detalles.isNotEmpty()) {
                    HorizontalDivider()
                    Text("Productos de la compra:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    detalles.forEachIndexed { index, d ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(d.insumo?.nombre ?: "Insumo #${d.insumoId}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("${d.cantidad ?: "—"} × $${"%.2f".format(d.precioUnitario?.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            }
                            Text("$${"%.2f".format(d.subtotal?.toDoubleOrNull() ?: (d.cantidad?.toDoubleOrNull() ?: 0.0) * (d.precioUnitario?.toDoubleOrNull() ?: 0.0))}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { detalles = detalles.filterIndexed { i, _ -> i != index } }) { Icon(Icons.Default.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { selectedProveedorId?.let { onConfirm(it, numeroFactura, detalles) } },
                enabled = selectedProveedorId != null && detalles.isNotEmpty() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = fill(SecondaryGreen), contentColor = Color.White)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Guardar Compra", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}