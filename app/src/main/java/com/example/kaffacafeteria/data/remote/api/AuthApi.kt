package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.LoginRequest
import com.example.kaffacafeteria.data.remote.dto.LoginResponse
import com.example.kaffacafeteria.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("me")
    suspend fun me(): Response<UsuarioDto>

    @POST("logout")
    suspend fun logout(): Response<Any>
}
