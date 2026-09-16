package com.example.kaffacafeteria.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.example.kaffacafeteria.data.remote.dto.RolFullDto
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToInsumos: () -> Unit,
    onNavigateToCompras: () -> Unit,
    onNavigateToMermas: () -> Unit,
    onNavigateToProveedores: () -> Unit,
    onNavigateToGastos: () -> Unit,
    onNavigateToCaja: () -> Unit,
    onNavigateToTurnos: () -> Unit,
    onNavigateToPromociones: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: AdminViewModel = createViewModel { AdminViewModel(it) },
    dashboardViewModel: AdminDashboardViewModel = createViewModel { AdminDashboardViewModel(it) }
) {
    val colorScheme = MaterialTheme.colorScheme
    val dashboard = dashboardViewModel.uiState
    var showAddBarista by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    LaunchedEffect(Unit) {
        viewModel.loadRoles()
        dashboardViewModel.load()
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Panel de Administración") },
            actions = {
                IconButton(onClick = onNavigateToProfile) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = colorScheme.onPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Métricas en tiempo real (KPIs) ──
            SectionTitle("Métricas en tiempo real")

            if (dashboard.isLoading) {
                KpiRow(
                    kpis = listOf(
                        KpiData("Ingresos hoy", "—", Icons.Default.Paid, Terracotta),
                        KpiData("Pedidos hoy", "—", Icons.Default.Receipt, PrimaryGreen),
                        KpiData("Clientes", "—", Icons.Default.People, CoffeeBrown),
                        KpiData("Ticket prom.", "—", Icons.Default.Calculate, Negro)
                    )
                )
            } else {
                KpiRow(
                    kpis = listOf(
                        KpiData("Ingresos hoy", "$${"%.0f".format(dashboard.ingresosHoy)}", Icons.Default.Paid, Terracotta),
                        KpiData("Pedidos hoy", "${dashboard.pedidosHoy}", Icons.Default.Receipt, PrimaryGreen),
                        KpiData("Clientes", "${dashboard.clientesRegistrados}", Icons.Default.People, CoffeeBrown),
                        KpiData("Ticket prom.", "$${"%.0f".format(dashboard.ticketPromedio)}", Icons.Default.Calculate, Negro)
                    )
                )
            }

            // ── Gráfica de balance 7 días ──
            SectionTitle("Balance de ingresos · últimos 7 días")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SevenDayChart(data = dashboard.ventas7Dias)
            }

            // ── Ranking productos más vendidos ──
            SectionTitle("Productos más vendidos")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (dashboard.topProductos.isEmpty()) {
                        Text("Aún no hay ventas registradas.", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                    } else {
                        dashboard.topProductos.forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${item.cantidad}", fontWeight = FontWeight.Black, color = colorScheme.primary, modifier = Modifier.width(34.dp))
                                Text(item.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Módulos de administración general ──
            SectionTitle("Gestión de oferta")
            AdminCard(icon = Icons.Default.Category, title = "Categorías", description = "Organización del menú por categorías", color = PrimaryGreen, onClick = onNavigateToCategories)
            AdminCard(icon = Icons.Default.Coffee, title = "Productos", description = "Control del catálogo de productos", color = CafeOscuro, onClick = onNavigateToProducts)

            SectionTitle("Ventas y pedidos")
            AdminCard(icon = Icons.Default.ReceiptLong, title = "Historial de pedidos", description = "Pedidos y facturas emitidas", color = Negro, onClick = onNavigateToOrders)
            AdminCard(icon = Icons.Default.Campaign, title = "Promociones", description = "Crea promociones con color e imagen", color = SecondaryGreen, onClick = onNavigateToPromociones)

            SectionTitle("Logística e inventario")
            AdminCard(icon = Icons.Default.Inventory, title = "Insumos", description = "Materias primas e inventario", color = SecondaryGreen, onClick = onNavigateToInsumos)
            AdminCard(icon = Icons.Default.Warning, title = "Mermas", description = "Registro de pérdidas o mermas", color = CafeOscuro, onClick = onNavigateToMermas)
            AdminCard(icon = Icons.Default.ShoppingCart, title = "Compras a proveedores", description = "Control de compras", color = Negro, onClick = onNavigateToCompras)
            AdminCard(icon = Icons.Default.LocalShipping, title = "Proveedores", description = "Base de datos de proveedores", color = CoffeeBrown, onClick = onNavigateToProveedores)

            SectionTitle("Finanzas y operaciones")
            AdminCard(icon = Icons.Default.MoneyOff, title = "Gastos generales", description = "Registro de gastos", color = CafeOscuro, onClick = onNavigateToGastos)
            AdminCard(icon = Icons.Default.PointOfSale, title = "Cajas de cobro", description = "Control de flujo de cajas", color = PrimaryGreen, onClick = onNavigateToCaja)
            AdminCard(icon = Icons.Default.Schedule, title = "Turnos laborales", description = "Asignación de turnos a baristas", color = Negro, onClick = onNavigateToTurnos)
            AdminCard(icon = Icons.Default.BarChart, title = "Reportes estadísticos", description = "Visualización de reportes y configuración", color = CoffeeBrown, onClick = onNavigateToReports)

            SectionTitle("Usuarios y roles")
            AdminCard(icon = Icons.Default.People, title = "Usuarios", description = "Gestionar usuarios, baristas y roles", color = colorScheme.primary, onClick = onNavigateToUsers)
            AdminCard(icon = Icons.Default.Payment, title = "Métodos de Pago", description = "Configurar métodos de pago", color = SecondaryGreen, onClick = onNavigateToPaymentMethods)

            Spacer(modifier = Modifier.height(8.dp))

            // Tarjeta destacada: Agregar barista
            AddBaristaCard(onClick = { showAddBarista = true })
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Black) },
            text = { Text("Estás en el Panel de Administración. ¿Deseas salir y cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = { showExitDialog = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
                ) {
                    Text("Cerrar sesión", color = colorScheme.onPrimary)
                }
            },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("Quedarme") } }
        )
    }

    if (showAddBarista) {
        AddBaristaDialog(
            roles = viewModel.uiState.roles,
            isLoading = viewModel.uiState.isLoading,
            onDismiss = { showAddBarista = false },
            onConfirm = { nombre, correo, password, roleIds ->
                viewModel.initCreateUser()
                viewModel.updateUserFormNombre(nombre)
                viewModel.updateUserFormCorreo(correo)
                viewModel.updateUserFormPassword(password)
                viewModel.setUserRoles(roleIds)
                viewModel.saveUser()
                showAddBarista = false
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}

data class KpiData(val title: String, val value: String, val icon: ImageVector, val color: Color)

@Composable
fun KpiRow(kpis: List<KpiData>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        kpis.chunked(2).forEach { rowKpis ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowKpis.forEach { kpi ->
                    KpiCard(kpi, modifier = Modifier.weight(1f))
                }
                if (rowKpis.size == 1) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun KpiCard(kpi: KpiData, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val dark = isDarkModeActive()
    val accent = if (dark && (kpi.color == Terracotta || kpi.color == CoffeeBrown || kpi.color == Negro)) VerdeClaro else kpi.color
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(shape = CircleShape, color = accent.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(kpi.icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp)) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(kpi.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = accent)
            Text(kpi.title, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
fun SevenDayChart(data: List<DaySales>) {
    val colorScheme = MaterialTheme.colorScheme
    val dark = isDarkModeActive()
    val chartValueColor = if (dark) VerdeClaro else Terracotta
    if (data.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Sin datos", color = colorScheme.onSurfaceVariant)
        }
        return
    }
    val max = data.maxOfOrNull { it.total } ?: 0.0
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(140.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("$${"%.0f".format(day.total)}", fontSize = 9.sp, color = chartValueColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(horizontal = 6.dp)
                        .height(if (max > 0) ((day.total / max) * 90).dp else 2.dp)
                        .background(PrimaryGreen, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) { }
                Spacer(modifier = Modifier.height(4.dp))
                Text(day.label, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AddBaristaCard(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = colorScheme.onSecondary.copy(alpha = 0.25f), modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = colorScheme.onSecondary, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Agregar Barista", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = colorScheme.onSecondary)
                Text("Crea una cuenta de barista con su contraseña", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSecondary.copy(alpha = 0.9f))
            }
            Icon(Icons.Default.AddCircle, contentDescription = null, tint = colorScheme.onSecondary, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun AdminCard(icon: ImageVector, title: String, description: String, color: Color, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val dark = isDarkModeActive()
    val accent = if (dark && (color == CafeOscuro || color == CoffeeBrown || color == Negro || color == Terracotta)) VerdeClaro else color
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.12f), modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
                Text(description, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AddBaristaDialog(
    roles: List<RolFullDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, List<Int>) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRoleIds by remember { mutableStateOf(setOf<Int>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Barista", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Registra un nuevo barista y asígnale su contraseña de acceso.", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre completo") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo electrónico") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Contraseña") },
                    trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null) } },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant)
                )
                Text("Rol:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                roles.forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = selectedRoleIds.contains(role.id), onCheckedChange = { checked -> selectedRoleIds = if (checked) selectedRoleIds + role.id else selectedRoleIds - role.id }, colors = CheckboxDefaults.colors(checkedColor = colorScheme.secondary))
                        Text(role.nombre.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nombre, correo, password, selectedRoleIds.toList()) },
                enabled = nombre.isNotBlank() && correo.isNotBlank() && password.isNotBlank() && selectedRoleIds.isNotEmpty() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)
            ) {
                Text("Guardar Barista", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
