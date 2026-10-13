package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.AsistenteChatRequest
import com.example.kaffacafeteria.data.remote.dto.AsistenteChatResponse
import com.example.kaffacafeteria.data.remote.dto.AsistenteMensajeDto
import com.example.kaffacafeteria.data.remote.dto.PaginatedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Asistente IA de KAFFA (IA local, sin API externa).
 *
 * Disponible para cualquier usuario autenticado; el backend adapta las
 * respuestas al rol (analítica para staff, ayuda para el cliente).
 */
interface AsistenteApi {
    @GET("asistente/mensajes")
    suspend fun getMensajes(
        @Query("per_page") perPage: Int? = null,
        @Query("page") page: Int? = null
    ): Response<PaginatedResponse<AsistenteMensajeDto>>

    @POST("asistente/chat")
    suspend fun chat(@Body request: AsistenteChatRequest): Response<AsistenteChatResponse>

    @DELETE("asistente/mensajes")
    suspend fun limpiar(): Response<Any>
}