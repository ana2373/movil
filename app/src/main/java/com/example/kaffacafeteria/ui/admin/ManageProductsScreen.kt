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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.data.remote.dto.ProductoRequest
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = createViewModel { AdminViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadProductos(); viewModel.loadCategorias() }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Gestionar Productos") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Crear producto", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoading && state.productos.isEmpty() -> LoadingIndicator()
            state.error != null -> ErrorMessage(message = state.error!!, onRetry = { viewModel.loadProductos() })
            state.productos.isEmpty() -> EmptyState("No hay productos")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.productos, key = { it.id }) { product ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(product.categoria?.nombre ?: "Sin categoría", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                Text("$${"%.2f".format(product.precioVenta.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.secondary, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.deleteProducto(product.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProductDialog(
            categorias = state.categorias,
            isLoading = state.isLoading,
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, precioStr, categoriaId ->
                val precio = precioStr.toDoubleOrNull() ?: 0.0
                viewModel.createProducto(ProductoRequest(nombre = nombre, descripcion = null, precioVenta = precio, categoriaId = categoriaId))
                showCreateDialog = false
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateProductDialog(
    categorias: List<com.example.kaffacafeteria.data.remote.dto.CategoriaDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var selectedCategoriaId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Producto", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio de venta") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(value = categorias.find { it.id == selectedCategoriaId }?.nombre ?: "Seleccionar categoría", onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categorias.forEach { cat -> DropdownMenuItem(text = { Text(cat.nombre) }, onClick = { selectedCategoriaId = cat.id; expanded = false }) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { selectedCategoriaId?.let { onConfirm(nombre, precio, it) } },
                enabled = nombre.isNotBlank() && precio.isNotBlank() && selectedCategoriaId != null && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                else Text("Crear", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
