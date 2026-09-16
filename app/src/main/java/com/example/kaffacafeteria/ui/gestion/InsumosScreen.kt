package com.example.kaffacafeteria.ui.gestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.data.remote.dto.InsumoDto
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsumosScreen(
    onBack: () -> Unit,
    isAdmin: Boolean = true,
    viewModel: InsumosViewModel = createViewModel { InsumosViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingInsumo by remember { mutableStateOf<InsumoDto?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Insumos") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { if (isAdmin) IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Agregar insumo", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(state.error!!, viewModel::load)
            state.insumos.isEmpty() -> EmptyState("No hay insumos registrados")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.insumos, key = { it.id }) { insumo ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = ink(PrimaryGreen).copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Inventory, contentDescription = null, tint = ink(PrimaryGreen), modifier = Modifier.size(24.dp)) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(insumo.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(insumo.unidad_medida ?: "Sin unidad", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(insumo.stock_actual ?: "0", fontWeight = FontWeight.Black, color = if ((insumo.stock_actual?.toDoubleOrNull() ?: 0.0) > 0.0) PrimaryGreen else MaterialTheme.colorScheme.error)
                                Text("stock", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            }
                            if (isAdmin) {
                                IconButton(onClick = { editingInsumo = insumo }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colorScheme.primary) }
                                IconButton(onClick = { viewModel.delete(insumo.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        InsumoFormDialog(
            initial = null,
            isLoading = state.isCreating,
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, stockStr, unidad ->
                viewModel.create(nombre.trim(), stockStr.toDoubleOrNull(), unidad.ifBlank { null })
                showCreateDialog = false
            }
        )
    }

    editingInsumo?.let { insumo ->
        InsumoFormDialog(
            initial = insumo,
            isLoading = state.isCreating,
            onDismiss = { editingInsumo = null },
            onConfirm = { nombre, stockStr, unidad ->
                viewModel.update(insumo.id, nombre.trim(), stockStr.toDoubleOrNull(), unidad.ifBlank { null })
                editingInsumo = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InsumoFormDialog(
    initial: InsumoDto?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var nombre by remember { mutableStateOf(initial?.nombre ?: "") }
    var stock by remember { mutableStateOf(initial?.stock_actual ?: "") }
    var unidad by remember { mutableStateOf(initial?.unidad_medida ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo Insumo" else "Editar Insumo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock actual") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text("Unidad de medida (ej. kg, l, und)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, stock, unidad) },
                enabled = nombre.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                else Text(if (initial == null) "Crear" else "Guardar", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}