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
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
import com.example.kaffacafeteria.domain.model.Promocion
import com.example.kaffacafeteria.domain.model.estaExpirada
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Application.promosDataStore by preferencesDataStore(name = "promociones_store")

data class PromocionesUiState(
    val promociones: List<Promocion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Catálogo actual, para que el administrador elija los productos de la promo. */
    val productos: List<ProductoDto> = emptyList(),
    /** Promoción del momento: la que muestran el Inicio y el Carrito. */
    val promoDestacada: Promocion? = null
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
    private val catalogApi = (application as KaffaApp).container.catalogApi
    private val promoSesion = (application as KaffaApp).container.promoSesion

    /** Id de la última promoción mostrada al abrir la app (para rotar). */
    private val ultimaMostradaKey = stringPreferencesKey("ultima_promo_mostrada")

    var uiState by mutableStateOf(PromocionesUiState())
        private set

    init {
        load()
        loadProductos()
    }

    /**
     * Siguiente promoción a mostrar en el arranque.
     *
     * Rota de forma determinista sobre las promociones activas y vigentes:
     * cada vez que se abre la app se muestra una distinta y, al agotar la
     * lista, vuelve a empezar. El id mostrado se guarda en DataStore para que
     * la rotación sobreviva al cierre de la aplicación.
     */
    suspend fun siguientePromocion(
        activas: List<Promocion> = uiState.promociones.filter { it.activa && !it.estaExpirada() }
    ): Promocion? {
        if (activas.isEmpty()) return null
        return try {
            val prefs = getApplication<Application>().promosDataStore.data.first()
            val ultimoId = prefs[ultimaMostradaKey]?.toIntOrNull()
            val indiceActual = activas.indexOfFirst { it.id == ultimoId }
            // Sin coincidencia (primer arranque o promo borrada) → la primera.
            val siguiente = activas[(indiceActual + 1) % activas.size]
            getApplication<Application>().promosDataStore.edit { it[ultimaMostradaKey] = siguiente.id.toString() }
            siguiente
        } catch (_: Exception) {
            activas.first()
        }
    }

    fun load() {
        android.util.Log.d("PromoDebug", "load() init vm=${hashCode()}")
        viewModelScope.launch {
            android.util.Log.d("PromoDebug", "load() coroutine start vm=${hashCode()}")
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val json = getApplication<Application>().promosDataStore.data.first()[promosKey]
                android.util.Log.d("PromoDebug", "load json=" + json)
                uiState = uiState.copy(promociones = leerPromociones(json), isLoading = false)
                android.util.Log.d("PromoDebug", "load promos=" + uiState.promociones)
            } catch (e: Exception) {
                android.util.Log.d("PromoDebug", "load() ERROR vm=${hashCode()} " + e.javaClass.name + ": " + e.message)
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    /** Catálogo para el selector de productos de la promoción. */
    private fun loadProductos() {
        viewModelScope.launch {
            try {
                val response = catalogApi.getProductos(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(productos = response.body()?.data ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Elige la promoción del momento (la primera vez que se abre la app).
     *
     * La decisión queda guardada en la sesión para que el Inicio y el Carrito
     * muestren la misma promoción. Si todavía no hay promociones cargadas, no
     * rota nada: se reintenta cuando lleguen.
     */
    fun cargarPromoSesion() {
        viewModelScope.launch {
            if (uiState.promoDestacada != null) return@launch
            val existente = promoSesion.promo
            if (existente != null) {
                uiState = uiState.copy(promoDestacada = existente)
                return@launch
            }
            val activas = uiState.promociones.filter { it.activa && !it.estaExpirada() }
            android.util.Log.d("PromoDebug", "cargarPromoSesion vm=${hashCode()} total=" + uiState.promociones.size + " activas=" + activas.size)
            if (activas.isEmpty()) return@launch
            val promo = siguientePromocion(activas)
            android.util.Log.d("PromoDebug", "siguientePromocion=" + promo)
            promoSesion.fijar(promo)
            uiState = uiState.copy(promoDestacada = promo)
        }
    }

    /**
     * Lee las promociones guardadas.
     *
     * Antes de deserializar se completan los campos que se agregaron después
     * (`productoIds`, `descuentoPorcentaje`): Gson no aplica los valores por
     * defecto de Kotlin y dejaría esas listas en null.
     */
    private fun leerPromociones(json: String?): List<Promocion> {
        android.util.Log.d("PromoDebug", "leerPromociones json=" + json)
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = gson.fromJson(json, JsonArray::class.java) ?: return emptyList()
            array.forEach { elemento ->
                if (elemento.isJsonObject) {
                    val obj = elemento.asJsonObject
                    if (!obj.has("productoIds")) obj.add("productoIds", JsonArray())
                    if (!obj.has("descuentoPorcentaje")) obj.add("descuentoPorcentaje", JsonPrimitive(0))
                }
            }
            gson.fromJson(array, object : TypeToken<List<Promocion>>() {}.type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
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
