package com.example.kaffacafeteria.util

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()

    /**
     * @param mensaje   texto listo para mostrar al usuario
     * @param code      código HTTP de la respuesta
     * @param codigoApi código de regla de negocio del backend
     *                 (EMAIL_NOT_VERIFIED, CAJA_CERRADA, ...), útil para
     *                 reaccionar a casos concretos sin analizar el mensaje.
     */
    data class Error(
        val message: String,
        val code: Int? = null,
        val codigoApi: String? = null
    ) : Resource<Nothing>()
}
