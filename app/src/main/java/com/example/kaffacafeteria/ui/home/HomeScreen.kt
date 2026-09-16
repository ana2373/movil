package com.example.kaffacafeteria.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.productos.ProductCard
import com.example.kaffacafeteria.ui.productos.ProductListViewModel
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel
import com.example.kaffacafeteria.util.imagenLocal
import com.example.kaffacafeteria.util.toImageUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToOrders: () -> Unit,
    onNavigateToPOS: () -> Unit,
    onBuyProduct: (ProductoDto) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    onNavigateToCaja: () -> Unit,
    onNavigateToCompras: () -> Unit,
    onNavigateToProductos: () -> Unit,
    onNavigateToGastos: () -> Unit,
    onNavigateToMermas: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = createViewModel { HomeViewModel(it) },
    productViewModel: ProductListViewModel = createViewModel { ProductListViewModel(it) }
) {
    val state = homeViewModel.uiState
    val products = productViewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val isGuest = state.isGuest || state.user == null

    var selectedProduct by remember { mutableStateOf<ProductoDto?>(null) }

    val promoKeywords = listOf("café", "pan", "postres", "postre", "repostería")
    val productosPromo = products.productos.filter { p ->
        val nombre = p.categoria?.nombre?.lowercase() ?: ""
        promoKeywords.any { nombre.contains(it) }
    }

    if (state.isLoading && state.user == null && !state.isGuest) { LoadingIndicator(); return }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Surface(
            color = colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Kaffa", style = MaterialTheme.typography.headlineMedium, color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            if (isGuest) "¡Bienvenido!" else "¡Hola, ${state.user?.nombre ?: ""}!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onPrimary
                        )
                    }
                    if (isGuest) {
                        Button(
                            onClick = onNavigateToLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeClaro, contentColor = White),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Iniciar sesión") }
                    } else {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                        }
                    }
                }

                val user = state.user
                if (!isGuest && user != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (user.isAdmin || user.isBarista) {
                            item { QuickChip(Icons.Default.ShoppingCart, "Vender", onNavigateToPOS) }
                            item { QuickChip(Icons.Default.Receipt, "Pedidos", onNavigateToOrders) }
                            item { QuickChip(Icons.Default.PointOfSale, "Caja", onNavigateToCaja) }
                            item { QuickChip(Icons.Default.Inventory2, "Compras", onNavigateToCompras) }
                            item { QuickChip(Icons.Default.MoneyOff, "Gastos", onNavigateToGastos) }
                            item { QuickChip(Icons.Default.Warning, "Mermas", onNavigateToMermas) }
                            if (user.isAdmin) {
                                item { QuickChip(Icons.Default.AdminPanelSettings, "Panel", onNavigateToAdminPanel) }
                            }
                        }
                    }
                }
            }
        }

LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    OutlinedTextField(
                        value = products.searchQuery,
                        onValueChange = productViewModel::updateSearchQuery,
                        placeholder = { Text("Buscar productos...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (products.searchQuery.isNotEmpty()) {{ IconButton(onClick = { productViewModel.updateSearchQuery("") }) { Icon(Icons.Default.Clear, contentDescription = null) } }} else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            cursorColor = colorScheme.primary
                        )
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = products.selectedCategoriaId == null, onClick = { productViewModel.selectCategoria(null) }, label = { Text("Todas") }) }
                        items(products.categorias) { cat ->
                            FilterChip(selected = products.selectedCategoriaId == cat.id, onClick = { productViewModel.selectCategoria(cat.id) }, label = { Text(cat.nombre) })
                        }
                    }
                }
            }

            if (productosPromo.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Destacados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ProductCarousel(
                        products = productosPromo,
                        onImageClick = { selectedProduct = it },
                        onBuy = { if (isGuest) onNavigateToLogin() else onBuyProduct(it) }
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nuestro Menú", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNavigateToProductos) {
                        Text("Ver todo", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            when {
                products.isLoading && products.productos.isEmpty() -> {
                    item(span = { GridItemSpan(maxLineSpan) }) { LoadingIndicator() }
                }
                products.error != null -> {
                    item(span = { GridItemSpan(maxLineSpan) }) { ErrorMessage(message = products.error!!, onRetry = { productViewModel.refresh() }) }
                }
                products.filteredProductos.isEmpty() -> {
                    item(span = { GridItemSpan(maxLineSpan) }) { EmptyState("No se encontraron productos") }
                }
                else -> {
                    items(products.filteredProductos, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onImageClick = { selectedProduct = product },
                            onBuy = { p -> if (isGuest) onNavigateToLogin() else onBuyProduct(p) }
                        )
                    }
                }
            }
        }
    }

    selectedProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { selectedProduct = null },
            onBuy = {
                selectedProduct = null
                if (isGuest) onNavigateToLogin() else onBuyProduct(product)
            }
        )
    }
}

@Composable
fun ProductCarousel(
    products: List<ProductoDto>,
    onImageClick: (ProductoDto) -> Unit,
    onBuy: (ProductoDto) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val pagerState = rememberPagerState(pageCount = { products.size })

    LaunchedEffect(products.size) {
        if (products.size > 1) {
            while (true) {
                delay(3000)
                val next = (pagerState.currentPage + 1) % products.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 14.dp,
            modifier = Modifier.fillMaxWidth().height(280.dp)
        ) { page ->
            val product = products[page]
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable { onImageClick(product) },
                        contentAlignment = Alignment.Center
                    ) {
                        val imageUrl = product.imagen.toImageUrl()
                        val localImg = product.imagenLocal()
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                error = localImg?.let { painterResource(it) },
                                fallback = localImg?.let { painterResource(it) },
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (localImg != null) {
                            Image(
                                painter = painterResource(localImg),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.linearGradient(listOf(VerdeClaro, PrimaryGreen))
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Coffee, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .matchParentSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f))))
                        )
                    }

                    Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(14.dp)) {
                        Text(
                            product.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            product.categoria?.nombre ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Precio", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                                Text(
                                    "$${"%.2f".format(product.precioVenta.toDoubleOrNull() ?: 0.0)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Button(
                                onClick = { onBuy(product) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Comprar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (products.size > 1) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(products.size) { index ->
                    val selected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(width = if (selected) 22.dp else 8.dp, height = 8.dp)
                            .background(
                                color = if (selected) VerdeClaro else PrimaryGreen.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailDialog(
    product: ProductoDto,
    onDismiss: () -> Unit,
    onBuy: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            val imageUrl = product.imagen.toImageUrl()
            val localImg = product.imagenLocal()
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    error = localImg?.let { painterResource(it) },
                    fallback = localImg?.let { painterResource(it) },
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (localImg != null) {
                Image(
                    painter = painterResource(localImg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(VerdeClaro, PrimaryGreen))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Coffee, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(product.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ink(CoffeeBrown))
            Text(product.categoria?.nombre ?: "", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkModeActive()) colorScheme.surfaceVariant else LightBrown,
                border = BorderStroke(1.dp, ink(CoffeeBrown).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Descripción", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ink(CoffeeBrown))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = product.descripcion?.takeIf { it.isNotBlank() } ?: "Información no disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ink(NearBlack)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Precio", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    Text("$${"%.2f".format(product.precioVenta.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.titleLarge, color = VerdeClaro, fontWeight = FontWeight.Black)
                }
                Button(
                    onClick = onBuy,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = fill(PrimaryGreen), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Comprar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = White.copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}
