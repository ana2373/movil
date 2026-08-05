package com.example.kaffacafeteria.ui.barista

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.ui.components.EmptyState
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
            title = { Text("Caja") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(state.error!!, viewModel::loadStatus)
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Estado de Caja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estado:", color = colorScheme.onSurfaceVariant)
                                Surface(shape = RoundedCornerShape(8.dp), color = (if (state.isOpen) colorScheme.secondary else colorScheme.onSurfaceVariant).copy(alpha = 0.1f)) {
                                    Text(if (state.isOpen) "Abierta" else "Cerrada", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = if (state.isOpen) colorScheme.secondary else colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            state.caja?.let { caja ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Saldo inicial:", color = colorScheme.onSurfaceVariant)
                                    Text("$${"%.2f".format(caja.montoInicial.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Saldo actual:", color = colorScheme.onSurfaceVariant)
                                    Text("$${"%.2f".format(caja.montoFinal?.toDoubleOrNull() ?: caja.montoInicial.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.Bold, color = colorScheme.secondary)
                                }
                            }
                        }
                    }

                    if (state.isOpen) {
                        Button(onClick = { showCloseDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.tertiary, contentColor = colorScheme.onTertiary)) { Text("Cerrar Caja", color = colorScheme.onTertiary) }
                    } else {
                        Button(onClick = { showOpenDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) { Text("Abrir Caja", color = colorScheme.onSecondary) }
                    }

                    Text("Movimientos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                    if (state.movimientos.isEmpty()) EmptyState("No hay movimientos")
                    else LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.movimientos) { mov ->
                            Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surface), shape = RoundedCornerShape(8.dp)) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mov.concepto ?: "", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(mov.created_at ?: "", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                    }
                                    Text("${if (mov.tipo == "ingreso") "+" else "-"}$${"%.2f".format(mov.monto.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.Bold, color = if (mov.tipo == "ingreso") colorScheme.secondary else MaterialTheme.colorScheme.error)
                                }
                            }
                        }
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
            text = { OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Saldo inicial") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant)) },
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
                    Text("Saldo actual: $${"%.2f".format(state.caja?.montoFinal?.toDoubleOrNull() ?: state.caja?.montoInicial?.toDoubleOrNull() ?: 0.0)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Saldo final") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
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
