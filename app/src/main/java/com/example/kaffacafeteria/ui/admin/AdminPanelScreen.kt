package com.example.kaffacafeteria.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Panel de Administración") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminCard(icon = Icons.Default.People, title = "Usuarios", description = "Gestionar usuarios y roles", color = colorScheme.primary, onClick = onNavigateToUsers)
            AdminCard(icon = Icons.Default.Category, title = "Categorías", description = "Gestionar categorías de productos", color = colorScheme.secondary, onClick = onNavigateToCategories)
            AdminCard(icon = Icons.Default.Coffee, title = "Productos", description = "Gestionar catálogo de productos", color = colorScheme.tertiary, onClick = onNavigateToProducts)
            AdminCard(icon = Icons.Default.Payment, title = "Métodos de Pago", description = "Configurar métodos de pago", color = colorScheme.secondary, onClick = onNavigateToPaymentMethods)
            AdminCard(icon = Icons.Default.BarChart, title = "Reportes", description = "Ver reportes y estadísticas", color = colorScheme.onSurfaceVariant, onClick = onNavigateToReports)
        }
    }
}

@Composable
fun AdminCard(icon: ImageVector, title: String, description: String, color: Color, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                Text(description, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
        }
    }
}
