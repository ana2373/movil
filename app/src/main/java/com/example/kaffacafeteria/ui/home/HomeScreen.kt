package com.example.kaffacafeteria.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.productos.ProductCard
import com.example.kaffacafeteria.ui.productos.ProductListViewModel
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@Composable
fun HomeScreen(
    onNavigateToOrders: () -> Unit,
    onNavigateToPOS: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    onNavigateToCaja: () -> Unit,
    onNavigateToCompras: () -> Unit,
    onNavigateToProductos: () -> Unit,
    onNavigateToGastos: () -> Unit,
    onNavigateToMermas: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = createViewModel { HomeViewModel(it) },
    productViewModel: ProductListViewModel = createViewModel { ProductListViewModel(it) }
) {
    val state = homeViewModel.uiState
    val products = productViewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val isGuest = state.isGuest || state.user == null

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
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = White),
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
                        } else {
                            item { QuickChip(Icons.Default.Receipt, "Mis Pedidos", onNavigateToOrders) }
                        }
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ValueProp(Icons.Default.Spa, "Granos seleccionados") }
            item { ValueProp(Icons.Default.LocalFireDepartment, "Tueste fresco semanal") }
            item { ValueProp(Icons.Default.Groups, "Baristas expertos") }
            item { ValueProp(Icons.Default.Chair, "Espacio acogedor") }
        }

        OutlinedTextField(
            value = products.searchQuery,
            onValueChange = productViewModel::updateSearchQuery,
            placeholder = { Text("Buscar productos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (products.searchQuery.isNotEmpty()) {{ IconButton(onClick = { productViewModel.updateSearchQuery("") }) { Icon(Icons.Default.Clear, contentDescription = null) } }} else null,
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
            item { FilterChip(selected = products.selectedCategoriaId == null, onClick = { productViewModel.selectCategoria(null) }, label = { Text("Todas") }) }
            items(products.categorias) { cat ->
                FilterChip(selected = products.selectedCategoriaId == cat.id, onClick = { productViewModel.selectCategoria(cat.id) }, label = { Text(cat.nombre) })
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nuestro Menú", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onNavigateToProductos) {
                Text("Ver todo", fontWeight = FontWeight.SemiBold)
            }
        }

        when {
            products.isLoading && products.productos.isEmpty() -> LoadingIndicator()
            products.error != null -> ErrorMessage(message = products.error!!, onRetry = { productViewModel.refresh() })
            products.filteredProductos.isEmpty() -> EmptyState("No se encontraron productos")
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products.filteredProductos, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onBuy = { if (isGuest) onNavigateToLogin() else onNavigateToPOS() }
                    )
                }
            }
        }
    }
}

@Composable
fun ValueProp(icon: ImageVector, label: String) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(92.dp)) {
        Surface(shape = RoundedCornerShape(18.dp), color = LightTerracotta) {
            Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Terracotta, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = colorScheme.onSurfaceVariant,
            maxLines = 2
        )
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
