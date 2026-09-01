package com.example.kaffacafeteria.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

@Composable
fun LoginScreen(
<<<<<<< HEAD
    onLoginSuccess: (com.example.kaffacafeteria.domain.model.User?) -> Unit,
=======
    onLoginSuccess: () -> Unit,
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = createViewModel { LoginViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isLoggedIn) {
<<<<<<< HEAD
        if (state.isLoggedIn) onLoginSuccess(state.user)
=======
        if (state.isLoggedIn) onLoginSuccess()
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colorScheme.background).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Kaffa", style = MaterialTheme.typography.headlineLarge, color = colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Cafetería POS", style = MaterialTheme.typography.titleMedium, color = colorScheme.tertiary)
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = state.correo, onValueChange = viewModel::updateCorreo,
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            isError = state.correoError != null,
            supportingText = state.correoError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                focusedLabelColor = colorScheme.primary,
                cursorColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline,
                unfocusedLabelColor = colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password, onValueChange = viewModel::updatePassword,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = state.passwordError != null,
            supportingText = state.passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.login() }),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                focusedLabelColor = colorScheme.primary,
                cursorColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline,
                unfocusedLabelColor = colorScheme.onSurfaceVariant
            )
        )

        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { viewModel.login() }, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) {
            if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
            else Text("Iniciar Sesión", color = colorScheme.onSecondary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) { Text("¿No tienes cuenta? Regístrate", color = colorScheme.secondary) }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
