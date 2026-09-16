package com.example.kaffacafeteria.ui.admin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Application.promosDataStore by preferencesDataStore(name = "promociones_store")

data class Promocion(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tipo: String,
    val colorIndex: Int,
    val imagenUri: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val expiracionMillis: Long? = null,
    val activa: Boolean = true
)

fun Promocion.fechaLimiteMillis(): Long? {
    expiracionMillis?.let { return it }
    val fin = fechaFin ?: return null
    val parts = fin.split("-").mapNotNull { it.toIntOrNull() }
    if (parts.size != 3) return null
    return runCatching {
        Calendar.getInstance().apply {
            clear()
            set(parts[0], parts[1] - 1, parts[2], 23, 59, 59)
        }.timeInMillis
    }.getOrNull()
}

fun Promocion.estaExpirada(now: Long = System.currentTimeMillis()): Boolean {
    val limite = fechaLimiteMillis() ?: return false
    return now >= limite
}

fun formatExpiracion(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

data class PromocionesUiState(
    val promociones: List<Promocion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PromocionesViewModel(application: Application) : AndroidViewModel(application) {
    private val gson = Gson()
    private val promosKey = stringPreferencesKey("promociones")

    var uiState by mutableStateOf(PromocionesUiState())
        private set

    init { load() }

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val json = getApplication<Application>().promosDataStore.data.first()[promosKey]
                val list: List<Promocion> = if (json.isNullOrBlank()) {
                    emptyList()
                } else {
                    try {
                        gson.fromJson(json, object : TypeToken<List<Promocion>>() {}.type)
                    } catch (_: Exception) { emptyList() }
                }
                uiState = uiState.copy(promociones = list, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun create(promo: Promocion) {
        viewModelScope.launch {
            val nextId = (uiState.promociones.maxOfOrNull { it.id } ?: 0) + 1
            val updated = uiState.promociones + promo.copy(id = nextId)
            persist(updated)
        }
    }

    fun toggleActiva(promo: Promocion) {
        viewModelScope.launch {
            val updated = uiState.promociones.map { if (it.id == promo.id) it.copy(activa = !it.activa) else it }
            persist(updated)
        }
    }

    fun delete(promo: Promocion) {
        viewModelScope.launch {
            val updated = uiState.promociones.filter { it.id != promo.id }
            persist(updated)
        }
    }

    private suspend fun persist(list: List<Promocion>) {
        try {
            val json = gson.toJson(list)
            getApplication<Application>().promosDataStore.edit { prefs -> prefs[promosKey] = json }
            uiState = uiState.copy(promociones = list)
        } catch (e: Exception) {
            uiState = uiState.copy(error = e.message)
        }
    }
}