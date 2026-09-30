package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.ForgotPasswordRequest
import com.example.kaffacafeteria.data.remote.dto.LoginRequest
import com.example.kaffacafeteria.data.remote.dto.LoginResponse
import com.example.kaffacafeteria.data.remote.dto.MessageResponse
import com.example.kaffacafeteria.data.remote.dto.RegisterRequest
import com.example.kaffacafeteria.data.remote.dto.RegisterResponse
import com.example.kaffacafeteria.data.remote.dto.ResetPasswordRequest
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

    /**
     * Crea la cuenta y envía el correo de verificación.
     * No devuelve token: el usuario debe verificar su correo antes de entrar.
     */
    @POST("registro")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("me")
    suspend fun me(): Response<UsuarioDto>

    @PUT("perfil")
    suspend fun updatePerfil(@Body request: UpdateProfileRequest): Response<UsuarioDto>

    @Multipart
    @POST("perfil/foto")
    suspend fun subirFoto(@Part foto: MultipartBody.Part): Response<UsuarioDto>

    @POST("logout")
    suspend fun logout(): Response<Any>

    /**
     * Reenvía el correo de verificación.
     * El backend responde igual exista o no el usuario, para no filtrar
     * qué correos están registrados.
     */
    @POST("email/verification-notification")
    suspend fun reenviarVerificacion(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>
}
