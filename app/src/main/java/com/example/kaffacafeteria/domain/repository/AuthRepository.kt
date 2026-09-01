package com.example.kaffacafeteria.domain.repository

import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.util.Resource

interface AuthRepository {
    suspend fun login(correo: String, password: String): Resource<User>
<<<<<<< HEAD
    suspend fun register(nombre: String, correo: String, password: String): Resource<User>
    suspend fun updateProfile(nombre: String, correo: String, password: String?): Resource<User>
    suspend fun subirFoto(fotoPath: String): Resource<User>
=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
    suspend fun getMe(): Resource<User>
    suspend fun logout(): Resource<Unit>
    suspend fun getToken(): String?
    suspend fun isLoggedIn(): Boolean
    fun getTokenFlow(): kotlinx.coroutines.flow.Flow<String?>
}
