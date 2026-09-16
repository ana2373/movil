package com.example.kaffacafeteria.ui.pos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel
import com.example.kaffacafeteria.util.imagenLocal
import com.example.kaffacafeteria.util.toImageUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(
    onBack: () -> Unit,
    onOrderCreated: (Int) -> Unit = {},
    viewModel: POSViewModel = createViewModel { POSViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(state.createdOrderId) {
        state.createdOrderId?.let { id ->
            onOrderCreated(id)
            viewModel.clearCart()
        }
    }

    var showPaymentDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Nueva Venta") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { Text("Total: $${"%.2f".format(viewModel.cartTotal)}", style = MaterialTheme.typography.titleMedium, color = colorScheme.onPrimary) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                LazyRow(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = state.selectedCategoria == null, onClick = { viewModel.selectCategoria(null) }, label = { Text("Todas") })
                    }
                    items(state.categorias) { cat ->
                        FilterChip(selected = state.selectedCategoria == cat.id, onClick = { viewModel.selectCategoria(cat.id) }, label = { Text(cat.nombre) })
                    }
                }

                when {
                    state.isLoadingProductos && state.productos.isEmpty() -> LoadingIndicator()
                    state.error != null -> ErrorMessage(message = state.error!!, onRetry = { viewModel.loadProductos() })
                    state.filteredProductos.isEmpty() -> EmptyState("No hay productos")
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)

                    ) {
                        items(state.filteredProductos) { product ->
                            Card(modifier = Modifier.fillMaxWidth().clickable { viewModel.addToCart(product) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                                Column {
                                    val localImg = product.imagenLocal()
                                    if (!product.imagen.isNullOrBlank()) {
                                        AsyncImage(model = product.imagen.toImageUrl(), error = localImg?.let { painterResource(it) }, fallback = localImg?.let { painterResource(it) }, contentDescription = null, modifier = Modifier.fillMaxWidth().height(100.dp), contentScale = ContentScale.Crop)
                                    } else if (localImg != null) {
                                        Image(painter = painterResource(localImg), contentDescription = null, modifier = Modifier.fillMaxWidth().height(100.dp), contentScale = ContentScale.Crop)
                                    } else {
                                        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.Coffee, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(32.dp)) }
                                    }
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(product.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("$${"%.2f".format(product.precioVenta.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.secondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.width(280.dp)) {
                Surface(color = colorScheme.surface, modifier = Modifier.fillMaxHeight(), shadowElevation = 4.dp) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Text("Carrito", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colorScheme.outlineVariant)
                        if (viewModel.cartItems.isEmpty()) {
                            EmptyState("Carrito vacío")
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(viewModel.cartItems) { item ->
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.producto.nombre, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("$${"%.2f".format(item.subtotal)}", style = MaterialTheme.typography.bodySmall, color = colorScheme.secondary)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { viewModel.updateCantidad(item.producto.id, item.cantidad - 1) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Remove, contentDescription = "Restar", modifier = Modifier.size(16.dp)) }
                                            Text(item.cantidad.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { viewModel.updateCantidad(item.producto.id, item.cantidad + 1) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Add, contentDescription = "Sumar", modifier = Modifier.size(16.dp)) }
                                        }
                                        IconButton(onClick = { viewModel.removeFromCart(item.producto.id) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colorScheme.outlineVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total:", fontWeight = FontWeight.Bold)
                            Text("$${"%.2f".format(viewModel.cartTotal)}", fontWeight = FontWeight.Bold, color = colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showPaymentDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary), enabled = viewModel.cartItems.isNotEmpty()) { Text("Cobrar", color = colorScheme.onSecondary) }
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            total = viewModel.cartTotal,
            paymentMethods = state.mediosPago,
            isLoading = state.isLoading,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { medioPagoId, comprobanteUrl ->
                viewModel.selectMedioPago(medioPagoId)
                viewModel.updateComprobante(comprobanteUrl ?: "")
                viewModel.createOrder()
                showPaymentDialog = false
            }
        )
    }
}

@Composable
fun PaymentDialog(
    total: Double,
    paymentMethods: List<com.example.kaffacafeteria.domain.model.MedioPago>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, String?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedMethodId by remember { mutableIntStateOf(paymentMethods.firstOrNull()?.id ?: 0) }
    var comprobanteUrl by remember { mutableStateOf("") }
    var comprobanteUri by remember { mutableStateOf<Uri?>(null) }
    var urlError by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> comprobanteUri = uri }

    val selectedMethod = paymentMethods.find { it.id == selectedMethodId }
    val needsComprobante = selectedMethod?.esVirtual == true
    val noMethods = paymentMethods.isEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Método de Pago", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Total a pagar: $${"%.2f".format(total)}", fontWeight = FontWeight.Bold)
                if (noMethods) {
                    Text("No hay métodos de pago configurados. El administrador debe crearlos en Panel Admin → Métodos de pago.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                } else {
                    paymentMethods.forEach { method ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { selectedMethodId = method.id }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedMethodId == method.id, onClick = { selectedMethodId = method.id }, colors = RadioButtonDefaults.colors(selectedColor = colorScheme.primary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(method.nombre)
                            if (method.esVirtual) { Spacer(modifier = Modifier.width(8.dp)); Text("(virtual)", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant) }
                        }
                    }
                }
                if (needsComprobante) {
                    Text("Comprobante de pago *", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (comprobanteUri != null) {
                            AsyncImage(model = comprobanteUri, contentDescription = "Comprobante", modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            TextButton(onClick = {
                                comprobanteUri = null
                                comprobanteUrl = ""
                            }) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
                        } else {
                            OutlinedButton(onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Subir comprobante")
                            }
                        }
                    }
                    OutlinedTextField(value = comprobanteUrl, onValueChange = { comprobanteUrl = it; urlError = null }, label = { Text("O pega la URL del comprobante") },
                        isError = urlError != null, supportingText = urlError?.let { { Text(it) } }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val resolved = comprobanteUri?.toString() ?: comprobanteUrl
                if (needsComprobante && resolved.isBlank()) urlError = "El comprobante es requerido para pagos virtuales"
                else onConfirm(selectedMethodId, resolved.ifBlank { null })
            }, enabled = !isLoading && !noMethods, colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                else Text("Confirmar Pago", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
