package com.example.kaffacafeteria.util

import com.example.kaffacafeteria.data.remote.dto.PaginatedResponse
import retrofit2.Response

/**
 * Cuerpo por defecto para respuestas 2xx sin contenido (DELETE/204):
 * evita el NPE de `response.body()!!` sin inventar datos.
 */
@Suppress("UNCHECKED_CAST")
fun <T> emptyBody(): T = Unit as T

/** Error de red/API con el mensaje ya traducido para el usuario. */
class ApiException(message: String) : Exception(message)

/**
 * Recorre todas las páginas de un listado paginado de Laravel.
 *
 * El backend limita `per_page` a 100 (`HasBasicQueries::consultar`), así que
 * pedir 1000 de golpe solo devuelve la primera página y los agregados
 * (KPIs del dashboard, reportes) quedaban calculados sobre datos incompletos.
 */
suspend fun <T> fetchAllPages(
    maxPages: Int = 100,
    fetch: suspend (page: Int) -> Response<PaginatedResponse<T>>
): List<T> {
    val resultado = mutableListOf<T>()
    var page = 1
    while (page <= maxPages) {
        val response = fetch(page)
        if (!response.isSuccessful) {
            // La primera página es obligatoria; si falla, el llamador debe verlo.
            if (page == 1) throw ApiException(ApiErrors.parse(response, "Error al cargar los datos"))
            break
        }
        val body = response.body() ?: break
        resultado += body.data
        if (body.data.isEmpty() || page >= body.meta.lastPage) break
        page++
    }
    return resultado
}
