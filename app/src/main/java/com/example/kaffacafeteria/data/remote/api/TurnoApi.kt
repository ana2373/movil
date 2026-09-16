package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TurnoApi {
    @GET("turnos")
    suspend fun getTurnos(
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<TurnoDto>>

    @POST("turnos")
    suspend fun createTurno(@Body request: TurnoRequest): Response<TurnoResponse>

    @DELETE("turnos/{id}")
    suspend fun deleteTurno(@Path("id") id: Int): Response<Any>
}
