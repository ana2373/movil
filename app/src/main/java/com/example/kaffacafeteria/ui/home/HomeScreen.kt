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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.kaffacafeteria.data.remote.dto.EventoDto
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
import com.example.kaffacafeteria.domain.model.Promocion
import com.example.kaffacafeteria.ui.admin.PromocionesViewModel
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.components.PromocionPopup
import com.example.kaffacafeteria.ui.components.proximoEvento
import com.example.kaffacafeteria.ui.components.productosDePromo
import com.example.kaffacafeteria.ui.productos.ProductCard
import com.example.kaffacafeteria.ui.productos.ProductListViewModel
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel
import com.example.kaffacafeteria.util.imagenLocal
import com.example.kaffacafeteria.util.toImageUrl
import com.example.kaffacafeteria.promoPopupDismissStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    onComprarPromo: (Promocion, List<ProductoDto>) -> Unit = { _, _ -> },
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = createViewModel { HomeViewModel(it) },
    productViewModel: ProductListViewModel = createViewModel { ProductListViewModel(it) },
    eventosViewModel: EventosPublicosViewModel = createViewModel { EventosPublicosViewModel(it) },
    promosViewModel: PromocionesViewModel = createViewModel { PromocionesViewModel(it) }
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val state = homeViewModel.uiState
    val products = productViewModel.uiState
    val eventosState = eventosViewModel.uiState
    val promosState = promosViewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val isGuest = state.isGuest || state.user == null

    var selectedProduct by remember { mutableStateOf<ProductoDto?>(null) }
    var selectedEvento by remember { mutableStateOf<EventoDto?>(null) }

    // ── Pantalla emergente de promo/eventos ──
    // La promoción del momento se muestra automáticamente al entrar al Inicio;
    // el próximo evento también se mantiene como popup si no fue descartado.
    val context = androidx.compose.ui.platform.LocalContext.current
    val dismissStore = remember { (context.applicationContext as com.example.kaffacafeteria.KaffaApp).promoPopupDismissStore }
    var mostrarPopup by rememberSaveable { mutableStateOf(false) }
    var dismissedEventos by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            dismissedEventos = dismissStore.getDismissedEventoIds()
        }
    }

    val eventoProximo = eventosState.eventos.firstOrNull { e ->
        val idStr = e.id.toString()
        !dismissedEventos.contains(idStr)
    } ?: eventosState.eventos.firstOrNull { e ->
        val idStr = e.id.toString()
        dismissedEventos.contains(idStr)
    }

    val promoDelMomento = promosState.promoDestacada

    // La promoción se muestra siempre al entrar, sin depender de descartes previos.
    val promoParaPopup = promoDelMomento

    val eventoParaPopup = eventoProximo?.let { evento ->
        val idStr = evento.id.toString()
        if (dismissedEventos.contains(idStr)) null else eventoProximo
    }

    LaunchedEffect(promosState.promociones) {
        promosViewModel.cargarPromoSesion()
    }

    // Abrir el popup automáticamente al entrar cuando ya hay promo o evento cargado.
    LaunchedEffect(promoParaPopup?.id, eventoParaPopup?.id) {
        if ((promoParaPopup != null || eventoParaPopup != null) && !mostrarPopup) {
            mostrarPopup = true
        }
    }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Botón para volver a abrir el popup de promoción/eventos
                            IconButton(onClick = { 
                                if (promoParaPopup != null || eventoParaPopup != null) {
                                    mostrarPopup = true
                                } else if (eventosState.eventos.isNotEmpty()) {
                                    selectedEvento = eventosState.eventos.first()
                                    coroutineScope.launch { gridState.animateScrollToItem(index = 0) }
                                }
                            }) {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = "Promociones y eventos",
                                    tint = colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = onNavigateToProfile) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                            }
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
            state = gridState,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
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

            // Carrusel de promociones activas/vigentes
            val promocionesActivas = promosState.promociones.filter { p -> 
                p.activa && (p.expiracionMillis?.let { exp -> System.currentTimeMillis() < exp } != false)
            }
            if (promocionesActivas.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Promociones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                        items(promocionesActivas, key = { it.id }) { promo ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.width(280.dp).clickable { 
                                    if (!isGuest) {
                                        val prods = productosDePromo(promo, products.productos)
                                        onComprarPromo(promo, prods)
                                    } else {
                                        onNavigateToLogin()
                                    }
                                }
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(120.dp).background(
                                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer))
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val img = promo.imagenUri
                                        if (!img.isNullOrBlank()) {
                                            AsyncImage(model = img, contentDescription = promo.nombre, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                        }
                                    }
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(promo.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (promo.descripcion.isNotBlank()) {
                                            Text(promo.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                        if (promo.descuentoPorcentaje > 0) {
                                            Text("${promo.descuentoPorcentaje}% descuento", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
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

            if (eventosState.eventos.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Próximos eventos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EventoCarousel(
                        eventos = eventosState.eventos,
                        onClick = { selectedEvento = it }
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

    selectedEvento?.let { evento ->
        EventoDetailDialog(evento = evento, onDismiss = { selectedEvento = null })
    }

    // Pantalla emergente de la promoción del momento y/o el próximo evento
    if (mostrarPopup && (promoParaPopup != null || eventoParaPopup != null)) {
        PromocionPopup(
            promo = promoParaPopup,
            evento = eventoParaPopup,
            productosPromo = promoParaPopup?.let { productosDePromo(it, products.productos) } ?: emptyList(),
            onDismiss = {
                mostrarPopup = false
                // Al cerrar con X, el evento pasa al carrusel ya existente (marcar como descartado)
                eventoParaPopup?.let { ev ->
                    coroutineScope.launch(Dispatchers.IO) {
                        dismissStore.dismissEvento(ev.id)
                    }
                    coroutineScope.launch {
                        val eventosCount = eventosState.eventos.size
                        if (eventosCount > 0) {
                            // Buscar el índice del carrusel de eventos en el grid
                            gridState.animateScrollToItem(index = 0)
                        }
                    }
                }
            },
            onComprarPromo = { promo ->
                mostrarPopup = false
                if (isGuest) {
                    onNavigateToLogin()
                } else {
                    onComprarPromo(promo, productosDePromo(promo, products.productos))
                }
            },
            onVerEvento = {
                mostrarPopup = false
                eventoParaPopup?.let { ev ->
                    coroutineScope.launch(Dispatchers.IO) {
                        dismissStore.dismissEvento(ev.id)
                    }
                    selectedEvento = ev
                    coroutineScope.launch {
                        gridState.animateScrollToItem(index = 0)
                    }
                }
            }
        )
    }
}

@Composable
fun EventoCarousel(
    eventos: List<EventoDto>,
    onClick: (EventoDto) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(eventos, key = { it.id }) { evento ->
            EventoHomeCard(evento = evento, onClick = { onClick(evento) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoHomeCard(evento: EventoDto, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.width(260.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                val portada = evento.portada
                if (!portada.isNullOrBlank()) {
                    AsyncImage(
                        model = portada,
                        contentDescription = evento.titulo,
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
                        Icon(Icons.Default.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    color = colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        textoFechaEvento(evento.fecha),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    evento.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val detalle = listOfNotNull(
                    evento.hora?.take(5)?.takeIf { it.isNotBlank() },
                    evento.lugar?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (detalle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            detalle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (evento.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        evento.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun EventoDetailDialog(evento: EventoDto, onDismiss: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
            ) { Text("Cerrar") }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
            val portada = evento.portada
            if (!portada.isNullOrBlank()) {
                AsyncImage(
                    model = portada,
                    contentDescription = evento.titulo,
                    modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(VerdeClaro, PrimaryGreen))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(evento.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(textoFechaHoraEvento(evento.fecha, evento.hora), style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
            evento.lugar?.takeIf { it.isNotBlank() }?.let { lugar ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(lugar, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                }
            }

            if (evento.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(evento.descripcion, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurface)
            }
            }
        }
    )
}

/** "2025-06-25" → "25/06" para la etiqueta de la tarjeta. */
private fun textoFechaEvento(fecha: String): String {
    if (fecha.isBlank()) return ""
    return try {
        val parsed = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(fecha)
        java.text.SimpleDateFormat("dd MMM", java.util.Locale("es", "CO")).format(parsed!!)
    } catch (_: Exception) {
        fecha
    }
}

/** Texto de fecha/hora para el diálogo: "25/06/2025 · 06:30 PM". */
private fun textoFechaHoraEvento(fecha: String, hora: String?): String {
    val dia = try {
        val parsed = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(fecha)
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(parsed!!)
    } catch (_: Exception) {
        fecha
    }
    val h = hora?.take(5)?.takeIf { it.isNotBlank() }
    return if (h != null) "$dia · $h" else dia
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
