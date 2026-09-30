package com.example.kaffacafeteria.data.repository

import com.example.kaffacafeteria.data.local.TokenManager
import com.example.kaffacafeteria.data.remote.api.AuthApi
import com.example.kaffacafeteria.data.remote.dto.ForgotPasswordRequest
import com.example.kaffacafeteria.data.remote.dto.LoginRequest
import com.example.kaffacafeteria.data.remote.dto.RegisterRequest
import com.example.kaffacafeteria.data.remote.dto.ResetPasswordRequest
import com.example.kaffacafeteria.data.remote.dto.UpdateProfileRequest
import com.example.kaffacafeteria.domain.model.Role
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.domain.repository.AuthRepository
import com.example.kaffacafeteria.domain.repository.RegistroExitoso
import com.example.kaffacafeteria.util.ApiErrors
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(correo: String, password: String): Resource<User> {
        return try {
            val response = authApi.login(LoginRequest(correo, password))
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                tokenManager.saveToken(loginResponse.accessToken)
                Resource.Success(loginResponse.usuario.toDomain())
            } else {
                Resource.Error(
                    ApiErrors.parse(response, "No se pudo iniciar sesión"),
                    response.code(),
                    ApiErrors.code(response)
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    /**
     * Registra la cuenta.
     *
     * El backend responde 201 con `{message, correo}` y NO entrega token, por
     * lo que la app no guarda sesión: muestra el aviso de "revisa tu correo"
     * y ofrece reenviar el enlace de verificación.
     */
    override suspend fun register(
        nombre: String,
        correo: String,
        password: String
    ): Resource<RegistroExitoso> {
        return try {
            val response = authApi.register(RegisterRequest(nombre, correo, password))
            if (response.isSuccessful) {
                val body = response.body()
                Resource.Success(
                    RegistroExitoso(
                        mensaje = body?.message
                            ?: "Registro exitoso. Revisa tu correo electrónico para verificar tu cuenta.",
                        correo = body?.correo ?: correo
                    )
                )
            } else {
                Resource.Error(
                    ApiErrors.parse(response, "No se pudo completar el registro"),
                    response.code()
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun reenviarVerificacion(correo: String): Resource<String> {
        return try {
            val response = authApi.reenviarVerificacion(ForgotPasswordRequest(correo))
            if (response.isSuccessful) {
                Resource.Success(
                    response.body()?.message
                        ?: "Si el correo está registrado, recibirás un nuevo enlace de verificación."
                )
            } else {
                Resource.Error(ApiErrors.parse(response, "No se pudo reenviar el correo"), response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun forgotPassword(correo: String): Resource<String> {
        return try {
            val response = authApi.forgotPassword(ForgotPasswordRequest(correo))
            if (response.isSuccessful) {
                Resource.Success(
                    response.body()?.message
                        ?: "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña."
                )
            } else {
                Resource.Error(ApiErrors.parse(response, "No se pudo procesar la solicitud"), response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun resetPassword(
        token: String,
        correo: String,
        password: String
    ): Resource<String> {
        return try {
            val response = authApi.resetPassword(
                ResetPasswordRequest(
                    token = token,
                    correo = correo,
                    password = password,
                    passwordConfirmation = password
                )
            )
            if (response.isSuccessful) {
                Resource.Success(
                    response.body()?.message ?: "Tu contraseña se restableció correctamente."
                )
            } else {
                Resource.Error(ApiErrors.parse(response, "No se pudo restablecer la contraseña"), response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun updateProfile(nombre: String, correo: String, password: String?): Resource<User> {
        return try {
            val response = authApi.updatePerfil(UpdateProfileRequest(nombre = nombre, correo = correo, password = password))
            if (response.isSuccessful) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error(
                    ApiErrors.parse(response, "No se pudo actualizar el perfil"),
                    response.code()
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun subirFoto(fotoPath: String): Resource<User> {
        return try {
            val file = File(fotoPath)
            val mediaType = "image/*".toMediaType()
            val requestBody = file.asRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("foto", file.name, requestBody)
            val response = authApi.subirFoto(part)
            if (response.isSuccessful) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error(ApiErrors.parse(response, "No se pudo subir la foto"), response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun getMe(): Resource<User> {
        return try {
            val response = authApi.me()
            if (response.isSuccessful) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error("No autorizado", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            authApi.logout()
            tokenManager.deleteToken()
            Resource.Success(Unit)
        } catch (e: Exception) {
            // El cierre de sesión local siempre debe completarse, incluso sin red.
            tokenManager.deleteToken()
            Resource.Success(Unit)
        }
    }

    override suspend fun getToken(): String? = tokenManager.getToken()

    override suspend fun isLoggedIn(): Boolean = tokenManager.getToken() != null

    override fun getTokenFlow(): Flow<String?> = tokenManager.tokenFlow
}

/** Traduce el DTO de la API al modelo de dominio. */
private fun com.example.kaffacafeteria.data.remote.dto.UsuarioDto.toDomain(): User = User(
    id = id,
    nombre = nombre,
    correo = correo,
    activo = activo,
    roles = roles.map { Role(it.id, it.nombre) },
    foto = foto
)
