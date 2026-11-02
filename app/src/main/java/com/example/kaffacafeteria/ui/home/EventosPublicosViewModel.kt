package com.example.kaffacafeteria.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.EventoDto
import com.example.kaffacafeteria.util.ApiErrors
import kotlinx.coroutines.launch

data class EventosPublicosUiState(
    val eventos: List<EventoDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Eventos publicados que se muestran al cliente en el inicio de la app.
 *
 * Consume el endpoint público `GET /eventos`, que ya filtra los eventos en
 * estado borrador, por lo que sólo llega lo que el administrador publicó.
 */
class EventosPublicosViewModel(application: Application) : AndroidViewModel(application) {
    private val api = (application as KaffaApp).container.eventoApi

    var uiState by mutableStateOf(EventosPublicosUiState())
        private set

    init { load() }

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = api.getEventos(perPage = 20)
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
}
