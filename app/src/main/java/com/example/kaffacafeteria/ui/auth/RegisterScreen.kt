package com.example.kaffacafeteria.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = createViewModel { RegisterViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme

    // El backend no inicia sesión al registrarse: se muestra la pantalla de
    // "revisa tu correo" en lugar de navegar al inicio.
    if (state.pendienteVerificacion) {
        RegistrationPendingScreen(state = state, viewModel = viewModel, onIrAlLogin = onRegisterSuccess)
        return
    }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Crear Cuenta") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = state.nombre, onValueChange = viewModel::updateNombre, label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = state.nombreError != null, supportingText = state.nombreError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, cursorColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
            OutlinedTextField(value = state.correo, onValueChange = viewModel::updateCorreo, label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = state.correoError != null, supportingText = state.correoError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, cursorColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
            OutlinedTextField(value = state.password, onValueChange = viewModel::updatePassword, label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null) } },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = state.passwordError != null, supportingText = state.passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, cursorColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
            OutlinedTextField(value = state.confirmPassword, onValueChange = viewModel::updateConfirmPassword, label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = { IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null) } },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = state.confirmPasswordError != null, supportingText = state.confirmPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.register() }),
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, cursorColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
            if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Button(onClick = { viewModel.register() }, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                else Text("Crear Cuenta", color = colorScheme.onSecondary)
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = colorScheme.primary)
            }
        }
    }
}

/**
 * Pantalla mostrada tras crear la cuenta.
 * Permite ingresar el código recibido por correo o reenviarlo si venció.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationPendingScreen(
    state: RegisterUiState,
    viewModel: RegisterViewModel,
    onIrAlLogin: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Verificar Cuenta") },
            navigationIcon = {
                IconButton(onClick = viewModel::volverAlFormulario) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = colorScheme.secondary.copy(alpha = 0.15f),
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = colorScheme.secondary, modifier = Modifier.size(44.dp))
                }
            }

            Text("¡Ingresa tu código!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = colorScheme.onBackground)

            Text(
                state.mensajeVerificacion
                    ?: "Enviamos un código de verificación a tu correo. Ingrésalo a continuación para activar tu cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (state.correoRegistrado.isNotBlank()) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Código enviado a", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                        Text(state.correoRegistrado, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }

            // =========================================================================
            // CAMPO PARA INGRESAR EL CÓDIGO
            // =========================================================================
            OutlinedTextField(
                value = state.codigoVerificacion, // Asegúrate de tener esta propiedad en tu State
                onValueChange = viewModel::updateCodigoVerificacion, // Asegúrate de implementar este método en tu ViewModel
                label = { Text("Código de verificación") },
                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                isError = state.codigoError != null,
                supportingText = state.codigoError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.verificarCodigo()
                }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    unfocusedLabelColor = colorScheme.onSurfaceVariant
                )
            )

            if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }

            // BOTÓN DE CONFIRMAR CÓDIGO
            Button(
                onClick = { viewModel.verificarCodigo() },
                enabled = !state.isVerifying && state.codigoVerificacion.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
            ) {
                if (state.isVerifying) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("Confirmar Código", color = colorScheme.onPrimary, textAlign = TextAlign.Center)
            }

            // BOTÓN DE REENVIAR CORREO
            OutlinedButton(
                onClick = { viewModel.reenviarVerificacion() },
                enabled = !state.isResending,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isResending) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = colorScheme.primary, strokeWidth = 2.dp)
                else Text("Reenviar código de verificación", color = colorScheme.primary, textAlign = TextAlign.Center)
            }

            TextButton(onClick = viewModel::editarCorreo) {
                Text("Usar otro correo", color = colorScheme.onSurfaceVariant)
            }

            TextButton(onClick = onIrAlLogin) {
                Text("Ir a iniciar sesión", color = colorScheme.primary)
            }
        }
    }
}