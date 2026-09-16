package com.example.kaffacafeteria.ui.gestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun MermasScreen(
    onBack: () -> Unit,
    viewModel: GestionViewModel = createViewModel { GestionViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMermas()
        viewModel.loadInsumos()
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Mermas") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Registrar merma", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoadingMermas -> LoadingIndicator()
            state.mermasError != null -> ErrorMessage(state.mermasError!!, viewModel::loadMermas)
            state.mermas.isEmpty() -> EmptyState("No hay mermas registradas")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.mermas, key = { it.id }) { merma ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp)) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(merma.insumo?.nombre ?: merma.descripcion, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    listOfNotNull(
                                        merma.motivo,
                                        merma.created_at?.take(10)
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("−${merma.cantidad}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                                if (merma.insumo?.unidad_medida != null) {
                                    Text(merma.insumo.unidad_medida!!, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var descripcion by remember { mutableStateOf("") }
        var cantidad by remember { mutableStateOf("") }
        var selectedInsumoId by remember { mutableStateOf<Int?>(null) }
        var motivo by remember { mutableStateOf("") }
        var insumoExpanded by remember { mutableStateOf(false) }
        val motivos = listOf("Vencido", "Dañado", "No vendido", "Derrame", "Otro")

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Registrar Merma", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Las mermas son pérdidas: productos vencidos o que no se vendieron.", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    if (state.insumos.isNotEmpty()) {
                        ExposedDropdownMenuBox(expanded = insumoExpanded, onExpandedChange = { insumoExpanded = it }) {
                            OutlinedTextField(value = state.insumos.find { it.id == selectedInsumoId }?.nombre ?: "Seleccionar insumo", onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = insumoExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                            ExposedDropdownMenu(expanded = insumoExpanded, onDismissRequest = { insumoExpanded = false }) {
                                state.insumos.forEach { insumo -> DropdownMenuItem(text = { Text(insumo.nombre) }, onClick = { selectedInsumoId = insumo.id; insumoExpanded = false }) }
                            }
                        }
                    } else {
                        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción del insumo") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    }
                    OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad perdida") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    Column {
                        Text("Motivo de la pérdida:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row {
                            motivos.forEach { m ->
                                FilterChip(
                                    selected = motivo == m,
                                    onClick = { motivo = m },
                                    label = { Text(m) },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.error, selectedLabelColor = MaterialTheme.colorScheme.onError)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val insumo = state.insumos.find { it.id == selectedInsumoId }
                    viewModel.createMerma(insumo?.nombre ?: descripcion, cantidad.toDoubleOrNull() ?: 0.0, selectedInsumoId, motivo.ifBlank { null })
                    showCreateDialog = false
                },
                    enabled = (descripcion.isNotBlank() || selectedInsumoId != null) && cantidad.toDoubleOrNull() != null && !state.isCreatingMerma,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)) {
                    if (state.isCreatingMerma) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onError, strokeWidth = 2.dp)
                    else Text("Registrar", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") } }
        )
    }
}