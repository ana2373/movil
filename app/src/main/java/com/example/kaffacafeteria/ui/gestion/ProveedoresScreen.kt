package com.example.kaffacafeteria.ui.gestion

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.kaffacafeteria.data.remote.dto.ProveedorDto
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedoresScreen(
    onBack: () -> Unit,
    viewModel: ProveedoresViewModel = createViewModel { ProveedoresViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingProveedor by remember { mutableStateOf<ProveedorDto?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Proveedores") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Agregar proveedor", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(state.error!!, viewModel::load)
            state.proveedores.isEmpty() -> EmptyState("No hay proveedores registrados")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.proveedores, key = { it.id }) { proveedor ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(12.dp), color = ink(CafeOscuro).copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = ink(CafeOscuro), modifier = Modifier.size(24.dp)) }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(proveedor.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(proveedor.nit ?: "Sin NIT", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                }
                                if (proveedor.activo != false) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = ink(PrimaryGreen).copy(alpha = 0.15f)) { Text("Activo", fontSize = 11.sp, color = ink(PrimaryGreen), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                                }
                                IconButton(onClick = { editingProveedor = proveedor }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colorScheme.primary) }
                                IconButton(onClick = { viewModel.delete(proveedor.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoItem(Icons.Default.Phone, proveedor.telefono ?: "Sin teléfono")
                                InfoItem(Icons.Default.Email, proveedor.email ?: "Sin correo")
                                InfoItem(Icons.Default.LocationOn, proveedor.direccion ?: "Sin dirección")
                            }
                            if (!proveedor.convenio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Handshake, contentDescription = null, tint = ink(SecondaryGreen), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Convenio: ${proveedor.convenio}", style = MaterialTheme.typography.bodySmall, color = ink(SecondaryGreen))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        ProveedorFormDialog(
            initial = null,
            isLoading = state.isCreating,
            onDismiss = { showCreateDialog = false },
            onConfirm = { p ->
                viewModel.create(p.nombre, p.contacto, p.telefono, p.email, p.direccion, p.nit, p.convenio)
                showCreateDialog = false
            }
        )
    }

    editingProveedor?.let { proveedor ->
        ProveedorFormDialog(
            initial = proveedor,
            isLoading = state.isCreating,
            onDismiss = { editingProveedor = null },
            onConfirm = { p ->
                viewModel.update(proveedor.id, p.nombre, p.contacto, p.telefono, p.email, p.direccion, p.nit, p.convenio)
                editingProveedor = null
            }
        )
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProveedorFormDialog(
    initial: ProveedorDto?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ProveedorDto) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var nombre by remember { mutableStateOf(initial?.nombre ?: "") }
    var contacto by remember { mutableStateOf(initial?.contacto ?: "") }
    var telefono by remember { mutableStateOf(initial?.telefono ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var direccion by remember { mutableStateOf(initial?.direccion ?: "") }
    var nit by remember { mutableStateOf(initial?.nit ?: "") }
    var convenio by remember { mutableStateOf(initial?.convenio ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Agregar Proveedor" else "Editar Proveedor", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = nit, onValueChange = { nit = it }, label = { Text("NIT") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = contacto, onValueChange = { contacto = it }, label = { Text("Persona de contacto (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = convenio, onValueChange = { convenio = it }, label = { Text("Convenio / acuerdo (opcional)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(ProveedorDto(
                    id = initial?.id ?: 0,
                    nombre = nombre.trim(),
                    contacto = contacto.ifBlank { null },
                    telefono = telefono.ifBlank { null },
                    email = email.ifBlank { null },
                    direccion = direccion.ifBlank { null },
                    activo = true,
                    nit = nit.ifBlank { null },
                    convenio = convenio.ifBlank { null },
                    created_at = null,
                    updated_at = null
                ))
            },
                enabled = nombre.isNotBlank() && nit.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                else Text("Guardar", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}