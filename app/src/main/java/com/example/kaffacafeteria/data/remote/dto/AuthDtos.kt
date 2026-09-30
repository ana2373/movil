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

/**
 * Respuesta real de `POST /registro`.
 *
 * El backend NO devuelve token: crea la cuenta y exige verificar el correo.
 * Antes la appODY el mismo DTO del login, lo que provocaba un error al
 * deserializar y dejaba al usuario sin sesión.
 */
data class RegisterResponse(
    val message: String? = null,
    val correo: String? = null
)

/** Cuerpo de `POST /forgot-password`. */
data class ForgotPasswordRequest(val correo: String)

/** Cuerpo de `POST /reset-password`. */
data class ResetPasswordRequest(
    val token: String,
    val correo: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String
)

/** Respuesta uniforme de recuperación: `{"message": "..."}`. */
data class MessageResponse(val message: String? = null)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String? = null,
    val usuario: UsuarioDto,
    /**
     * Estado operativo del barista en el momento del login.
     * El backend ya lo devuelve para evitar una segunda llamada.
     */
    @SerializedName("turno_activo") val turnoActivo: Boolean? = null,
    @SerializedName("caja_abierta") val cajaAbierta: Boolean? = null,
    @SerializedName("puede_operar") val puedeOperar: Boolean? = null,
    @SerializedName("motivo_bloqueo") val motivoBloqueo: String? = null
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
