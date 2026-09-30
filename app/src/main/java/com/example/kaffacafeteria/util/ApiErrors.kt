package com.example.kaffacafeteria.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

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
 */
object ApiErrors {

    private val gson = Gson()

    fun parse(response: Response<*>?, fallback: String = "Ocurrió un error"): String {
        val body = runCatching { response?.errorBody()?.string() }.getOrNull()
        if (body.isNullOrBlank()) return fallback

        return try {
            val json = gson.fromJson(body, JsonObject::class.java) ?: return fallback

            // Validación: el primer mensaje de "errors" es el más útil para el usuario.
            json.getAsJsonObject("errors")?.entrySet()?.firstOrNull()?.value
                ?.let { array ->
                    val mensaje = runCatching { array.asJsonArray[0].asString }.getOrNull()
                    if (!mensaje.isNullOrBlank()) return mensaje
                }

            when {
                json.has("error") && !json.get("error").isJsonNull -> json.get("error").asString
                json.has("message") && json.get("message").isJsonPrimitive ->
                    json.get("message").asString
                json.has("message") && json.get("message").isJsonObject ->
                    json.getAsJsonObject("message").entrySet().firstOrNull()?.value?.asString
                        ?: fallback
                else -> fallback
            }
        } catch (e: Exception) {
            fallback
        }
    }

    /** Extrae el código de regla de negocio (`CAJA_CERRADA`, `EMAIL_NOT_VERIFIED`...). */
    fun code(response: Response<*>?): String? {
        val body = runCatching { response?.errorBody()?.string() }.getOrNull() ?: return null
        return try {
            gson.fromJson(body, JsonObject::class.java)
                ?.get("code")
                ?.takeIf { !it.isJsonNull }
                ?.asString
        } catch (e: Exception) {
            null
        }
    }
}
