package com.example.kaffacafeteria.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.ErrorMessage
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = createViewModel { AdminViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadUsuarios(); viewModel.loadRoles() }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Usuarios") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { viewModel.initCreateUser(); showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Crear usuario", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.isLoading && state.usuarios.isEmpty() -> LoadingIndicator()
            state.error != null -> ErrorMessage(message = state.error!!, onRetry = { viewModel.loadUsuarios() })
            state.usuarios.isEmpty() -> EmptyState("No hay usuarios")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.usuarios, key = { it.id }) { user ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(user.correo, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    user.roles?.forEach { role ->
                                        Surface(shape = RoundedCornerShape(6.dp), color = colorScheme.primary.copy(alpha = 0.1f)) {
                                            Text(role.nombre, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.bodySmall, color = colorScheme.primary)
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteUser(user.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            roles = state.roles,
            isLoading = state.isLoading,
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, correo, password, roleIds ->
                viewModel.updateUserFormNombre(nombre)
                viewModel.updateUserFormCorreo(correo)
                viewModel.updateUserFormPassword(password)
                roleIds.forEach { viewModel.toggleUserRole(it) }
                viewModel.saveUser()
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateUserDialog(
    roles: List<com.example.kaffacafeteria.data.remote.dto.RolFullDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, List<Int>) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRoleIds by remember { mutableStateOf(setOf<Int>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Usuario", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                Text("Roles:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                roles.forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = selectedRoleIds.contains(role.id), onCheckedChange = { checked -> selectedRoleIds = if (checked) selectedRoleIds + role.id else selectedRoleIds - role.id }, colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary))
                        Text(role.nombre)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, correo, password, selectedRoleIds.toList()) },
                enabled = nombre.isNotBlank() && correo.isNotBlank() && password.isNotBlank() && selectedRoleIds.isNotEmpty() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                else Text("Crear", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
