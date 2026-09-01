package com.example.kaffacafeteria.ui.productos

<<<<<<< HEAD
import androidx.compose.foundation.Image
=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
<<<<<<< HEAD
import androidx.compose.ui.res.painterResource
=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
<<<<<<< HEAD
import com.example.kaffacafeteria.ui.home.ProductDetailDialog
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel
import com.example.kaffacafeteria.util.imagenLocal
import com.example.kaffacafeteria.util.toImageUrl
=======
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onBack: () -> Unit,
    onBuy: (ProductoDto) -> Unit,
    viewModel: ProductListViewModel = createViewModel { ProductListViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
<<<<<<< HEAD
    var selectedProduct by remember { mutableStateOf<ProductoDto?>(null) }
=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Productos") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        OutlinedTextField(
            value = state.searchQuery, onValueChange = viewModel::updateSearchQuery,
            placeholder = { Text("Buscar productos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (state.searchQuery.isNotEmpty()) {{ IconButton(onClick = { viewModel.updateSearchQuery("") }) { Icon(Icons.Default.Clear, contentDescription = null) } }} else null,
            singleLine = true, modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                focusedLabelColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline,
                cursorColor = colorScheme.primary
            )
        )

        LazyRow(modifier = Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = state.selectedCategoriaId == null, onClick = { viewModel.selectCategoria(null) }, label = { Text("Todas") }) }
            items(state.categorias) { cat ->
                FilterChip(selected = state.selectedCategoriaId == cat.id, onClick = { viewModel.selectCategoria(cat.id) }, label = { Text(cat.nombre) })
            }
        }

        when {
            state.isLoading && state.productos.isEmpty() -> LoadingIndicator()
            state.error != null -> ErrorMessage(message = state.error!!, onRetry = { viewModel.refresh() })
            state.filteredProductos.isEmpty() -> EmptyState("No se encontraron productos")
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredProductos, key = { it.id }) { product ->
<<<<<<< HEAD
                    ProductCard(product = product, onImageClick = { selectedProduct = product }, onBuy = { onBuy(product) })
=======
                    ProductCard(product = product, onBuy = { onBuy(product) })
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
                }
            }
        }
    }
<<<<<<< HEAD

    selectedProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { selectedProduct = null },
            onBuy = {
                selectedProduct = null
                onBuy(product)
            }
        )
    }
}

@Composable
fun ProductCard(product: ProductoDto, onBuy: () -> Unit, onImageClick: () -> Unit = {}) {
    val colorScheme = MaterialTheme.colorScheme
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onImageClick) {
        Column {
            val localImg = product.imagenLocal()
            if (!product.imagen.isNullOrBlank()) {
                AsyncImage(model = product.imagen.toImageUrl(), error = localImg?.let { painterResource(it) }, fallback = localImg?.let { painterResource(it) }, contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp), contentScale = ContentScale.Crop)
            } else if (localImg != null) {
                Image(painter = painterResource(localImg), contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp), contentScale = ContentScale.Crop)
=======
}

@Composable
fun ProductCard(product: ProductoDto, onBuy: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column {
            if (!product.imagen.isNullOrBlank()) {
                AsyncImage(model = product.imagen, contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp), contentScale = ContentScale.Crop)
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Coffee, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(40.dp))
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(product.categoria?.nombre ?: "", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$${"%.2f".format(product.precioVenta.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.secondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onBuy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Comprar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
