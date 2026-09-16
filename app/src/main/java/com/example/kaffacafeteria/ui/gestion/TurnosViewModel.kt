package com.example.kaffacafeteria.ui.gestion

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.TurnoDto
import com.example.kaffacafeteria.data.remote.dto.TurnoRequest
import com.example.kaffacafeteria.data.remote.dto.UsuarioFullDto
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response

data class TurnosUiState(
    val turnos: List<TurnoDto> = emptyList(),
    val baristas: List<UsuarioFullDto> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingBaristas: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class TurnosViewModel(application: Application) : AndroidViewModel(application) {
    private val userApi = (application as KaffaApp).container.userApi
    private val turnoApi = (application as KaffaApp).container.turnoApi

    var uiState by mutableStateOf(TurnosUiState())
        private set

    fun loadTurnos() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = turnoApi.getTurnos(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(turnos = response.body()?.data ?: emptyList(), isLoading = false)
                } else {
                    uiState = uiState.copy(isLoading = false, error = serverErrorMessage(response, "Error al cargar turnos"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error de conexión")
            }
        }
    }

    fun loadBaristas() {
        viewModelScope.launch {
            if (uiState.baristas.isNotEmpty()) return@launch
            uiState = uiState.copy(isLoadingBaristas = true)
            try {
                val response = userApi.getUsuarios(perPage = 1000)
                if (response.isSuccessful) {
                    val baristas = (response.body()?.data ?: emptyList())
                        .filter { u -> u.roles?.any { it.nombre.equals("barista", ignoreCase = true) } == true }
                    uiState = uiState.copy(baristas = baristas, isLoadingBaristas = false)
                } else {
                    uiState = uiState.copy(isLoadingBaristas = false)
                }
            } catch (_: Exception) {
                uiState = uiState.copy(isLoadingBaristas = false)
            }
        }
    }

    fun createTurno(fecha: String, tipo: String, baristaIds: List<Int>) {
        if (fecha.isBlank()) {
            uiState = uiState.copy(error = "Selecciona una fecha")
            return
        }
        if (baristaIds.isEmpty()) {
            uiState = uiState.copy(error = "Selecciona al menos un barista")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = turnoApi.createTurno(TurnoRequest(fecha, tipo, baristaIds))
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, successMessage = "Turno guardado")
                    loadTurnos()
                } else {
                    uiState = uiState.copy(isLoading = false, error = serverErrorMessage(response, "Error al guardar turno"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error de conexión")
            }
        }
    }

    fun deleteTurno(id: Int) {
        viewModelScope.launch {
            try {
                val response = turnoApi.deleteTurno(id)
                if (response.isSuccessful) {
                    uiState = uiState.copy(successMessage = "Turno eliminado")
                    loadTurnos()
                } else {
                    uiState = uiState.copy(error = serverErrorMessage(response, "Error al eliminar turno"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Error al eliminar turno")
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, successMessage = null)
    }

    private fun serverErrorMessage(response: Response<*>, fallback: String): String {
        val code = response.code()
        val body = runCatching { response.errorBody()?.string() }.getOrNull()
        val detail = body?.let { raw ->
            runCatching {
                val map = Gson().fromJson(raw, Map::class.java)
                val msg = map["message"] as? String
                val errors = map["errors"] as? Map<*, *>
                val firstError = errors?.values?.firstOrNull()?.let { v -> (v as? List<*>)?.firstOrNull() as? String }
                when {
                    !firstError.isNullOrBlank() -> firstError
                    !msg.isNullOrBlank() && msg != "Server Error" -> msg
                    else -> null
                }?.take(300)
            }.getOrNull()
        }
        return if (!detail.isNullOrBlank()) "$fallback: $detail" else "$fallback (código $code)"
    }
}
