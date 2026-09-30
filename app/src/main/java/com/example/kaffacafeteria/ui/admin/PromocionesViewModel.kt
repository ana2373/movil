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

/**
 * Promoción del splash.
 *
 * Vigencia: se guarda un único instante (`expiracionMillis`) con día y hora.
 * Si es `null` la promoción no caduca y queda activa hasta que el
 * administrador la desactive o la elimine.
 */
data class Promocion(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tipo: String,
    val colorIndex: Int,
    val imagenUri: String? = null,
    val expiracionMillis: Long? = null,
    val activa: Boolean = true
)

/** Instante en que deja de mostrarse la promoción, o null si no caduca. */
fun Promocion.fechaLimiteMillis(): Long? = expiracionMillis

fun Promocion.estaExpirada(now: Long = System.currentTimeMillis()): Boolean {
    val limite = fechaLimiteMillis() ?: return false
    return now >= limite
}

/** Vigencia en formato 12 h (am/pm) para la lista: "25/06/2025, 03:30 p. m." */
fun formatExpiracion(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale("es", "CO")).format(Date(millis))

/** Combina el día elegido con la hora (formato 24 h) en un único instante. */
fun buildExpiracionMillis(diaMillis: Long, hora: Int, minuto: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = diaMillis
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

data class PromocionesUiState(
    val promociones: List<Promocion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Almacén local de promociones.
 *
 * Se guardan en DataStore (no hay endpoint de promociones en el backend), por
 * eso el CRUD es local: crear, editar, activar/desactivar y eliminar.
 */
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
                        gson.fromJson(json, object : TypeToken<List<Promocion>>() {}.type) ?: emptyList()
                    } catch (_: Exception) { emptyList() }
                }
                uiState = uiState.copy(promociones = list, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    /** Alta de una promoción nueva con id correlativo. */
    fun create(promo: Promocion) {
        viewModelScope.launch {
            val nextId = (uiState.promociones.maxOfOrNull { it.id } ?: 0) + 1
            persist(uiState.promociones + promo.copy(id = nextId))
        }
    }

    /**
     * Edición de una promoción existente.
     *
     * Se conserva el id original para no romper la referencia de la imagen ni
     * el historial; sólo cambian los campos editables.
     */
    fun update(promo: Promocion) {
        viewModelScope.launch {
            val updated = uiState.promociones.map {
                if (it.id == promo.id) promo.copy(id = it.id) else it
            }
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
            persist(uiState.promociones.filterNot { it.id == promo.id })
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
