package com.example.kaffacafeteria.ui.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kaffacafeteria.data.remote.dto.EventoDto
import com.example.kaffacafeteria.data.remote.dto.EventoRequest
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(
    onBack: () -> Unit,
    viewModel: EventosViewModel = createViewModel { EventosViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }

    var eventoEnEdicion by remember { mutableStateOf<EventoDto?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var porEliminar by remember { mutableStateOf<EventoDto?>(null) }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Eventos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { eventoEnEdicion = null; mostrarDialogo = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Crear evento", tint = colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(colorScheme.background)) {
            when {
                state.isLoading && state.eventos.isEmpty() -> LoadingIndicator()
                state.eventos.isEmpty() -> EmptyState("No hay eventos. Pulsa + para crear uno.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.eventos, key = { it.id }) { evento ->
                        EventoCard(
                            evento = evento,
                            onTogglePublicado = { viewModel.togglePublicado(evento) },
                            onEditar = { eventoEnEdicion = evento; mostrarDialogo = true },
                            onEliminar = { porEliminar = evento }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        EventoDialog(
            inicial = eventoEnEdicion,
            subiendoImagen = state.subiendoImagen,
            onSubirImagen = { uri, onUrl -> viewModel.subirImagen(uri, onUrl) },
            onDismiss = { mostrarDialogo = false; eventoEnEdicion = null },
            onConfirm = { request ->
                if (eventoEnEdicion != null) viewModel.update(eventoEnEdicion!!.id, request) else viewModel.create(request)
                mostrarDialogo = false
                eventoEnEdicion = null
            }
        )
    }

    porEliminar?.let { evento ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar evento", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará \"${evento.titulo}\" y dejará de mostrarse en la web. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.delete(evento.id); porEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun EventoCard(
    evento: EventoDto,
    onTogglePublicado: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val publicadoColor = PrimaryGreen
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            evento.portada?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = evento.titulo,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        evento.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    EstadoPublicacion(publicado = evento.publicado, color = publicadoColor, onClick = onTogglePublicado)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        textoFechaHora(evento.fecha, evento.hora),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                evento.lugar?.takeIf { it.isNotBlank() }?.let { lugar ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(lugar, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (evento.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        evento.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar")
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoPublicacion(publicado: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (publicado) color.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.4f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(if (publicado) color else Color.Gray, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(
                if (publicado) "Publicado" else "Borrador",
                fontSize = 12.sp,
                color = if (publicado) color else Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Alta y edición de eventos.
 *
 * La imagen se sube al backend en cuanto se elige y se guarda su URL; el
 * evento se envía con esa URL en `imagenes`. `fecha` viaja como "YYYY-MM-DD"
 * y `hora` como "HH:mm".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventoDialog(
    inicial: EventoDto?,
    subiendoImagen: Boolean,
    onSubirImagen: (Uri, (String?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (EventoRequest) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val esEdicion = inicial != null

    var titulo by remember { mutableStateOf(inicial?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(inicial?.descripcion ?: "") }
    var lugar by remember { mutableStateOf(inicial?.lugar ?: "") }
    var publicado by remember { mutableStateOf(inicial?.publicado ?: true) }
    var imagenUrl by remember { mutableStateOf(inicial?.portada) }

    var fechaMillis by remember { mutableStateOf(parseFecha(inicial?.fecha)) }
    var hora by remember { mutableStateOf(parseHora(inicial?.hora) ?: DEFAULT_HORA) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onSubirImagen(uri) { url -> if (url != null) imagenUrl = url }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (esEdicion) "Editar Evento" else "Crear Evento", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del evento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogFieldColors(colorScheme)
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogFieldColors(colorScheme)
                )

                OutlinedTextField(
                    value = lugar,
                    onValueChange = { lugar = it },
                    label = { Text("Lugar (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogFieldColors(colorScheme)
                )

                // ── Fecha (obligatoria) y hora (opcional) ──
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val cal = fechaMillis?.let { Calendar.getInstance().apply { timeInMillis = it } }
                                ?: Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    fechaMillis = Calendar.getInstance().apply {
                                        set(y, m, d, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                },
                                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            fechaMillis?.let { formatDia(it) } ?: "Elegir fecha",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, h, m -> hora = h to m }, hora.first, hora.second, false).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(formatHora12(hora.first, hora.second))
                    }
                }

                // ── Imagen ──
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (imagenUrl != null) {
                        AsyncImage(
                            model = imagenUrl,
                            contentDescription = "Imagen del evento",
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        TextButton(onClick = { imagenUrl = null }) {
                            Text("Quitar", color = colorScheme.error)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            enabled = !subiendoImagen
                        ) {
                            if (subiendoImagen) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(if (subiendoImagen) "Subiendo…" else "Elegir imagen")
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = publicado, onCheckedChange = { publicado = it }, colors = SwitchDefaults.colors(checkedThumbColor = colorScheme.onPrimary, checkedTrackColor = colorScheme.primary))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (publicado) "Publicado (visible en la web)" else "Borrador (oculto)",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val millis = fechaMillis ?: return@Button
                    onConfirm(
                        EventoRequest(
                            titulo = titulo.trim(),
                            descripcion = descripcion.trim(),
                            lugar = lugar.trim().ifBlank { null },
                            fecha = fechaIso(millis),
                            hora = "%02d:%02d".format(hora.first, hora.second),
                            publicado = publicado,
                            // null → en edición el backend conserva la galería previa.
                            imagenes = imagenUrl?.let { listOf(it) } ?: if (esEdicion) null else emptyList()
                        )
                    )
                },
                enabled = titulo.isNotBlank() && fechaMillis != null && !subiendoImagen,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)
            ) {
                Text(if (esEdicion) "Guardar cambios" else "Crear", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun dialogFieldColors(colorScheme: androidx.compose.material3.ColorScheme) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colorScheme.primary,
        focusedLabelColor = colorScheme.primary,
        unfocusedBorderColor = colorScheme.outline,
        unfocusedLabelColor = colorScheme.onSurfaceVariant
    )

// ── Fechas y horas ──

private val DEFAULT_HORA = 18 to 0

/** "2025-06-25" → millis a medianoche, o null si no se pudo leer. */
private fun parseFecha(fecha: String?): Long? {
    if (fecha.isNullOrBlank()) return null
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)?.time
    } catch (_: Exception) {
        null
    }
}

/** millis → "2025-06-25" (formato que espera el backend). */
private fun fechaIso(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

/** "25/06/2025" para mostrar en el botón. */
private fun formatDia(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))

/** "18:30:00" → (18, 30), o null si no es válido. */
private fun parseHora(hora: String?): Pair<Int, Int>? {
    if (hora.isNullOrBlank()) return null
    val partes = hora.split(":")
    if (partes.size < 2) return null
    val h = partes[0].toIntOrNull() ?: return null
    val m = partes[1].toIntOrNull() ?: return null
    return h to m
}

/** "09:05 AM" / "06:30 PM". */
private fun formatHora12(hora24: Int, minuto: Int): String {
    val suffix = if (hora24 < 12) "AM" else "PM"
    val h12 = if (hora24 % 12 == 0) 12 else hora24 % 12
    return "%02d:%02d %s".format(h12, minuto, suffix)
}

/** Texto de fecha/hora para la tarjeta: "25/06/2025 · 06:30 PM". */
private fun textoFechaHora(fecha: String, hora: String?): String {
    val dia = parseFecha(fecha)?.let { formatDia(it) } ?: fecha
    val h = parseHora(hora)?.let { formatHora12(it.first, it.second) }
    return if (h != null) "$dia · $h" else dia
}