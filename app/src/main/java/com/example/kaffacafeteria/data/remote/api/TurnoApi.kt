package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TurnoApi {
    @GET("turnos")
    suspend fun getTurnos(
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<TurnoDto>>

    /**
     * Estado operativo del barista autenticado (`GET /turno-activo`).
     *
     * Indica si tiene turno vigente y si hay caja abierta. Se consulta al
     * entrar al panel para poder bloquear las acciones con un mensaje claro
     * en vez de dejar que el usuario descubra el error al registrar algo.
     */
    @GET("turno-activo")
    suspend fun getTurnoActivo(): Response<TurnoActivoDto>

    @POST("turnos")
    suspend fun createTurno(@Body request: TurnoRequest): Response<TurnoResponse>

    @PUT("turnos/{id}")
    suspend fun updateTurno(@Path("id") id: Int, @Body request: TurnoUpdateRequest): Response<TurnoResponse>

    @DELETE("turnos/{id}")
    suspend fun deleteTurno(@Path("id") id: Int): Response<Any>
}
