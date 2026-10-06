package com.example.kaffacafeteria.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response
import java.util.WeakHashMap

/**
 * Lectura uniforme de errores de la API Laravel.
 *
 * El backend responde con distintos formatos según el origen del error:
 *  - `{"message": "..."}`                     → mensaje simple (422 del login).
 *  - `{"message": "...", "code": "CAJA_CERRADA"}` → error de reglas de negocio.
 *  - `{"message": {...}, "errors": {...}}`   → validación de formulario (422).
 *
 * Esta función unifica los tres casos para que ninguna pantalla muestre
 * "HTTP 422" o una traza técnica al usuario final.
 *
 * IMPORTANTE: Retrofit amortigua el cuerpo del error en un buffer de Okio que
 * **solo puede leerse una vez**; la segunda lectura devuelve una cadena vacía.
 * Por eso el resultado se memoriza por respuesta: llamar `parse()` y luego
 * `code()` (como hace AuthRepositoryImpl) devuelve el código correcto.
 */
object ApiErrors {

    private val gson = Gson()

    data class Info(val message: String, val codigo: String?)

    private val cache = java.util.Collections.synchronizedMap(WeakHashMap<Response<*>, Pair<String?, String?>>())

    fun parse(response: Response<*>?, fallback: String = "Ocurrió un error"): String =
        read(response, fallback).message

    /** Extrae el código de regla de negocio (`CAJA_CERRADA`, `EMAIL_NOT_VERIFIED`...). */
    fun code(response: Response<*>?): String? = read(response, "").codigo

    fun read(response: Response<*>?, fallback: String = "Ocurrió un error"): Info {
        response ?: return Info(fallback, null)
        val (mensaje, codigo) = cache.getOrPut(response) { parseOnce(response) }
        return Info(mensaje ?: fallback, codigo)
    }

    private fun parseOnce(response: Response<*>): Pair<String?, String?> {
        val body = runCatching { response.errorBody()?.string() }.getOrNull()
        if (body.isNullOrBlank()) return null to null

        return try {
            val json = gson.fromJson(body, JsonObject::class.java) ?: return null to null
            parseJson(json) to codigoDe(json)
        } catch (e: Exception) {
            null to null
        }
    }

    /** Primer mensaje útil para el usuario: validaciones, `error`, `message`. */
    private fun parseJson(json: JsonObject): String? {
        // Validación: el primer mensaje de "errors" es el más útil para el usuario.
        json.getAsJsonObject("errors")?.entrySet()?.firstOrNull()?.value
            ?.let { array ->
                val mensaje = runCatching { array.asJsonArray[0].asString }.getOrNull()
                if (!mensaje.isNullOrBlank()) return mensaje
            }

        return when {
            json.has("error") && !json.get("error").isJsonNull -> json.get("error").asString
            json.has("message") && json.get("message").isJsonPrimitive ->
                json.get("message").asString
            json.has("message") && json.get("message").isJsonObject ->
                json.getAsJsonObject("message").entrySet().firstOrNull()?.value?.asString
            else -> null
        }
    }

    private fun codigoDe(json: JsonObject): String? =
        runCatching {
            json.get("code")?.takeIf { !it.isJsonNull }?.asString
        }.getOrNull()
}
