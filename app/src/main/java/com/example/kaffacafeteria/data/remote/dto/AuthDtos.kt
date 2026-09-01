package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val correo: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val password: String
)

data class UpdateProfileRequest(
    val nombre: String? = null,
    val correo: String? = null,
    val password: String? = null
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val usuario: UsuarioDto
)

data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val activo: Boolean,
    val roles: List<RolDto>,
    val foto: String? = null
)

data class RolDto(
    val id: Int,
    val nombre: String
)

data class ApiError(
    val message: String?,
    val errors: Map<String, List<String>>?
)
