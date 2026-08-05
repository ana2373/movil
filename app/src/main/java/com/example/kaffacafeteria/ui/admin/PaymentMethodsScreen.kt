package com.example.kaffacafeteria.ui.admin

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
import androidx.compose.ui.unit.dp
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
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadMediosPago() }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Métodos de Pago") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Crear método", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoading && state.mediosPago.isEmpty() -> LoadingIndicator()
            state.error != null -> ErrorMessage(message = state.error!!, onRetry = { viewModel.loadMediosPago() })
            state.mediosPago.isEmpty() -> EmptyState("No hay métodos de pago")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.mediosPago, key = { it.id }) { method ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(if (method.esVirtual) "Virtual - requiere comprobante" else "Presencial", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.deleteCategoria(method.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var nombre by remember { mutableStateOf("") }
        var esVirtual by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nuevo Método de Pago", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = esVirtual, onCheckedChange = { esVirtual = it }, colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary))
                        Text("Es virtual (requiere comprobante)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.createMedioPago(MedioPagoRequest(nombre, esVirtual)); showCreateDialog = false },
                    enabled = nombre.isNotBlank() && !state.isLoading, colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) { Text("Crear", color = colorScheme.onSecondary) }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") } }
        )
    }
}
