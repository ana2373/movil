package com.example.kaffacafeteria.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.data.local.ReportArchive
import com.example.kaffacafeteria.ui.components.EmptyState
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.ui.theme.PrimaryGreen
import com.example.kaffacafeteria.util.PdfFileActions
import com.example.kaffacafeteria.util.createViewModel

/**
 * Listado de reportes PDF ya generados.
 *
 * El archivo se conserva dentro de la app hasta que un administrador lo
 * elimina; cualquier usuario puede abrirlo, compartirlo o descargar una
 * copia a la carpeta Descargas del teléfono.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedReportsScreen(
    onBack: () -> Unit,
    esAdmin: Boolean = false,
    viewModel: ReportArchiveViewModel = createViewModel { ReportArchiveViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<ReportArchive.ReporteArchivado?>(null) }

    LaunchedEffect(Unit) { viewModel.cargar() }

    LaunchedEffect(state.mensaje, state.error) {
        (state.mensaje ?: state.error)?.let {
            snackbarHost.showSnackbar(it)
            viewModel.limpiarAviso()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TopAppBar(
                title = { Text("Reportes guardados") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
            )

            if (state.cargando && state.reportes.isEmpty()) {
                LoadingIndicator()
            } else if (state.reportes.isEmpty()) {
                EmptyState("Aún no has generado ningún reporte PDF")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Los PDF quedan guardados en la aplicación. Puedes abrirlos, compartirlos o descargarlos las veces que necesites; sólo un administrador puede eliminarlos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(state.reportes, key = { it.id }) { reporte ->
                        ArchivedReportCard(
                            reporte = reporte,
                            esAdmin = esAdmin,
                            onAbrir = { PdfFileActions.abrir(context, viewModel.archivoDe(reporte)) },
                            onCompartir = { PdfFileActions.compartir(context, viewModel.archivoDe(reporte), reporte.titulo) },
                            onDescargar = { viewModel.publicarEnDescargas(reporte) },
                            onEliminar = { porEliminar = reporte }
                        )
                    }
                }
            }
        }
    }

    porEliminar?.let { reporte ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar reporte", fontWeight = FontWeight.Bold) },
            text = { Text("Se borrará \"${reporte.titulo}\" del ${reporte.fechaGenerado}. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminar(reporte.id, esAdmin); porEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ArchivedReportCard(
    reporte: ReportArchive.ReporteArchivado,
    esAdmin: Boolean,
    onAbrir: () -> Unit,
    onCompartir: () -> Unit,
    onDescargar: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryGreen.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PrimaryGreen)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(reporte.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(reporte.periodo, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    Text(
                        "${reporte.fechaGenerado} · ${reporte.tamanoBytes / 1024} KB · ${reporte.generadoPor}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                if (esAdmin) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar reporte", tint = colorScheme.error)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAbrir, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Abrir")
                }
                OutlinedButton(onClick = onCompartir, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Compartir")
                }
                OutlinedButton(onClick = onDescargar, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Guardar")
                }
            }
        }
    }
}
