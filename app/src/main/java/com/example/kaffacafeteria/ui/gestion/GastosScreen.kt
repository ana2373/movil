package com.example.kaffacafeteria.ui.gestion

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

data class GastoCategoria(val nombre: String, val icono: ImageVector, val color: Color)

val GastoCategorias = listOf(
    GastoCategoria("Servicios públicos", Icons.Default.Lightbulb, CafeOscuro),
    GastoCategoria("Arriendo", Icons.Default.Home, CoffeeBrown),
    GastoCategoria("Nómina", Icons.Default.People, Negro),
    GastoCategoria("Mantenimiento", Icons.Default.Build, SecondaryGreen),
    GastoCategoria("Impuestos", Icons.Default.Receipt, Negro),
    GastoCategoria("Insumos", Icons.Default.Inventory, PrimaryGreen),
    GastoCategoria("Otro", Icons.Default.MoreHoriz, TextGray)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosScreen(
    onBack: () -> Unit,
    viewModel: GestionViewModel = createViewModel { GestionViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMediosPago()
        viewModel.loadGastos()
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Gastos Generales") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Registrar gasto", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        val total = state.gastos.sumOf { it.montoTotal }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total de gastos", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                    Text("${state.gastos.size} registros", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                }
                Text("$${"%.2f".format(total)}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
            }
        }

        when {
            state.isLoadingGastos -> LoadingIndicator()
            state.gastosError != null -> ErrorMessage(state.gastosError!!, viewModel::loadGastos)
            state.gastos.isEmpty() -> EmptyState("No hay gastos registrados")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.gastos, key = { it.id }) { gasto ->
                    val cat = GastoCategorias.find { it.nombre == gasto.categoria } ?: GastoCategorias.last()
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = ink(cat.color).copy(alpha = 0.12f), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(cat.icono, contentDescription = null, tint = ink(cat.color), modifier = Modifier.size(22.dp)) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(gasto.descripcion, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(gasto.categoria ?: "Sin categoría", style = MaterialTheme.typography.bodySmall, color = ink(cat.color))
                                Text(gasto.created_at?.take(10) ?: "", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            }
                            Text("$${"%.2f".format(gasto.montoTotal)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var descripcion by remember { mutableStateOf("") }
        var monto by remember { mutableStateOf("") }
        var categoria by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Registrar Gasto", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    Column {
                        Text("Categoría:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row {
                            GastoCategorias.forEach { cat ->
                                FilterChip(
                                    selected = categoria == cat.nombre,
                                    onClick = { categoria = cat.nombre },
                                    label = { Text(cat.nombre) },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = cat.color,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.createGasto(descripcion, monto.toDoubleOrNull() ?: 0.0, categoria.ifBlank { "Otro" }); showCreateDialog = false },
                    enabled = descripcion.isNotBlank() && monto.toDoubleOrNull() != null && !state.isCreatingGasto,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                    if (state.isCreatingGasto) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                    else Text("Registrar", color = colorScheme.onSecondary)
                }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") } }
        )
    }
}