package com.example.kaffacafeteria.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
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
    var editingProduct by remember { mutableStateOf<ProductoDto?>(null) }
    var productForInsumos by remember { mutableStateOf<ProductoDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProductos()
        viewModel.loadCategorias()
        viewModel.loadInsumos()
    }

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
                            if (!product.imagen.isNullOrBlank()) {
                                AsyncImage(
                                    model = product.imagen,
                                    contentDescription = product.nombre,
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(product.categoria?.nombre ?: "Sin categoría", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                Text("$${"%.2f".format(product.precioVenta.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.secondary, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { editingProduct = product }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colorScheme.primary) }
                            IconButton(onClick = { productForInsumos = product }) { Icon(Icons.Default.LocalDrink, contentDescription = "Insumos", tint = SecondaryGreen) }
                            IconButton(onClick = { viewModel.deleteProducto(product.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        ProductFormDialog(
            categorias = state.categorias,
            isLoading = state.isLoading,
            initialProduct = null,
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, descripcion, precioStr, categoriaId, imagenUri ->
                val precio = precioStr.toDoubleOrNull() ?: 0.0
                viewModel.createProducto(ProductoRequest(nombre = nombre, descripcion = descripcion.ifBlank { null }, precioVenta = precio, categoriaId = categoriaId, imagen = imagenUri?.toString()))
                showCreateDialog = false
            }
        )
    }

    editingProduct?.let { product ->
        ProductFormDialog(
            categorias = state.categorias,
            isLoading = state.isLoading,
            initialProduct = product,
            onDismiss = { editingProduct = null },
            onConfirm = { nombre, descripcion, precioStr, categoriaId, imagenUri ->
                val precio = precioStr.toDoubleOrNull() ?: 0.0
                viewModel.updateProducto(product.id, ProductoRequest(nombre = nombre, descripcion = descripcion.ifBlank { null }, precioVenta = precio, categoriaId = categoriaId, imagen = imagenUri?.toString() ?: product.imagen))
                editingProduct = null
            }
        )
    }

    productForInsumos?.let { product ->
        ProductInsumosDialog(
            product = product,
            insumos = state.insumos,
            recetas = state.recetas,
            isLoading = state.isLoading,
            onDismiss = { productForInsumos = null },
            onAddInsumo = { insumoId, cantidad ->
                viewModel.addInsumoToProducto(product.id, insumoId, cantidad)
            },
            onRemoveInsumo = { recetaId ->
                viewModel.removeReceta(recetaId, product.id)
            }
        )
        LaunchedEffect(product.id) { viewModel.loadRecetas(product.id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    categorias: List<com.example.kaffacafeteria.data.remote.dto.CategoriaDto>,
    isLoading: Boolean,
    initialProduct: ProductoDto?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Uri?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var nombre by remember { mutableStateOf(initialProduct?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(initialProduct?.descripcion ?: "") }
    var precio by remember { mutableStateOf(initialProduct?.precioVenta?.toDoubleOrNull()?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var selectedCategoriaId by remember { mutableStateOf<Int?>(initialProduct?.categoriaId ?: initialProduct?.categoria?.id) }
    var expanded by remember { mutableStateOf(false) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> imagenUri = uri }

    val isEditing = initialProduct != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Producto" else "Nuevo Producto", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción (opcional)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio de venta") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(value = categorias.find { it.id == selectedCategoriaId }?.nombre ?: "Seleccionar categoría", onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categorias.forEach { cat -> DropdownMenuItem(text = { Text(cat.nombre) }, onClick = { selectedCategoriaId = cat.id; expanded = false }) }
                    }
                }

                Text("Imagen del producto:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val currentUri = imagenUri ?: initialProduct?.imagen?.let { Uri.parse(it) }
                    if (currentUri != null) {
                        AsyncImage(model = currentUri, contentDescription = "Imagen", modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                    } else {
                        Surface(shape = RoundedCornerShape(8.dp), color = colorScheme.surfaceVariant, modifier = Modifier.size(56.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Image, contentDescription = null, tint = colorScheme.onSurfaceVariant) }
                        }
                    }
                    OutlinedButton(onClick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Elegir imagen")
                    }
                    if (currentUri != null) {
                        TextButton(onClick = { imagenUri = null }) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { selectedCategoriaId?.let { onConfirm(nombre, descripcion, precio, it, imagenUri) } },
                enabled = nombre.isNotBlank() && precio.isNotBlank() && selectedCategoriaId != null && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                else Text(if (isEditing) "Guardar" else "Crear", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductInsumosDialog(
    product: ProductoDto,
    insumos: List<com.example.kaffacafeteria.data.remote.dto.InsumoDto>,
    recetas: List<com.example.kaffacafeteria.data.remote.dto.RecetaDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAddInsumo: (Int, Double) -> Unit,
    onRemoveInsumo: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedInsumoId by remember { mutableStateOf<Int?>(null) }
    var cantidad by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val disponibles = insumos.filter { i -> recetas.none { r -> r.insumo?.id == i.id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insumos de ${product.nombre}", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Insumos actuales:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (recetas.isEmpty()) {
                    Text("Este producto no tiene insumos asociados.", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                } else {
                    recetas.forEach { receta ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Inventory, contentDescription = null, tint = ink(PrimaryGreen), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(receta.insumo?.nombre ?: "Insumo #${receta.insumo?.id}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Cantidad: ${receta.cantidad ?: "—"}${receta.insumo?.unidad_medida?.let { " $it" } ?: ""}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { receta.id?.let { onRemoveInsumo(it) } }) { Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }

                HorizontalDivider()

                Text("Agregar insumo:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (disponibles.isEmpty()) {
                    Text("Todos los insumos ya están asociados.", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(value = insumos.find { it.id == selectedInsumoId }?.nombre ?: "Seleccionar insumo", onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            disponibles.forEach { insumo ->
                                DropdownMenuItem(text = { Text(insumo.nombre) }, onClick = { selectedInsumoId = insumo.id; expanded = false })
                            }
                        }
                    }
                    OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    Button(
                        onClick = { selectedInsumoId?.let { onAddInsumo(it, cantidad.toDoubleOrNull() ?: 0.0) } },
                        enabled = selectedInsumoId != null && cantidad.toDoubleOrNull() != null && !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)
                    ) { Text("Agregar", color = colorScheme.onSecondary) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}