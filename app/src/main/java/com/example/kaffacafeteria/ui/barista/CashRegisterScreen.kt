package com.example.kaffacafeteria.ui.barista

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.data.remote.dto.CajaDto
import com.example.kaffacafeteria.data.remote.dto.MovimientoCajaDto
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashRegisterScreen(
    onBack: () -> Unit,
    viewModel: CashRegisterViewModel = createViewModel { CashRegisterViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showOpenDialog by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Caja de Cobro") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { viewModel.loadStatus() }) { Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoading && state.cajaActual == null && state.historial.isEmpty() -> LoadingIndicator()
            state.error != null && state.cajaActual == null && state.historial.isEmpty() -> ErrorMessage(state.error!!, viewModel::loadStatus)
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    state.success?.let { Text(it, color = colorScheme.secondary, style = MaterialTheme.typography.bodySmall) }

                    CajaActualCard(state = state, colorScheme = colorScheme)

                    if (state.isOpen) {
                        Button(onClick = { showCloseDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.tertiary, contentColor = colorScheme.onTertiary)) { Text("Cerrar Caja", color = colorScheme.onTertiary) }
                    } else {
                        Button(onClick = { showOpenDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) { Text("Abrir Caja", color = colorScheme.onSecondary) }
                    }

                    Text("Movimientos de la caja actual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                    if (state.movimientos.isEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(10.dp)) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    if (state.isOpen) "Aún no hay movimientos en esta caja." else "No hay una caja abierta.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        state.movimientos.forEach { mov -> MovimientoCard(mov, colorScheme) }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Registro de cajas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                    if (state.historial.isEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(10.dp)) {
                            Text("Todavía no se ha abierto ninguna caja.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        }
                    } else {
                        state.historial.forEach { caja -> HistorialCard(caja, colorScheme) }
                    }
                }
            }
        }
    }

    if (showOpenDialog) {
        var monto by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showOpenDialog = false },
            title = { Text("Abrir Caja", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Registra el saldo inicial (monto físico) con el que se abre la caja.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Saldo inicial") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.openCashRegister(monto.toDoubleOrNull() ?: 0.0); showOpenDialog = false },
                    enabled = monto.toDoubleOrNull() != null && !state.isLoading, colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) { Text("Abrir", color = colorScheme.onSecondary) }
            },
            dismissButton = { TextButton(onClick = { showOpenDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showCloseDialog) {
        var monto by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Cerrar Caja", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Saldo esperado en el sistema: $${"%.2f".format(state.saldoActual)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Saldo final (monto físico)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.closeCashRegister(monto.toDoubleOrNull() ?: 0.0); showCloseDialog = false },
                    enabled = monto.toDoubleOrNull() != null && !state.isLoading, colors = ButtonDefaults.buttonColors(containerColor = colorScheme.tertiary, contentColor = colorScheme.onTertiary)) { Text("Cerrar", color = colorScheme.onTertiary) }
            },
            dismissButton = { TextButton(onClick = { showCloseDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CajaActualCard(state: CashRegisterUiState, colorScheme: androidx.compose.material3.ColorScheme) {
    val caja = state.cajaActual
    Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.PointOfSale, contentDescription = null, tint = colorScheme.primary) }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Estado de Caja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Estado:", color = colorScheme.onSurfaceVariant)
                Surface(shape = RoundedCornerShape(8.dp), color = (if (state.isOpen) colorScheme.secondary else colorScheme.onSurfaceVariant).copy(alpha = 0.1f)) {
                    Text(if (state.isOpen) "Abierta" else "Cerrada", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = if (state.isOpen) colorScheme.secondary else colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                }
            }

            if (caja == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("No hay una caja abierta actualmente.", color = colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            } else {
                MontoRow("Abierta por:", caja.usuarioApertura?.nombre ?: "Usuario #${caja.abiertaPor ?: "-"}", colorScheme)
                if (caja.fechaApertura != null) MontoRow("Apertura:", formatFecha(caja.fechaApertura), colorScheme)
                if (caja.fechaCierre != null) MontoRow("Cierre:", formatFecha(caja.fechaCierre), colorScheme)
                MontoRow("Saldo inicial:", "$" + "%.2f".format(caja.montoApertura), colorScheme)
                MontoRow("Ingresos:", "+$" + "%.2f".format(state.totalIngresos), colorScheme, colorScheme.secondary)
                MontoRow("Egresos:", "-$" + "%.2f".format(state.totalEgresos), colorScheme, MaterialTheme.colorScheme.error)
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Saldo actual:", color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Text("$" + "%.2f".format(state.saldoActual), fontWeight = FontWeight.Bold, color = colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
private fun MontoRow(label: String, value: String, colorScheme: androidx.compose.material3.ColorScheme, valueColor: androidx.compose.ui.graphics.Color? = null) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold, color = valueColor ?: colorScheme.onSurface)
    }
}

@Composable
private fun MovimientoCard(mov: MovimientoCajaDto, colorScheme: androidx.compose.material3.ColorScheme) {
    Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mov.descripcion ?: "Movimiento", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatFecha(mov.created_at), style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
            Text("${if (mov.tipo == "ingreso") "+" else "-"}$${"%.2f".format(mov.monto.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.Bold, color = if (mov.tipo == "ingreso") colorScheme.secondary else MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun HistorialCard(caja: CajaDto, colorScheme: androidx.compose.material3.ColorScheme) {
    Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Apertura: ${formatFecha(caja.fechaApertura)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Surface(shape = RoundedCornerShape(8.dp), color = (if (caja.estaAbierta) colorScheme.secondary else colorScheme.onSurfaceVariant).copy(alpha = 0.1f)) {
                    Text(if (caja.estaAbierta) "Abierta" else "Cerrada", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = if (caja.estaAbierta) colorScheme.secondary else colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Abrió: ${caja.usuarioApertura?.nombre ?: "Usuario #${caja.abiertaPor ?: "-"}"}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            if (caja.fechaCierre != null) {
                Text("Cerró: ${caja.usuarioCierre?.nombre ?: "Usuario #${caja.cerradaPor ?: "-"}"} · ${formatFecha(caja.fechaCierre)}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saldo inicial: $" + "%.2f".format(caja.montoApertura), style = MaterialTheme.typography.bodySmall)
                Text("Ingresos: +$" + "%.2f".format(caja.totalIngresos), style = MaterialTheme.typography.bodySmall, color = colorScheme.secondary, fontWeight = FontWeight.SemiBold)
            }
            if (caja.montoCierre != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Egresos: -$" + "%.2f".format(caja.totalEgresos), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    Text("Saldo final: $" + "%.2f".format(caja.montoCierre!!), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatFecha(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"
    return raw.replace('T', ' ').take(16)
}
