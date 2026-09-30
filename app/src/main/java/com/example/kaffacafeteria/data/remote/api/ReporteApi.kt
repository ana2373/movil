package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.PaginatedResponse
import com.example.kaffacafeteria.data.remote.dto.ReporteDto
import com.example.kaffacafeteria.data.remote.dto.ReporteRequest
import com.example.kaffacafeteria.data.remote.dto.ReporteResponse
import com.example.kaffacafeteria.data.remote.dto.ReporteUpdateRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Reportes/novedades (barista ↔ admin).
 *
 * - Barista: POST /reportes
 * - Admin:   GET/PUT/DELETE /reportes
 */
interface ReporteApi {
    @GET("reportes")
    suspend fun getReportes(
        @Query("per_page") perPage: Int? = null,
        @Query("page") page: Int? = null
    ): Response<PaginatedResponse<ReporteDto>>

    @GET("reportes/{id}")
    suspend fun getReporte(@Path("id") id: Int): Response<ReporteResponse>

    @POST("reportes")
    suspend fun createReporte(@Body request: ReporteRequest): Response<ReporteResponse>

    @PUT("reportes/{id}")
    suspend fun updateReporte(@Path("id") id: Int, @Body request: ReporteUpdateRequest): Response<ReporteResponse>

    @DELETE("reportes/{id}")
    suspend fun deleteReporte(@Path("id") id: Int): Response<Any>
}
