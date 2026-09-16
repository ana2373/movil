package com.example.kaffacafeteria.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.data.remote.dto.MedioPagoDto
import com.example.kaffacafeteria.data.remote.dto.MedioPagoRequest
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = createViewModel { AdminViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val dark = isDarkModeActive()
    val methodAccent: (Boolean) -> Color = { esVirtual -> if (esVirtual) (if (dark) VerdeClaro else CafeOscuro) else PrimaryGreen }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingMethod by remember { mutableStateOf<MedioPagoDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadMediosPago() }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            snackbarHostState.showSnackbar(state.successMessage!!)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error!!)
            viewModel.clearMessages()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Métodos de Pago") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Crear método", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickMethod(
                    modifier = Modifier.weight(1f),
                    nombre = "Efectivo",
                    esVirtual = false,
                    onClick = {
                        if (state.mediosPago.none { it.nombre.equals("Efectivo", true) }) {
                            viewModel.createMedioPago(MedioPagoRequest("Efectivo", false))
                        }
                    }
                )
                QuickMethod(
                    modifier = Modifier.weight(1f),
                    nombre = "Nequi",
                    esVirtual = true,
                    onClick = {
                        if (state.mediosPago.none { it.nombre.equals("Nequi", true) }) {
                            viewModel.createMedioPago(MedioPagoRequest("Nequi", true))
                        }
                    }
                )
            }

            when {
                state.isLoading && state.mediosPago.isEmpty() -> LoadingIndicator()
                state.mediosPago.isEmpty() && state.error != null -> ErrorMessage(message = state.error!!, onRetry = { viewModel.loadMediosPago() })
                state.mediosPago.isEmpty() -> EmptyState("No hay métodos de pago")
                else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.mediosPago, key = { it.id }) { method ->
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(12.dp), color = methodAccent(method.esVirtual).copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(if (method.esVirtual) Icons.Default.Smartphone else Icons.Default.Payments, contentDescription = null, tint = methodAccent(method.esVirtual), modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(method.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(if (method.esVirtual) "Virtual - requiere comprobante" else "Presencial (efectivo)", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { editingMethod = method }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colorScheme.primary) }
                                IconButton(onClick = { viewModel.deleteMedioPago(method.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
    }

    if (showCreateDialog) {
        PaymentMethodFormDialog(
            initial = null,
            isLoading = state.isLoading,
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, esVirtual ->
                viewModel.createMedioPago(MedioPagoRequest(nombre, esVirtual))
                showCreateDialog = false
            }
        )
    }

    editingMethod?.let { method ->
        PaymentMethodFormDialog(
            initial = method,
            isLoading = state.isLoading,
            onDismiss = { editingMethod = null },
            onConfirm = { nombre, esVirtual ->
                viewModel.updateMedioPago(method.id, MedioPagoRequest(nombre, esVirtual))
                editingMethod = null
            }
        )
    }
}

@Composable
private fun QuickMethod(
    modifier: Modifier = Modifier,
    nombre: String,
    esVirtual: Boolean,
    onClick: () -> Unit
) {
    val dark = isDarkModeActive()
    val accent = if (esVirtual) (if (dark) VerdeClaro else CafeOscuro) else PrimaryGreen
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent)
    ) {
        Icon(if (esVirtual) Icons.Default.Smartphone else Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(nombre)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodFormDialog(
    initial: MedioPagoDto?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var nombre by remember { mutableStateOf(initial?.nombre ?: "") }
    var esVirtual by remember { mutableStateOf(initial?.esVirtual ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo Método de Pago" else "Editar Método de Pago", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = esVirtual, onCheckedChange = { esVirtual = it }, colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary))
                    Text("Virtual (requiere comprobante)")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, esVirtual) },
                enabled = nombre.isNotBlank() && !isLoading, colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) { Text(if (initial == null) "Crear" else "Guardar", color = colorScheme.onSecondary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}