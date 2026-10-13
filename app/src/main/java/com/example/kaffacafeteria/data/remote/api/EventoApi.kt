package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.EventoDto
import com.example.kaffacafeteria.data.remote.dto.EventoRequest
import com.example.kaffacafeteria.data.remote.dto.EventoResponse
import com.example.kaffacafeteria.data.remote.dto.ImagenSubidaResponse
import com.example.kaffacafeteria.data.remote.dto.PaginatedResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Eventos de la cafetería.
 *
 * - Público: GET /eventos (sólo publicados, para la landing).
 * - Admin:   GET /eventos/admin, POST/PUT/DELETE y subida de imagen.
 */
interface EventoApi {
    @GET("eventos")
    suspend fun getEventos(
        @Query("per_page") perPage: Int? = null,
        @Query("page") page: Int? = null
    ): Response<PaginatedResponse<EventoDto>>

    @GET("eventos/admin")
    suspend fun getEventosAdmin(
        @Query("per_page") perPage: Int? = null,
        @Query("page") page: Int? = null
    ): Response<PaginatedResponse<EventoDto>>

    @POST("eventos")
    suspend fun createEvento(@Body request: EventoRequest): Response<EventoResponse>

    @PUT("eventos/{id}")
    suspend fun updateEvento(@Path("id") id: Int, @Body request: EventoRequest): Response<EventoResponse>

    @DELETE("eventos/{id}")
    suspend fun deleteEvento(@Path("id") id: Int): Response<Any>

    @Multipart
    @POST("eventos/imagen")
    suspend fun subirImagen(@Part imagen: MultipartBody.Part): Response<ImagenSubidaResponse>
}