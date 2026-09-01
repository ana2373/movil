package com.example.kaffacafeteria.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.kaffacafeteria.data.remote.dto.PedidoDto
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    onBack: () -> Unit,
    onOrderClick: (Int) -> Unit,
    viewModel: OrderViewModel = createViewModel { OrderViewModel(it) }
) {
    val state = viewModel.listState
    val colorScheme = MaterialTheme.colorScheme
    val statuses = listOf("pendiente", "pagado", "en_preparacion", "entregado")

    LaunchedEffect(Unit) { viewModel.loadPedidos() }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Pedidos") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        LazyRow(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = state.selectedFilter == null, onClick = { viewModel.loadPedidos() }, label = { Text("Todas") }) }
            items(statuses) { status ->
                FilterChip(selected = state.selectedFilter == status, onClick = { viewModel.loadPedidos(filter = status) }, label = { Text(status.replaceFirstChar { c -> c.uppercase() }) })
            }
        }

        when {
            state.isLoading && state.pedidos.isEmpty() -> LoadingIndicator()
            state.error != null -> ErrorMessage(message = state.error!!, onRetry = { viewModel.loadPedidos() })
            state.pedidos.isEmpty() -> EmptyState("No hay pedidos")
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.pedidos, key = { it.id }) { pedido ->
                    OrderCard(pedido = pedido, onClick = { onOrderClick(pedido.id) })
                }
            }
        }
    }
}

@Composable
fun OrderCard(pedido: PedidoDto, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pedido #${pedido.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(pedido.cliente?.nombre ?: "Cliente", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$${"%.2f".format(pedido.total.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(pedido.estado)
                Spacer(modifier = Modifier.height(4.dp))
                Text(pedido.created_at?.substring(0, 10) ?: "", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val colorScheme = MaterialTheme.colorScheme
    val (color, label) = when (status) {
        "pendiente" -> Pair(colorScheme.onSurfaceVariant to colorScheme.primary, "Pendiente")
        "pagado" -> Pair(colorScheme.secondary to colorScheme.secondary, "Pagado")
        "en_preparacion" -> Pair(colorScheme.tertiary to colorScheme.tertiary, "En preparación")
        "entregado" -> Pair(colorScheme.primary to colorScheme.primary, "Entregado")
        else -> Pair(colorScheme.onSurfaceVariant to colorScheme.onSurfaceVariant, status)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.first.copy(alpha = 0.1f)) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.bodySmall, color = color.second, fontWeight = FontWeight.SemiBold)
    }
}
