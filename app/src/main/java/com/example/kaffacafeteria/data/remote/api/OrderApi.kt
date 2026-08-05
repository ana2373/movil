package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @GET("pedidos")
    suspend fun getPedidos(
        @Query("per_page") perPage: Int? = null,
        @Query("estado") estado: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): Response<PaginatedResponse<PedidoDto>>

    @GET("pedidos/{id}")
    suspend fun getPedido(@Path("id") id: Int): Response<PedidoDto>

    @POST("pedidos")
    suspend fun createPedido(@Body request: PedidoRequest): Response<PedidoDto>

    @PUT("pedidos/{id}")
    suspend fun updatePedido(
        @Path("id") id: Int,
        @Body request: PedidoUpdateRequest
    ): Response<PedidoDto>

    @DELETE("pedidos/{id}")
    suspend fun deletePedido(@Path("id") id: Int): Response<Any>
}
