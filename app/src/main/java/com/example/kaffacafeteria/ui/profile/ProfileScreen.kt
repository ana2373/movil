package com.example.kaffacafeteria.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel
import com.example.kaffacafeteria.util.toImageUrl
import java.io.File
import java.io.FileOutputStream

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
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { copyToCacheAndUpload(it, context, viewModel) }
    }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        if (state.successMessage != null || state.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

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
            Box {
                Surface(modifier = Modifier.size(96.dp), shape = CircleShape, color = colorScheme.primaryContainer) {
                    val fotoUrl = state.user?.foto?.toImageUrl()
                    Box(contentAlignment = Alignment.Center) {
                        if (fotoUrl != null) {
                            AsyncImage(model = fotoUrl, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = colorScheme.primary)
                        }
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape).clickable {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = CircleShape,
                    color = colorScheme.secondary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (state.isUploadingPhoto) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Edit, contentDescription = "Cambiar foto", modifier = Modifier.size(16.dp), tint = colorScheme.onSecondary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(state.user?.nombre ?: "Invitado", style = MaterialTheme.typography.headlineMedium, color = colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(state.user?.correo ?: "", style = MaterialTheme.typography.bodyLarge, color = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            if (state.successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.successMessage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

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
                        Text("Información de la Cuenta", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoRow("ID", state.user?.id?.toString() ?: "", colorScheme)
                        ProfileInfoRow("Rol", roleLabel(state.user), colorScheme)
                        ProfileInfoRow("Estado", if (state.user?.activo == true) "Activo" else "Inactivo", colorScheme)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorScheme.surface)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Editar Información", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = state.editingNombre,
                            onValueChange = viewModel::updateEditingNombre,
                            label = { Text("Nombre completo") },
                            isError = state.editNombreError != null,
                            supportingText = state.editNombreError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, cursorColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant)
                        )
                        OutlinedTextField(
                            value = state.editingCorreo,
                            onValueChange = viewModel::updateEditingCorreo,
                            label = { Text("Correo electrónico") },
                            isError = state.editCorreoError != null,
                            supportingText = state.editCorreoError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, cursorColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant)
                        )
                        Button(
                            onClick = { viewModel.saveProfile() },
                            enabled = !state.isSavingProfile,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)
                        ) {
                            if (state.isSavingProfile) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                            else Text("Guardar cambios")
                        }
                    }
                }

                if (viewModel.isAdmin) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorScheme.surface)) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cambiar Contraseña", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            OutlinedTextField(
                                value = state.newPassword,
                                onValueChange = viewModel::updateNewPassword,
                                label = { Text("Nueva contraseña") },
                                trailingIcon = { IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) { Icon(if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null) } },
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = state.newPasswordError != null,
                                supportingText = state.newPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, cursorColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant)
                            )
                            OutlinedTextField(
                                value = state.confirmNewPassword,
                                onValueChange = viewModel::updateConfirmNewPassword,
                                label = { Text("Confirmar nueva contraseña") },
                                trailingIcon = { IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null) } },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = state.confirmNewPasswordError != null,
                                supportingText = state.confirmNewPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, cursorColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant)
                            )
                            Button(
                                onClick = { viewModel.changePassword() },
                                enabled = !state.isSavingPassword,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
                            ) {
                                if (state.isSavingPassword) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                                else Text("Actualizar contraseña")
                            }
                        }
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

private fun roleLabel(user: com.example.kaffacafeteria.domain.model.User?): String {
    if (user == null) return ""
    return when {
        user.isAdmin -> "Administrador"
        user.isBarista -> "Barista"
        else -> "Cliente"
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, colorScheme: androidx.compose.material3.ColorScheme) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun copyToCacheAndUpload(uri: Uri, context: android.content.Context, viewModel: ProfileViewModel) {
    try {
        val fileName = "perfil_${System.currentTimeMillis()}.jpg"
        val cacheFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
        }
        viewModel.subirFoto(cacheFile.absolutePath)
    } catch (e: Exception) {
        viewModel.updateError(e.message ?: "No se pudo cargar la imagen")
    }
}
