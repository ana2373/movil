package com.example.kaffacafeteria.domain.repository

import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.util.Resource

/**
 * Resultado de un registro exitoso.
 *
 * El backend exige verificar el correo antes del primer inicio de sesión,
 * así que el registro NO crea sesión: [correoNotificado] indica el correo al
 * que se envió el enlace de verificación.
 */
data class RegistroExitoso(
    val mensaje: String,
    val correo: String?
)

interface AuthRepository {
    suspend fun login(correo: String, password: String): Resource<User>

    /** Registra la cuenta; requiere verificar el correo antes de entrar. */
    suspend fun register(nombre: String, correo: String, password: String): Resource<RegistroExitoso>

    suspend fun updateProfile(nombre: String, correo: String, password: String?): Resource<User>
    suspend fun subirFoto(fotoPath: String): Resource<User>
    suspend fun getMe(): Resource<User>
    suspend fun logout(): Resource<Unit>
    suspend fun getToken(): String?
    suspend fun isLoggedIn(): Boolean
    fun getTokenFlow(): kotlinx.coroutines.flow.Flow<String?>

    /** Reenvía el correo de verificación de la cuenta. */
    suspend fun reenviarVerificacion(correo: String): Resource<String>

    /** Solicita el enlace de restablecimiento; la respuesta nunca revela si el correo existe. */
    suspend fun forgotPassword(correo: String): Resource<String>

    /** Guarda la nueva contraseña usando el token recibido por correo. */
    suspend fun resetPassword(token: String, correo: String, password: String): Resource<String>
}
