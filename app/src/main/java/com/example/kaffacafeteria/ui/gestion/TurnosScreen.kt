package com.example.kaffacafeteria.ui.gestion

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnosScreen(
    onBack: () -> Unit,
    viewModel: TurnosViewModel = createViewModel { TurnosViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadTurnos()
        viewModel.loadBaristas()
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            snackbarHostState.showSnackbar(state.successMessage!!)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error!!)
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Turnos Laborales") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
                actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Agregar turno", tint = colorScheme.onPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
            )

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ink(CoffeeBrown).copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = ink(CoffeeBrown), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Asigna turnos (mañana o tarde) a tus baristas. Los pedidos requieren que el barista tenga un turno activo.", style = MaterialTheme.typography.bodySmall, color = ink(CoffeeBrown))
                    }
                }

                when {
                    state.isLoading && state.turnos.isEmpty() -> Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colorScheme.primary) }
                    state.turnos.isEmpty() -> Text("No hay turnos asignados", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
                    else -> state.turnos.forEach { turno ->
                        val esManana = turno.tipo.lowercase().startsWith("ma")
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(12.dp), color = ink(if (esManana) Terracotta else PrimaryGreen).copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = ink(if (esManana) Terracotta else PrimaryGreen)) }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(formatFechaTurno(turno.fecha), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${turno.tipo.replaceFirstChar { it.uppercase() }} · ${horarioDe(turno.tipo)}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                    val nombres = turno.baristas?.joinToString { it.nombre }?.takeIf { it.isNotBlank() } ?: "Sin barista"
                                    Text(nombres, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { viewModel.deleteTurno(turno.id) }) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (showCreateDialog) {
        var selectedFecha by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)) }
        var tipo by remember { mutableStateOf("mañana") }
        val selectedIds = remember { mutableStateListOf<Int>() }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Registrar Turno", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val fechaLabel = runCatching {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedFecha)!!
                        )
                    }.getOrDefault(selectedFecha)
                    OutlinedTextField(
                        value = fechaLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline, unfocusedLabelColor = colorScheme.onSurfaceVariant)
                    )
                    TextButton(onClick = {
                        val cal = Calendar.getInstance()
                        runCatching { cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedFecha)!! }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val c = Calendar.getInstance().apply { set(year, month, day) }
                                selectedFecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
                            },
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) { Text("Elegir fecha") }

                    Column {
                        Text("Turno:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row {
                            listOf("mañana" to "Mañana", "tarde" to "Tarde").forEach { (valor, etiqueta) ->
                                FilterChip(
                                    selected = tipo == valor,
                                    onClick = { tipo = valor },
                                    label = { Text(etiqueta) },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colorScheme.secondary, selectedLabelColor = colorScheme.onSecondary)
                                )
                            }
                        }
                    }

                    Column {
                        Text("Baristas:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        if (state.baristas.isEmpty()) {
                            Text("No hay baristas registrados. Crea uno desde el panel de administración.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        } else {
                            state.baristas.forEach { b ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Checkbox(
                                        checked = selectedIds.contains(b.id),
                                        onCheckedChange = { checked ->
                                            if (checked) { if (!selectedIds.contains(b.id)) selectedIds.add(b.id) }
                                            else selectedIds.remove(b.id)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                                    )
                                    Text(b.nombre)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createTurno(selectedFecha, tipo, selectedIds.toList())
                        showCreateDialog = false
                    },
                    enabled = selectedIds.isNotEmpty() && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)
                ) { Text("Guardar", color = colorScheme.onSecondary) }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") } }
        )
    }
}

private fun horarioDe(tipo: String): String =
    if (tipo.lowercase().startsWith("ma")) "07:00 - 13:00" else "13:00 - 18:00"

private fun formatFechaTurno(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"
    val datePart = raw.substringBefore('T')
    return runCatching {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(datePart)!!
        )
    }.getOrDefault(datePart)
}
