package com.example.kaffacafeteria.ui.admin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.local.ReportArchive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReportArchiveUiState(
    val cargando: Boolean = true,
    val reportes: List<ReportArchive.ReporteArchivado> = emptyList(),
    val error: String? = null,
    val mensaje: String? = null
)

/**
 * Gestiona la lista de reportes PDF ya generados.
 *
 * Permisos:
 *  - Cualquier usuario autenticado puede consultar, abrir, compartir o
 *    volver a descargar un reporte existente.
 *  - Únicamente el administrador puede eliminar el archivo del dispositivo
 *    ([eliminar] valida el rol antes de tocar el disco).
 */
class ReportArchiveViewModel(application: Application) : AndroidViewModel(application) {

    private val archive = ReportArchive(application)

    var uiState by mutableStateOf(ReportArchiveUiState())
        private set

    fun cargar() {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true, error = null)
            val lista = withContext(Dispatchers.IO) {
                runCatching { archive.listarTodos() }.getOrDefault(emptyList())
            }
            uiState = uiState.copy(cargando = false, reportes = lista)
        }
    }

    fun eliminar(id: String, esAdmin: Boolean) {
        if (!esAdmin) {
            uiState = uiState.copy(error = "Sólo un administrador puede eliminar reportes guardados")
            return
        }
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { archive.eliminar(id) }
            cargar()
            uiState = uiState.copy(
                mensaje = if (ok) "Reporte eliminado" else "No se pudo eliminar el reporte",
                error = if (ok) null else "No se pudo eliminar el reporte"
            )
        }
    }

    fun limpiarAviso() {
        uiState = uiState.copy(mensaje = null, error = null)
    }

    fun archivoDe(reporte: ReportArchive.ReporteArchivado) = archive.archivoDe(reporte)

    fun publicarEnDescargas(reporte: ReportArchive.ReporteArchivado): String? {
        val archivo = archive.archivoDe(reporte)
        if (!archivo.exists()) {
            uiState = uiState.copy(error = "El archivo ya no está en el dispositivo")
            return null
        }
        val uri = archive.publicarEnDescargas(archivo)
        return if (uri != null) {
            uiState = uiState.copy(mensaje = "Reporte guardado en Descargas", error = null)
            uri.toString()
        } else {
            uiState = uiState.copy(error = "No se pudo guardar en Descargas. Usa compartir para enviarlo.")
            null
        }
    }
}
