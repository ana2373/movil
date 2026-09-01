package com.example.kaffacafeteria.data.repository

import com.example.kaffacafeteria.data.local.TokenManager
import com.example.kaffacafeteria.data.remote.api.AuthApi
import com.example.kaffacafeteria.data.remote.dto.LoginRequest
import com.example.kaffacafeteria.data.remote.dto.RegisterRequest
import com.example.kaffacafeteria.data.remote.dto.UpdateProfileRequest
import com.example.kaffacafeteria.domain.model.Role
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.domain.repository.AuthRepository
import com.example.kaffacafeteria.util.Resource
import com.google.gson.Gson
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
                val user = loginResponse.usuario
                Resource.Success(
                    User(
                        id = user.id,
                        nombre = user.nombre,
                        correo = user.correo,
                        activo = user.activo,
                        roles = user.roles.map { Role(it.id, it.nombre) },
                        foto = user.foto
                    )
                )
            } else {
                val errorBody = response.errorBody()?.string()
                val msg = try {
                    Gson().fromJson(errorBody, Map::class.java)["message"] as? String
                } catch (e: Exception) { null }
                Resource.Error(msg ?: "Error al iniciar sesión", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun register(nombre: String, correo: String, password: String): Resource<User> {
        return try {
            val response = authApi.register(RegisterRequest(nombre, correo, password))
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                tokenManager.saveToken(loginResponse.accessToken)
                val user = loginResponse.usuario
                Resource.Success(
                    User(
                        id = user.id,
                        nombre = user.nombre,
                        correo = user.correo,
                        activo = user.activo,
                        roles = user.roles.map { Role(it.id, it.nombre) },
                        foto = user.foto
                    )
                )
            } else {
                val errorBody = response.errorBody()?.string()
                val msg = try {
                    Gson().fromJson(errorBody, Map::class.java)["message"] as? String
                } catch (e: Exception) { null }
                Resource.Error(msg ?: "Error al registrarse", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun updateProfile(nombre: String, correo: String, password: String?): Resource<User> {
        return try {
            val response = authApi.updatePerfil(UpdateProfileRequest(nombre = nombre, correo = correo, password = password))
            if (response.isSuccessful) {
                val user = response.body()!!
                Resource.Success(
                    User(
                        id = user.id,
                        nombre = user.nombre,
                        correo = user.correo,
                        activo = user.activo,
                        roles = user.roles.map { Role(it.id, it.nombre) },
                        foto = user.foto
                    )
                )
            } else {
                val errorBody = response.errorBody()?.string()
                val msg = try {
                    Gson().fromJson(errorBody, Map::class.java)["message"] as? String
                } catch (e: Exception) { null }
                Resource.Error(msg ?: "Error al actualizar el perfil", response.code())
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
                val user = response.body()!!
                Resource.Success(
                    User(
                        id = user.id,
                        nombre = user.nombre,
                        correo = user.correo,
                        activo = user.activo,
                        roles = user.roles.map { Role(it.id, it.nombre) },
                        foto = user.foto
                    )
                )
            } else {
                val errorBody = response.errorBody()?.string()
                val msg = try {
                    Gson().fromJson(errorBody, Map::class.java)["message"] as? String
                } catch (e: Exception) { null }
                Resource.Error(msg ?: "Error al subir la foto", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun getMe(): Resource<User> {
        return try {
            val response = authApi.me()
            if (response.isSuccessful) {
                val user = response.body()!!
                Resource.Success(
                    User(
                        id = user.id,
                        nombre = user.nombre,
                        correo = user.correo,
                        activo = user.activo,
                        roles = user.roles.map { Role(it.id, it.nombre) },
                        foto = user.foto
                    )
                )
            } else {
                Resource.Error("No autorizado", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            val response = authApi.logout()
            tokenManager.deleteToken()
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Success(Unit)
        } catch (e: Exception) {
            tokenManager.deleteToken()
            Resource.Success(Unit)
        }
    }

    override suspend fun getToken(): String? = tokenManager.getToken()

    override suspend fun isLoggedIn(): Boolean = tokenManager.getToken() != null

    override fun getTokenFlow(): Flow<String?> = tokenManager.tokenFlow
}
