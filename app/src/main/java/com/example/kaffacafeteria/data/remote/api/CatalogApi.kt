package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CatalogApi {
    // Categorias
    @GET("categorias")
    suspend fun getCategorias(
        @Query("per_page") perPage: Int? = null,
        @Query("nombre") nombre: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): Response<PaginatedResponse<CategoriaDto>>

    @GET("categorias/{id}")
    suspend fun getCategoria(@Path("id") id: Int): Response<CategoriaDto>

    @POST("categorias")
    suspend fun createCategoria(@Body request: CategoriaRequest): Response<CategoriaDto>

    @PUT("categorias/{id}")
    suspend fun updateCategoria(@Path("id") id: Int, @Body request: CategoriaRequest): Response<CategoriaDto>

    @DELETE("categorias/{id}")
    suspend fun deleteCategoria(@Path("id") id: Int): Response<Any>

    // Productos
    @GET("productos")
    suspend fun getProductos(
        @Query("per_page") perPage: Int? = null,
        @Query("nombre") nombre: String? = null,
        @Query("categoria_id") categoriaId: Int? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): Response<PaginatedResponse<ProductoDto>>

    @GET("productos/{id}")
    suspend fun getProducto(@Path("id") id: Int): Response<ProductoDto>

    @POST("productos")
    suspend fun createProducto(@Body request: ProductoRequest): Response<ProductoDto>

    @PUT("productos/{id}")
    suspend fun updateProducto(@Path("id") id: Int, @Body request: ProductoRequest): Response<ProductoDto>

    @DELETE("productos/{id}")
    suspend fun deleteProducto(@Path("id") id: Int): Response<Any>

    // Insumos
    @GET("insumos")
    suspend fun getInsumos(
        @Query("per_page") perPage: Int? = null,
        @Query("nombre") nombre: String? = null
    ): Response<PaginatedResponse<InsumoDto>>

    @GET("insumos/{id}")
    suspend fun getInsumo(@Path("id") id: Int): Response<InsumoDto>

    @POST("insumos")
    suspend fun createInsumo(@Body request: InsumoRequest): Response<InsumoDto>

    @PUT("insumos/{id}")
    suspend fun updateInsumo(@Path("id") id: Int, @Body request: InsumoRequest): Response<InsumoDto>

    @DELETE("insumos/{id}")
    suspend fun deleteInsumo(@Path("id") id: Int): Response<Any>

    // Recetas (insumos de un producto)
    @POST("productos/{id}/insumos")
    suspend fun addInsumoToProducto(@Path("id") productoId: Int, @Body request: RecetaRequest): Response<RecetaDto>

    @DELETE("recetas/{id}")
    suspend fun deleteReceta(@Path("id") id: Int): Response<Any>

    @GET("productos/{id}/insumos")
    suspend fun getInsumosProducto(@Path("id") productoId: Int): Response<PaginatedResponse<RecetaDto>>

    // Medios de Pago
    @GET("medios-pago")
    suspend fun getMediosPago(
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<MedioPagoDto>>

    @POST("medios-pago")
    suspend fun createMedioPago(@Body request: MedioPagoRequest): Response<MedioPagoDto>

    @PUT("medios-pago/{id}")
    suspend fun updateMedioPago(@Path("id") id: Int, @Body request: MedioPagoRequest): Response<MedioPagoDto>

    @DELETE("medios-pago/{id}")
    suspend fun deleteMedioPago(@Path("id") id: Int): Response<Any>
}
