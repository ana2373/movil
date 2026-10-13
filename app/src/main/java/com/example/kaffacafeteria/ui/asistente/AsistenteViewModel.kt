package com.example.kaffacafeteria.ui.asistente

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.AsistenteChatRequest
import com.example.kaffacafeteria.data.remote.dto.AsistenteMensajeDto
import com.example.kaffacafeteria.util.ApiErrors
import kotlinx.coroutines.launch

data class AsistenteUiState(
    val mensajes: List<AsistenteMensajeDto> = emptyList(),
    val cargandoHistorial: Boolean = false,
    val enviando: Boolean = false,
    val error: String? = null
)

/**
 * Chat con el Asistente KAFFA (IA local). El historial y las respuestas viven
 * en el backend; aquí sólo se orquesta la conversación y se muestra en orden
 * cronológico.
 */
class AsistenteViewModel(application: Application) : AndroidViewModel(application) {
    private val api = (application as KaffaApp).container.asistenteApi

    var uiState by mutableStateOf(AsistenteUiState())
        private set

    private var idTemporal = -1

    /** Identifica la sesión (login) cuyo historial se está mostrando. */
    private var sesionActual: Int? = null

    /**
     * Sincroniza la conversación con la sesión actual. El ViewModel vive a nivel
     * de Activity, por lo que si cambia el usuario (o su rol) hay que descartar
     * los mensajes anteriores y recargar los del usuario correcto; de lo
     * contrario se mezclarían las conversaciones de cliente, barista y admin.
     */
    fun sincronizarSesion(sessionKey: Int) {
        if (sesionActual == sessionKey) return
        sesionActual = sessionKey
        idTemporal = -1
        uiState = AsistenteUiState()
        cargarHistorial()
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            uiState = uiState.copy(cargandoHistorial = true, error = null)
            try {
                val response = api.getMensajes(perPage = 50)
                if (response.isSuccessful) {
                    // El backend devuelve del más reciente al más antiguo.
                    val ordenados = response.body()?.data?.reversed() ?: emptyList()
                    uiState = uiState.copy(mensajes = ordenados, cargandoHistorial = false)
                } else {
                    uiState = uiState.copy(cargandoHistorial = false, error = ApiErrors.parse(response, "No se pudo cargar la conversación"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(cargandoHistorial = false, error = e.message ?: "Error de conexión")
            }
        }
    }

    fun enviar(texto: String) {
        val mensaje = texto.trim()
        if (mensaje.isEmpty() || uiState.enviando) return

        val local = AsistenteMensajeDto(id = idTemporal--, rol = "user", contenido = mensaje)
        uiState = uiState.copy(mensajes = uiState.mensajes + local, enviando = true, error = null)

        viewModelScope.launch {
            try {
                val response = api.chat(AsistenteChatRequest(mensaje))
                if (response.isSuccessful && response.body() != null) {
                    val cuerpo = response.body()!!
                    val respuesta = AsistenteMensajeDto(
                        id = cuerpo.mensajeId,
                        rol = "assistant",
                        contenido = cuerpo.respuesta,
                        createdAt = cuerpo.createdAt
                    )
                    uiState = uiState.copy(mensajes = uiState.mensajes + respuesta, enviando = false)
                } else {
                    uiState = uiState.copy(enviando = false, error = ApiErrors.parse(response, "El asistente no pudo responder"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(enviando = false, error = e.message ?: "Error de conexión")
            }
        }
    }

    fun limpiarConversacion() {
        viewModelScope.launch {
            try {
                api.limpiar()
                uiState = uiState.copy(mensajes = emptyList())
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }
}