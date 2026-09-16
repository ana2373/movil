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
import com.example.kaffacafeteria.ui.theme.*
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
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text("Promociones") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
            actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Crear promoción", tint = colorScheme.onPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        when {
            state.promociones.isEmpty() -> EmptyState("No hay promociones. Pulsa + para crear una.")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.promociones, key = { it.id }) { promo ->
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
                                    Text(promo.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = palette.titleText)
                                    Text(promo.tipo, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = palette.highlightText)
                                }
                                TaskStatus(
                                    activa = promo.activa,
                                    activaColor = palette.accentDark,
                                    onClick = { viewModel.toggleActiva(promo) }
                                )
                            }
                            promo.imagenUri?.let { uri ->
                                Spacer(modifier = Modifier.height(10.dp))
                                AsyncImage(model = uri, contentDescription = promo.nombre, modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                            }
                            if (promo.descripcion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(promo.descripcion, style = MaterialTheme.typography.bodyMedium, color = palette.subtitleText)
                            }
                            if (promo.estaExpirada()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Expirada", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                            }
                            if (promo.fechaInicio != null || promo.fechaFin != null || promo.expiracionMillis != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    listOfNotNull(
                                        promo.fechaInicio?.let { "Inicio: $it" },
                                        promo.fechaFin?.let { "Fin: $it" },
                                        promo.expiracionMillis?.let { "Expira: ${formatExpiracion(it)}" }
                                    ).joinToString("  ·  "),
                                    style = MaterialTheme.typography.labelSmall, color = palette.subtitleText
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { viewModel.delete(promo) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePromocionDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { promo ->
                viewModel.create(promo)
                showCreateDialog = false
            }
        )
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
            Box(modifier = Modifier.size(8.dp).background(if (activa) activaColor else Color.Gray, RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (activa) "Activa" else "Inactiva", fontSize = 12.sp, color = if (activa) activaColor else Color.Gray, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePromocionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Promocion) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(listOf("Descuento", "2x1", "Combo", "Personalizado").first()) }
    var colorIndex by remember { mutableStateOf(0) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var expDiaMillis by remember { mutableStateOf<Long?>(null) }
    var expHoraMin by remember { mutableStateOf<Int?>(null) }
    val tipos = listOf("Descuento", "2x1", "Combo", "Personalizado")

    val expDiaLabel = expDiaMillis?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "Elegir día"
    val expHoraLabel = expHoraMin?.let { "%02d:%02d".format(it / 60, it % 60) } ?: "Elegir hora"

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> imagenUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Promoción", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre de la promoción") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Mensaje a mostrar (ej. 20% DCTO en bebidas)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))

                Column {
                    Text("Tipo de promoción:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row {
                        tipos.forEach { t ->
                            FilterChip(
                                selected = tipo == t,
                                onClick = { tipo = t },
                                label = { Text(t) },
                                modifier = Modifier.padding(end = 6.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colorScheme.secondary, selectedLabelColor = colorScheme.onSecondary)
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
                                        if (colorIndex == index) Modifier.border(3.dp, colorScheme.primary, RoundedCornerShape(8.dp)).padding(1.dp)
                                        else Modifier
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
                        AsyncImage(model = imagenUri, contentDescription = "Imagen", modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        TextButton(onClick = { imagenUri = null }) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
                    } else {
                        OutlinedButton(onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Elegir imagen")
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fechaInicio, onValueChange = { fechaInicio = it }, label = { Text("Inicio (AAAA-MM-DD)") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                    OutlinedTextField(value = fechaFin, onValueChange = { fechaFin = it }, label = { Text("Fin (AAAA-MM-DD)") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant))
                }

                Column {
                    Text("Expiración (día y hora, opcional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val cal = (expDiaMillis?.let { Calendar.getInstance().apply { timeInMillis = it } } ?: Calendar.getInstance())
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        expDiaMillis = Calendar.getInstance().apply {
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
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(expDiaLabel, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = {
                                val default = expHoraMin ?: (12 * 60)
                                TimePickerDialog(
                                    context,
                                    { _, h, m -> expHoraMin = h * 60 + m },
                                    default / 60, default % 60, true
                                ).show()
                            },
                            enabled = expDiaMillis != null,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(expHoraLabel)
                        }
                    }
                    if (expDiaMillis != null || expHoraMin != null) {
                        TextButton(onClick = { expDiaMillis = null; expHoraMin = null }) { Text("Quitar expiración", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val limite: Long? = when {
                    expDiaMillis != null && expHoraMin != null -> Calendar.getInstance().apply {
                        timeInMillis = expDiaMillis!!
                        set(Calendar.HOUR_OF_DAY, expHoraMin!! / 60)
                        set(Calendar.MINUTE, expHoraMin!! % 60)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    expDiaMillis != null -> Calendar.getInstance().apply {
                        timeInMillis = expDiaMillis!!
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    else -> null
                }
                onConfirm(
                    Promocion(
                        id = 0,
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim(),
                        tipo = tipo,
                        colorIndex = colorIndex,
                        imagenUri = imagenUri?.toString(),
                        fechaInicio = fechaInicio.ifBlank { null },
                        fechaFin = fechaFin.ifBlank { null },
                        expiracionMillis = limite,
                        activa = true
                    )
                )
            },
                enabled = nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)) { Text("Crear", color = colorScheme.onSecondary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}