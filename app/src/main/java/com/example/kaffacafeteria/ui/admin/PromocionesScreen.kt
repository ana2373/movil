package com.example.kaffacafeteria.ui.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.PromoPalettes
import com.example.kaffacafeteria.util.createViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocionesScreen(
    onBack: () -> Unit,
    viewModel: PromocionesViewModel = createViewModel { PromocionesViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme

    // Una sola variable controla el diálogo: null = cerrado, con id = edición.
    var promoEnEdicion by remember { mutableStateOf<Promocion?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    // Diálogo de confirmación de borrado (evita eliminar con un toque accidental).
    var porEliminar by remember { mutableStateOf<Promocion?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Promociones") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary)
                }
            },
            actions = {
                IconButton(onClick = { promoEnEdicion = null; mostrarDialogo = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Crear promoción", tint = colorScheme.onPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.primary,
                titleContentColor = colorScheme.onPrimary
            )
        )

        when {
            state.isLoading -> LoadingIndicator()
            state.promociones.isEmpty() -> EmptyState("No hay promociones. Pulsa + para crear una.")
            else -> LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.promociones, key = { it.id }) { promo ->
                    PromocionCard(
                        promo = promo,
                        onToggleActiva = { viewModel.toggleActiva(promo) },
                        onEditar = { promoEnEdicion = promo; mostrarDialogo = true },
                        onEliminar = { porEliminar = promo }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        PromocionDialog(
            inicial = promoEnEdicion,
            onDismiss = { mostrarDialogo = false; promoEnEdicion = null },
            onConfirm = { promo ->
                if (promoEnEdicion != null) viewModel.update(promo) else viewModel.create(promo)
                mostrarDialogo = false
                promoEnEdicion = null
            }
        )
    }

    porEliminar?.let { promo ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar promoción", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará \"${promo.nombre}\" y ya no se mostrará en el inicio. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.delete(promo)
                        porEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun PromocionCard(
    promo: Promocion,
    onToggleActiva: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val palette = PromoPalettes.getOrElse(promo.colorIndex) { PromoPalettes.first() }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        promo.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = palette.titleText
                    )
                    Text(
                        promo.tipo,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.highlightText
                    )
                }
                TaskStatus(
                    activa = promo.activa,
                    activaColor = palette.accentDark,
                    onClick = onToggleActiva
                )
            }

            promo.imagenUri?.let { uri ->
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = uri,
                    contentDescription = promo.nombre,
                    modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (promo.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(promo.descripcion, style = MaterialTheme.typography.bodyMedium, color = palette.subtitleText)
            }

            Spacer(modifier = Modifier.height(6.dp))
            if (promo.expiracionMillis != null) {
                Text(
                    "Vence: ${formatExpiracion(promo.expiracionMillis)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (promo.estaExpirada()) MaterialTheme.colorScheme.error else palette.subtitleText,
                    fontWeight = if (promo.estaExpirada()) FontWeight.Bold else FontWeight.Normal
                )
                if (promo.estaExpirada()) {
                    Text(
                        "Expirada",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    "Sin fecha de vencimiento",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.subtitleText
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar")
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun TaskStatus(activa: Boolean, activaColor: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (activa) activaColor.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.4f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (activa) activaColor else Color.Gray, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (activa) "Activa" else "Inactiva",
                fontSize = 12.sp,
                color = if (activa) activaColor else Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Alta y edición de promociones.
 *
 * La vigencia se elige con un selector de día y otro de hora en formato 12 h
 * (am/pm). Si no se elige nada, la promoción queda sin vencimiento y sólo
 * termina cuando el administrador la desactiva o la elimina.
 *
 * @param inicial promoción a editar; `null` para crear una nueva.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromocionDialog(
    inicial: Promocion?,
    onDismiss: () -> Unit,
    onConfirm: (Promocion) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    var nombre by remember { mutableStateOf(inicial?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(inicial?.descripcion ?: "") }
    var tipo by remember { mutableStateOf(inicial?.tipo ?: TIPOS_PROMOCION.first()) }
    var colorIndex by remember { mutableStateOf(inicial?.colorIndex ?: 0) }
    var imagenUri by remember { mutableStateOf(inicial?.imagenUri) }

    // Día y hora por separado; se combinan al guardar.
    var diaMillis by remember { mutableStateOf(inicial?.expiracionMillis) }
    var hora by remember { mutableStateOf(inicial?.expiracionMillis?.let { minutoDelDia(it) } ?: DEFAULT_HORA) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> imagenUri = uri?.toString() }

    val esEdicion = inicial != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (esEdicion) "Editar Promoción" else "Crear Promoción",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la promoción") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Mensaje a mostrar (ej. 20% DCTO en bebidas)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant
                    )
                )

                Column {
                    Text("Tipo de promoción:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row {
                        TIPOS_PROMOCION.forEach { t ->
                            FilterChip(
                                selected = tipo == t,
                                onClick = { tipo = t },
                                label = { Text(t) },
                                modifier = Modifier.padding(end = 6.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorScheme.secondary,
                                    selectedLabelColor = colorScheme.onSecondary
                                )
                            )
                        }
                    }
                }

                Column {
                    Text("Color de la promoción:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row {
                        PromoPalettes.forEachIndexed { index, palette ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.accentDark)
                                    .clickable { colorIndex = index }
                                    .then(
                                        if (colorIndex == index) {
                                            Modifier.border(3.dp, colorScheme.primary, RoundedCornerShape(8.dp))
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (colorIndex == index) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (imagenUri != null) {
                        AsyncImage(
                            model = imagenUri,
                            contentDescription = "Imagen",
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        TextButton(onClick = { imagenUri = null }) {
                            Text("Quitar", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Elegir imagen")
                        }
                    }
                }

                // ── Vigencia: día + hora en formato am/pm ──
                Column {
                    Text(
                        "Fecha y hora de vencimiento",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val cal = diaMillis?.let { Calendar.getInstance().apply { timeInMillis = it } }
                                    ?: Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        diaMillis = Calendar.getInstance().apply {
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
                                diaMillis?.let { formatDia(it) } ?: "Elegir fecha",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                // is24HourView = false → selector de 12 h con AM/PM.
                                TimePickerDialog(
                                    context,
                                    { _, h, m -> hora = h to m },
                                    hora.first,
                                    hora.second,
                                    false
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(formatHora12(hora.first, hora.second))
                        }
                    }
                    Text(
                        "Si no eliges fecha, la promoción queda activa hasta que la desactives.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    if (diaMillis != null) {
                        TextButton(onClick = { diaMillis = null }) {
                            Text("Quitar vencimiento", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        Promocion(
                            id = inicial?.id ?: 0,
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim(),
                            tipo = tipo,
                            colorIndex = colorIndex,
                            imagenUri = imagenUri,
                            // Sólo hay vencimiento si se escogió día.
                            expiracionMillis = diaMillis?.let { buildExpiracionMillis(it, hora.first, hora.second) },
                            activa = inicial?.activa ?: true
                        )
                    )
                },
                enabled = nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.secondary,
                    contentColor = colorScheme.onSecondary
                )
            ) {
                Text(if (esEdicion) "Guardar cambios" else "Crear", color = colorScheme.onSecondary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/** Minutos desde medianoche de un instante, para recuperar la hora al editar. */
private fun minutoDelDia(millis: Long): Pair<Int, Int> {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
}

/** "25/06/2025" */
private fun formatDia(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))

/** "09:05 AM" / "02:30 PM". */
private fun formatHora12(hora24: Int, minuto: Int): String {
    val suffix = if (hora24 < 12) "AM" else "PM"
    val h12 = when {
        hora24 % 12 == 0 -> 12
        else -> hora24 % 12
    }
    return "%02d:%02d %s".format(h12, minuto, suffix)
}

private val TIPOS_PROMOCION = listOf("Descuento", "2x1", "Combo", "Personalizado")

/** Hora por defecto cuando el usuario no escoge otra: 5:00 PM. */
private val DEFAULT_HORA = 17 to 0
