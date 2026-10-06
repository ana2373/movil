package com.example.kaffacafeteria.data.remote.interceptor

import com.example.kaffacafeteria.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.getToken() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .build()
        } else {
            chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
        }
        val response = chain.proceed(request)

        // 401 con token adjunto = sesión vencida o inválida: se borra el token y
        // se avisa a la UI para que lleve al usuario al login. Si no había token
        // (p. ej. login con credenciales incorrectas), no se hace nada.
        if (response.code == 401 && token != null) {
            runBlocking { tokenManager.deleteToken() }
            tokenManager.notificarSesionExpirada()
        }
        return response
    }
}
