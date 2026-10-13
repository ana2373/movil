package com.example.kaffacafeteria.ui.asistente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kaffacafeteria.data.remote.dto.AsistenteMensajeDto
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

/**
 * Ícono flotante del Asistente KAFFA para todos los usuarios autenticados.
 *
 * Es un overlay transparente (no bloquea el resto de la UI): sólo el botón
 * circular captura el toque. Al pulsarlo se abre el chat en un diálogo.
 */
@Composable
fun AsistenteFlotante(
    rol: String? = null,
    sessionKey: Int = 0,
    viewModel: AsistenteViewModel = createViewModel { AsistenteViewModel(it) }
) {
    // Al cambiar de sesión (login/logout) se descarta el historial anterior para
    // no mezclar las conversaciones de distintos usuarios o roles.
    LaunchedEffect(sessionKey) { viewModel.sincronizarSesion(sessionKey) }

    var abierto by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { abierto = true },
            containerColor = CafeOscuro,
            contentColor = Blanco,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Coffee, contentDescription = "Asistente KAFFA", modifier = Modifier.size(26.dp))
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CafeClaro,
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                )
            }
        }
    }

    if (abierto) {
        AsistenteChatDialog(
            rol = rol,
            viewModel = viewModel,
            onClose = { abierto = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsistenteChatDialog(
    rol: String?,
    viewModel: AsistenteViewModel,
    onClose: () -> Unit
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current
    var texto by remember { mutableStateOf("") }

    // Mantener siempre visible el último mensaje.
    LaunchedEffect(state.mensajes.size, state.enviando) {
        if (state.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(state.mensajes.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Cabecera ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(CafeOscuro, CoffeeBrown)))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = CafeClaro, modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Coffee, contentDescription = null, tint = CafeOscuro, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Asistente KAFFA", color = Blanco, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text("Pregúntame lo que necesites", color = Blanco.copy(alpha = 0.85f), fontSize = 12.sp)
                        }
                        IconButton(onClick = { viewModel.limpiarConversacion() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar", tint = Blanco)
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Blanco)
                        }
                    }
                }

                // ── Mensajes ──
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (state.cargandoHistorial && state.mensajes.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = colorScheme.primary)
                    } else if (state.mensajes.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Coffee, contentDescription = null, tint = CafeClaro, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "¡Hola! Soy tu asistente de café ☕",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Escríbeme o toca una sugerencia para empezar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            sugerenciasPorRol(rol).forEach { sugerencia ->
                                SuggestionChip2(sugerencia) { viewModel.enviar(sugerencia) }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.mensajes.size) { index ->
                                MensajeBurbuja(state.mensajes[index])
                            }
                            if (state.enviando) {
                                item {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colorScheme.primary)
                                        Spacer(Modifier.width(8.dp))
                                        Text("El asistente está escribiendo…", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                state.error?.let {
                    Text(
                        it,
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // ── Entrada ──
                Surface(color = colorScheme.surface, tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { texto = it },
                            placeholder = { Text("Escribe tu mensaje…") },
                            modifier = Modifier.weight(1f),
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                val mensaje = texto
                                texto = ""
                                keyboard?.hide()
                                viewModel.enviar(mensaje)
                            },
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeBurbuja(mensaje: AsistenteMensajeDto) {
    val colorScheme = MaterialTheme.colorScheme
    val esAsistente = mensaje.esAsistente
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esAsistente) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (esAsistente) 4.dp else 16.dp,
                bottomEnd = if (esAsistente) 16.dp else 4.dp
            ),
            color = if (esAsistente) colorScheme.surfaceVariant else colorScheme.primary,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(
                text = if (esAsistente) limpiarMarkdown(mensaje.contenido) else mensaje.contenido,
                color = if (esAsistente) colorScheme.onSurfaceVariant else colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun SuggestionChip2(texto: String, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.secondaryContainer,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            texto,
            color = colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

/** Sugerencias rápidas adaptadas a lo que cada rol realmente puede consultar. */
private fun sugerenciasPorRol(rol: String?): List<String> = when (rol) {
    "admin" -> listOf(
        "¿Cómo van las ventas de esta semana?",
        "Predice las ventas de los próximos 7 días",
        "¿Qué insumos debo reponer?"
    )
    "barista" -> listOf(
        "¿Cómo van las ventas de hoy?",
        "¿Cuántos pedidos pendientes hay?",
        "¿Qué insumos debo reponer?"
    )
    else -> listOf(
        "¿Qué productos tienen?",
        "¿Cómo hago un pedido?",
        "¿Qué eventos hay próximos?"
    )
}

/** Quita los marcadores de markdown que usa el backend (**negrita**) para que
 *  el texto se lea limpio en la burbuja. */
private fun limpiarMarkdown(texto: String): String = texto.replace("**", "")