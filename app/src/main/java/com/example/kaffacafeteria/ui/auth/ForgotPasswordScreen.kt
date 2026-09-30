package com.example.kaffacafeteria.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.util.createViewModel

/**
 * Recuperación de contraseña en dos pasos.
 *
 * Paso 1 (`!esEnviadoExternamente && !resetExitoso`): se pide el correo y la
 * app solicita el enlace.
 *
 * Paso 2 (al abrir el deep link `kaffa://reset-password`): el token y el correo
 * llegan precargados y sólo se solicita la contraseña nueva.
 *
 * El botón "Volver" permite recuperar el enlace si se abrió desde el correo
 * sin token, o solicitar uno nuevo si expiró.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: ForgotPasswordViewModel = createViewModel { ForgotPasswordViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(containerColor = colorScheme.background) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            TopAppBar(
                title = { Text(if (state.esEnviadoExternamente) "Nueva contraseña" else "Recuperar acceso") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary
                )
            )

            when {
                state.resetExitoso -> ResetSuccess(
                    mensaje = state.mensajeReset,
                    onIrAlLogin = onResetSuccess
                )

                state.esEnviadoExternamente || state.token.isNotBlank() -> ResetForm(
                    state = state,
                    viewModel = viewModel,
                    onSolicitarNuevo = viewModel::volverASolicitarEnlace
                )

                state.enlaceEnviado -> EnvelopeSent(
                    state = state,
                    viewModel = viewModel
                )

                else -> RequestLinkForm(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun RequestLinkForm(state: ForgotPasswordUiState, viewModel: ForgotPasswordViewModel) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(Icons.Default.Email, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(48.dp))
        Text("Recuperar acceso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = colorScheme.onBackground)
        Text(
            "Escribe tu correo y te enviaremos un enlace para crear una contraseña nueva.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = state.correo,
            onValueChange = viewModel::updateCorreo,
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            isError = state.correoError != null,
            supportingText = state.correoError?.let { { Text(it, color = colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.error != null) {
            Text(state.error, color = colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }

        Button(
            onClick = viewModel::solicitarEnlace,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = colorScheme.onPrimary)
            } else {
                Text("Enviar enlace")
            }
        }
    }
}

@Composable
private fun EnvelopeSent(state: ForgotPasswordUiState, viewModel: ForgotPasswordViewModel) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Surface(shape = androidx.compose.foundation.shape.CircleShape, color = colorScheme.secondary.copy(alpha = 0.15f), modifier = Modifier.size(88.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = colorScheme.secondary, modifier = Modifier.size(44.dp))
            }
        }
        Text("Revisa tu correo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = colorScheme.onBackground, textAlign = TextAlign.Center)
        Text(
            state.mensajeEnvio ?: "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Enviado a", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                Text(state.correo.trim(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
        Text(
            "Revisa también la carpeta de spam. El enlace abre la app si la tienes instalada y funciona en el navegador si no.",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        OutlinedButton(
            onClick = viewModel::volverASolicitarEnlace,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Usar otro correo")
        }
    }
}

@Composable
private fun ResetForm(
    state: ForgotPasswordUiState,
    viewModel: ForgotPasswordViewModel,
    onSolicitarNuevo: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(Icons.Default.Lock, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(48.dp))
        Text("Crea tu contraseña", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = colorScheme.onBackground, textAlign = TextAlign.Center)
        Text(
            "El enlace tiene una vigencia limitada. Al cambiarla se cerrarán las demás sesiones.",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (state.correo.isNotBlank()) {
            Text(
                "Cuenta: ${state.correo}",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = state.nuevaPassword,
            onValueChange = viewModel::updatePassword,
            label = { Text("Contraseña nueva") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisible) {
                    Icon(
                        if (state.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = state.passwordError != null,
            supportingText = state.passwordError?.let { { Text(it, color = colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.confirmarPassword,
            onValueChange = viewModel::updateConfirmar,
            label = { Text("Confirmar contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = viewModel::toggleConfirmarVisible) {
                    Icon(
                        if (state.confirmarVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (state.confirmarVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = state.confirmarError != null,
            supportingText = state.confirmarError?.let { { Text(it, color = colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.error != null) {
            Text(state.error, color = colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }

        Button(
            onClick = viewModel::restablecer,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = colorScheme.onPrimary)
            } else {
                Text("Guardar contraseña")
            }
        }

        TextButton(onClick = onSolicitarNuevo) {
            Text("El enlace expiró · Solicitar uno nuevo", color = colorScheme.primary)
        }
    }
}

@Composable
private fun ResetSuccess(mensaje: String?, onIrAlLogin: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Surface(shape = androidx.compose.foundation.shape.CircleShape, color = colorScheme.secondary.copy(alpha = 0.15f), modifier = Modifier.size(88.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colorScheme.secondary, modifier = Modifier.size(44.dp))
            }
        }
        Text("Contraseña actualizada", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = colorScheme.onBackground, textAlign = TextAlign.Center)
        Text(
            mensaje ?: "Ya puedes iniciar sesión con tu contraseña nueva.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onIrAlLogin, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Ir a iniciar sesión")
        }
    }
}
