package com.example.kaffacafeteria.ui.admin

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.EventoDto
import com.example.kaffacafeteria.data.remote.dto.EventoRequest
import com.example.kaffacafeteria.util.ApiErrors
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class EventosUiState(
    val eventos: List<EventoDto> = emptyList(),
    val isLoading: Boolean = false,
    val subiendoImagen: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * Administración de eventos de la cafetería (panel de la app).
 *
 * Todo se persiste en el backend, por lo que lo que se cree o publique aquí
 * aparece automáticamente en la web (sección pública "Eventos").
 */
class EventosViewModel(application: Application) : AndroidViewModel(application) {
    private val api = (application as KaffaApp).container.eventoApi

    var uiState by mutableStateOf(EventosUiState())
        private set

    init { load() }

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = api.getEventosAdmin(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(eventos = response.body()?.data ?: emptyList(), isLoading = false)
                } else {
                    uiState = uiState.copy(isLoading = false, error = ApiErrors.parse(response, "No se pudieron cargar los eventos"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error de conexión")
            }
        }
    }

    fun create(request: EventoRequest) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = api.createEvento(request)
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, successMessage = "Evento creado")
                    load()
                } else {
                    uiState = uiState.copy(isLoading = false, error = ApiErrors.parse(response, "No se pudo crear el evento"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error de conexión")
            }
        }
    }

    fun update(id: Int, request: EventoRequest) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = api.updateEvento(id, request)
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, successMessage = "Evento actualizado")
                    load()
                } else {
                    uiState = uiState.copy(isLoading = false, error = ApiErrors.parse(response, "No se pudo actualizar el evento"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error de conexión")
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            try {
                val response = api.deleteEvento(id)
                if (response.isSuccessful) {
                    uiState = uiState.copy(successMessage = "Evento eliminado")
                    load()
                } else {
                    uiState = uiState.copy(error = ApiErrors.parse(response, "No se pudo eliminar el evento"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Error de conexión")
            }
        }
    }

    /** Publica o despublica un evento sin pasar por el formulario completo. */
    fun togglePublicado(evento: EventoDto) {
        update(
            evento.id,
            EventoRequest(
                titulo = evento.titulo,
                descripcion = evento.descripcion,
                lugar = evento.lugar,
                fecha = evento.fecha,
                hora = evento.hora?.take(5),
                publicado = !evento.publicado,
                // null → el backend conserva la galería actual.
                imagenes = null
            )
        )
    }

    /**
     * Sube una imagen al backend y entrega su URL absoluta. El diálogo usa esa
     * URL dentro del campo `imagenes` del evento.
     */
    fun subirImagen(uri: Uri, onUploaded: (String?) -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(subiendoImagen = true, error = null)
            try {
                val part = partDeUri(uri)
                if (part == null) {
                    uiState = uiState.copy(subiendoImagen = false, error = "No se pudo leer la imagen seleccionada")
                    onUploaded(null)
                    return@launch
                }
                val response = api.subirImagen(part)
                if (response.isSuccessful) {
                    uiState = uiState.copy(subiendoImagen = false)
                    onUploaded(response.body()?.url)
                } else {
                    uiState = uiState.copy(subiendoImagen = false, error = ApiErrors.parse(response, "No se pudo subir la imagen"))
                    onUploaded(null)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(subiendoImagen = false, error = e.message ?: "Error de conexión")
                onUploaded(null)
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, successMessage = null)
    }

    /** Copia el contenido de la Uri a un archivo temporal y arma la parte multipart. */
    private fun partDeUri(uri: Uri): MultipartBody.Part? {
        val context = getApplication<Application>()
        val contentType = context.contentResolver.getType(uri) ?: "image/*"
        val file = File(context.cacheDir, "evento_${System.currentTimeMillis()}.jpg")
        val input = context.contentResolver.openInputStream(uri) ?: return null

        input.use { source ->
            file.outputStream().use { target -> source.copyTo(target) }
        }

        val body = file.asRequestBody(contentType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("imagen", file.name, body)
    }
}