package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("usuarios")
    suspend fun getUsuarios(
        @Query("per_page") perPage: Int? = null,
        @Query("nombre") nombre: String? = null
    ): Response<PaginatedResponse<UsuarioFullDto>>

    @GET("usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Int): Response<UsuarioFullDto>

    @POST("usuarios")
    suspend fun createUsuario(@Body request: UsuarioCreateRequest): Response<UsuarioFullDto>

    @PUT("usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: Int,
        @Body request: UsuarioUpdateRequest
    ): Response<UsuarioFullDto>

    @DELETE("usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Int): Response<Any>

    @GET("roles")
    suspend fun getRoles(): Response<List<RolFullDto>>
}
