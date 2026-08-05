package com.example.kaffacafeteria.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    viewModel: ProfileViewModel = createViewModel { ProfileViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = { TextButton(onClick = { showLogoutDialog = false; viewModel.logout(); onLogout() }) { Text("Cerrar sesión", color = colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Mi Perfil") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Surface(modifier = Modifier.size(96.dp), shape = CircleShape, color = colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = colorScheme.primary) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(state.user?.nombre ?: "Invitado", style = MaterialTheme.typography.headlineMedium, color = colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(state.user?.correo ?: "", style = MaterialTheme.typography.bodyLarge, color = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            if (state.user == null) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorScheme.surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("No has iniciado sesión", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Inicia sesión para ver tu perfil y realizar pedidos.", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorScheme.surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Roles Asignados", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        state.user?.roles?.forEach { role ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = when (role.nombre) { "admin" -> colorScheme.primary; "barista" -> colorScheme.tertiary; else -> colorScheme.secondary }, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(role.nombre.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorScheme.surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Información de la Cuenta", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoRow("ID", state.user?.id?.toString() ?: "", colorScheme)
                        ProfileInfoRow("Estado", if (state.user?.activo == true) "Activo" else "Inactivo", colorScheme)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorScheme.surface)) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tema oscuro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Cambia la apariencia de la app", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = onToggleTheme,
                        colors = SwitchDefaults.colors(checkedTrackColor = colorScheme.primary, checkedThumbColor = colorScheme.onPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(onClick = { showLogoutDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.error)) {
                Icon(Icons.Default.Logout, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Cerrar Sesión")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, colorScheme: androidx.compose.material3.ColorScheme) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
