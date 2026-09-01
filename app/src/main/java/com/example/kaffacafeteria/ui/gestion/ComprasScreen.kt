package com.example.kaffacafeteria.ui.gestion

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
<<<<<<< HEAD
import androidx.compose.ui.graphics.Color
=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
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
fun ComprasScreen(
    onBack: () -> Unit,
<<<<<<< HEAD
    viewModel: ComprasViewModel = createViewModel { ComprasViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Compras a Proveedores") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        val total = state.compras.sumOf { it.total.toDoubleOrNull() ?: 0.0 }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Total comprado", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text("$${"%.2f".format(total)}", fontWeight = FontWeight.Black, color = Color(0xFF0891B2))
            }
        }

        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(state.error!!, viewModel::load)
            state.compras.isEmpty() -> EmptyState("No hay compras registradas")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.compras) { compra ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF0891B2).copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF0891B2), modifier = Modifier.size(24.dp)) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(compra.proveedor?.nombre ?: "Proveedor #${compra.proveedorId}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(compra.created_at ?: "", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            }
                            Text("$${"%.2f".format(compra.total.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.Bold, color = Color(0xFF0891B2))
=======
    viewModel: GestionViewModel = createViewModel { GestionViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadGastos() }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Gastos") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Registrar gasto", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoadingGastos -> LoadingIndicator()
            state.gastosError != null -> ErrorMessage(state.gastosError!!, viewModel::loadGastos)
            state.gastos.isEmpty() -> EmptyState("No hay gastos registrados")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.gastos) { gasto ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(gasto.descripcion, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(gasto.created_at ?: "", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            }
                            Text("$${"%.2f".format(gasto.monto.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
                        }
                    }
                }
            }
        }
    }
<<<<<<< HEAD
=======

    if (showCreateDialog) {
        var descripcion by remember { mutableStateOf("") }
        var monto by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Registrar Gasto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.createGasto(descripcion, monto.toDoubleOrNull() ?: 0.0); showCreateDialog = false },
                    enabled = descripcion.isNotBlank() && monto.toDoubleOrNull() != null && !state.isCreatingGasto,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                    if (state.isCreatingGasto) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                    else Text("Registrar", color = colorScheme.onSecondary)
                }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") } }
        )
    }
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
}
