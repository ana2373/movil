package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.LoginRequest
import com.example.kaffacafeteria.data.remote.dto.LoginResponse
<<<<<<< HEAD
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
=======
import com.example.kaffacafeteria.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

<<<<<<< HEAD
    @POST("registro")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @GET("me")
    suspend fun me(): Response<UsuarioDto>

    @PUT("perfil")
    suspend fun updatePerfil(@Body request: UpdateProfileRequest): Response<UsuarioDto>

    @Multipart
    @POST("perfil/foto")
    suspend fun subirFoto(@Part foto: MultipartBody.Part): Response<UsuarioDto>

=======
    @GET("me")
    suspend fun me(): Response<UsuarioDto>

>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
    @POST("logout")
    suspend fun logout(): Response<Any>
}
