package com.example.kaffacafeteria.domain.repository

import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.util.Resource

interface AuthRepository {
    suspend fun login(correo: String, password: String): Resource<User>
    suspend fun getMe(): Resource<User>
    suspend fun logout(): Resource<Unit>
    suspend fun getToken(): String?
    suspend fun isLoggedIn(): Boolean
    fun getTokenFlow(): kotlinx.coroutines.flow.Flow<String?>
}
