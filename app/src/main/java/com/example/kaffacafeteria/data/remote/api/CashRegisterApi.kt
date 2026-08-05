package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CashRegisterApi {
    @GET("cajas")
    suspend fun getCajas(
        @Query("per_page") perPage: Int? = null,
        @Query("estado") estado: String? = null
    ): Response<PaginatedResponse<CajaDto>>

    @GET("cajas/{id}")
    suspend fun getCaja(@Path("id") id: Int): Response<CajaDto>

    @POST("cajas")
    suspend fun createCaja(@Body request: CajaRequest): Response<CajaDto>

    @PUT("cajas/{id}/cerrar")
    suspend fun cerrarCaja(@Path("id") id: Int, @Body request: CajaCierreRequest): Response<CajaDto>

    @GET("movimiento-cajas")
    suspend fun getMovimientos(
        @Query("caja_id") cajaId: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<MovimientoCajaDto>>

    @POST("movimiento-cajas")
    suspend fun createMovimiento(@Body request: MovimientoCajaRequest): Response<MovimientoCajaDto>
}
