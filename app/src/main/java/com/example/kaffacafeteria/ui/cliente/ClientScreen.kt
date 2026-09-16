package com.example.kaffacafeteria.ui.cliente

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.VerdeClaro
import com.example.kaffacafeteria.ui.theme.White
import com.example.kaffacafeteria.util.createViewModel
import com.example.kaffacafeteria.util.imagenLocal
import com.example.kaffacafeteria.util.toImageUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    onViewMyOrders: () -> Unit,
    viewModel: ClientViewModel = createViewModel { ClientViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showCart by remember { mutableStateOf(false) }
    var showPayment by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessages()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Surface(
            color = colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kaffa", style = MaterialTheme.typography.headlineMedium, color = colorScheme.onPrimary, fontWeight = FontWeight.Black)
                        Text("Hola, ${state.user?.nombre ?: "Cliente"} 👋", style = MaterialTheme.typography.bodyLarge, color = colorScheme.onPrimary)
                    }
                    IconButton(onClick = onViewMyOrders) {
                        Icon(Icons.Default.Receipt, contentDescription = "Mis pedidos", tint = colorScheme.onPrimary)
                    }
                    BadgedBox(
                        badge = {
                            if (state.cartCount > 0) {
                                Badge(containerColor = colorScheme.tertiary, contentColor = colorScheme.onTertiary) { Text(state.cartCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { showCart = true }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = colorScheme.onPrimary)
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::updateSearch,
            placeholder = { Text("Buscar productos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                focusedLabelColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline,
                cursorColor = colorScheme.primary
            )
        )

        LazyRow(modifier = Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = state.selectedCategoria == null, onClick = { viewModel.selectCategoria(null) }, label = { Text("Todas") }) }
            items(state.categorias) { cat ->
                FilterChip(selected = state.selectedCategoria == cat.id, onClick = { viewModel.selectCategoria(cat.id) }, label = { Text(cat.nombre) })
            }
        }

        when {
            state.isLoading && state.productos.isEmpty() -> LoadingIndicator()
            state.filteredProductos.isEmpty() -> EmptyState("No se encontraron productos")
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.filteredProductos, key = { it.id }) { product ->
                    ClientProductCard(product = product, onAdd = { viewModel.addToCart(product) })
                }
            }
        }

        if (state.cartCount > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.primary,
                shadowElevation = 8.dp,
                onClick = { showCart = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${state.cartCount} producto(s)", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onPrimary)
                        Text("$${"%.2f".format(state.cartTotal)}", style = MaterialTheme.typography.titleLarge, color = colorScheme.onPrimary, fontWeight = FontWeight.Black)
                    }
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = colorScheme.onPrimary)
                }
            }
        }
    }

    if (showCart) {
        CartSheet(
            state = state,
            onDismiss = { showCart = false },
            onUpdateCantidad = viewModel::updateCantidad,
            onRemove = viewModel::removeFromCart,
            onCheckout = { showCart = false; showPayment = true }
        )
    }

    if (showPayment) {
        ClientPaymentDialog(
            total = state.cartTotal,
            methods = state.mediosPago,
            isLoading = state.isLoading,
            onDismiss = { showPayment = false },
            onConfirm = { cashId, transferId, comprobante ->
                viewModel.createOrder(cashId, transferId, comprobante)
                showPayment = false
            }
        )
    }
}

@Composable
fun ClientProductCard(product: ProductoDto, onAdd: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            val localImg = product.imagenLocal()
            if (!product.imagen.isNullOrBlank()) {
                AsyncImage(model = product.imagen.toImageUrl(), error = localImg?.let { painterResource(it) }, fallback = localImg?.let { painterResource(it) }, contentDescription = null, modifier = Modifier.fillMaxWidth().height(110.dp), contentScale = ContentScale.Crop)
            } else if (localImg != null) {
                Image(painter = painterResource(localImg), contentDescription = null, modifier = Modifier.fillMaxWidth().height(110.dp), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Coffee, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(36.dp))
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(product.categoria?.nombre ?: "", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("$${"%.2f".format(product.precioVenta.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.titleSmall, color = VerdeClaro, fontWeight = FontWeight.Black)
                    FilledIconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(34.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = VerdeClaro, contentColor = White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartSheet(
    state: ClientUiState,
    onDismiss: () -> Unit,
    onUpdateCantidad: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onCheckout: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp)) {
            Text("Tu Carrito", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            if (state.cartItems.isEmpty()) {
                EmptyState("Tu carrito está vacío")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false).heightIn(max = 400.dp)) {
                    items(state.cartItems, key = { it.producto.id }) { item ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(colorScheme.primaryContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Coffee, contentDescription = null, tint = colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.producto.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("$${"%.2f".format(item.subtotal)}", style = MaterialTheme.typography.bodySmall, color = VerdeClaro)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(onClick = { onUpdateCantidad(item.producto.id, item.cantidad - 1) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Remove, contentDescription = "Restar", modifier = Modifier.size(16.dp)) }
                                Text(item.cantidad.toString(), modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                                FilledTonalIconButton(onClick = { onUpdateCantidad(item.producto.id, item.cantidad + 1) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Add, contentDescription = "Sumar", modifier = Modifier.size(16.dp)) }
                                IconButton(onClick = { onRemove(item.producto.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$${"%.2f".format(state.cartTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), enabled = state.cartItems.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = VerdeClaro, contentColor = White)) {
                    Text("Proceder al pago", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ClientPaymentDialog(
    total: Double,
    methods: List<com.example.kaffacafeteria.domain.model.MedioPago>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int?, Int?, String?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var paymentType by remember { mutableStateOf("efectivo") }
    var receiptUri by remember { mutableStateOf<String?>(null) }
    var receiptError by remember { mutableStateOf<String?>(null) }

    val cashMethods = methods.filter { !it.esVirtual }
    val transferMethods = methods.filter { it.esVirtual }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        receiptUri = uri?.toString()
        receiptError = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Método de pago", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Total a pagar: $${"%.2f".format(total)}", fontWeight = FontWeight.Bold, color = colorScheme.primary)

                // Opción efectivo
                Row(modifier = Modifier.fillMaxWidth().clickable { paymentType = "efectivo" }.background(if (paymentType == "efectivo") colorScheme.primaryContainer else colorScheme.surface, RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = paymentType == "efectivo", onClick = { paymentType = "efectivo" })
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Payments, contentDescription = null, tint = colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(cashMethods.firstOrNull()?.nombre ?: "Efectivo", fontWeight = FontWeight.SemiBold)
                        Text("Paga cuando recojas", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }
                }

                // Opción transferencia
                Row(modifier = Modifier.fillMaxWidth().clickable { paymentType = "transferencia" }.background(if (paymentType == "transferencia") colorScheme.tertiaryContainer else colorScheme.surface, RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = paymentType == "transferencia", onClick = { paymentType = "transferencia" })
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(transferMethods.firstOrNull()?.nombre ?: "Transferencia", fontWeight = FontWeight.SemiBold)
                        Text("Adjunta tu comprobante", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }
                }

                if (paymentType == "transferencia") {
                    HorizontalDivider(color = colorScheme.outlineVariant)
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (receiptUri != null) colorScheme.primaryContainer else colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (receiptUri != null) {
                                AsyncImage(model = receiptUri, contentDescription = "Comprobante", modifier = Modifier.size(80.dp).background(colorScheme.surface, RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Comprobante adjunto ✓", style = MaterialTheme.typography.bodySmall, color = colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                TextButton(onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Cambiar imagen") }
                            } else {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Subir comprobante (captura)", fontWeight = FontWeight.SemiBold)
                                Text("Toca para elegir de tu galería", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (receiptError != null) {
                        Text(receiptError!!, color = colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (paymentType == "transferencia") {
                        if (receiptUri == null) {
                            receiptError = "Debes adjuntar el comprobante de la transferencia"
                        } else {
                            onConfirm(
                                cashMethods.firstOrNull()?.id,
                                transferMethods.firstOrNull()?.id,
                                receiptUri
                            )
                        }
                    } else {
                        onConfirm(cashMethods.firstOrNull()?.id, null, null)
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeClaro, contentColor = White)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                else Text("Confirmar pedido", color = White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
