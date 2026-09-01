package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UsuarioFullDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val activo: Boolean,
    val roles: List<RolDto>?,
    val created_at: String?,
    val updated_at: String?
)

data class UsuarioCreateRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    val roles: List<Int>? = null
)

data class UsuarioUpdateRequest(
    val nombre: String? = null,
    val correo: String? = null,
    val password: String? = null,
    val activo: Boolean? = null,
    val roles: List<Int>? = null
)

data class RolFullDto(
    val id: Int,
    val nombre: String,
    val usuarios_count: Int?
)
