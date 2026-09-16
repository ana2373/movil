package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {
    // Compras
    @GET("compras")
    suspend fun getCompras(@Query("per_page") perPage: Int? = null): Response<PaginatedResponse<CompraDto>>

    @POST("compras")
    suspend fun createCompra(@Body request: CompraDto): Response<CompraDto>

    // Gastos
    @GET("gastos")
    suspend fun getGastos(@Query("per_page") perPage: Int? = null): Response<PaginatedResponse<GastoDto>>

    @POST("gastos")
    suspend fun createGasto(@Body request: GastoRequest): Response<GastoDto>

    // Mermas
    @GET("mermas")
    suspend fun getMermas(@Query("per_page") perPage: Int? = null): Response<PaginatedResponse<MermaDto>>

    @POST("mermas")
    suspend fun createMerma(@Body request: MermaDto): Response<MermaDto>

    // Proveedores
    @GET("proveedores")
    suspend fun getProveedores(
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<ProveedorDto>>

    @POST("proveedores")
    suspend fun createProveedor(@Body request: ProveedorDto): Response<ProveedorDto>

    @PUT("proveedores/{id}")
    suspend fun updateProveedor(@Path("id") id: Int, @Body request: ProveedorDto): Response<ProveedorDto>

    @DELETE("proveedores/{id}")
    suspend fun deleteProveedor(@Path("id") id: Int): Response<Any>
}
