package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.LoginRequest
import com.example.kaffacafeteria.data.remote.dto.LoginResponse
import com.example.kaffacafeteria.data.remote.dto.RegisterRequest
import com.example.kaffacafeteria.data.remote.dto.UpdateProfileRequest
import com.example.kaffacafeteria.data.remote.dto.UsuarioDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("registro")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @GET("me")
    suspend fun me(): Response<UsuarioDto>

    @PUT("perfil")
    suspend fun updatePerfil(@Body request: UpdateProfileRequest): Response<UsuarioDto>

    @Multipart
    @POST("perfil/foto")
    suspend fun subirFoto(@Part foto: MultipartBody.Part): Response<UsuarioDto>

    @POST("logout")
    suspend fun logout(): Response<Any>
}
