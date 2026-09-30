package com.example.kaffacafeteria.data.remote.interceptor

import com.example.kaffacafeteria.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Interceptor de diagnóstico de red.
 *
 * IMPORTANTE (producción): el cuerpo completo de cada petición y respuesta
 * contiene datos sensibles (correos, tokens, contraseñas y payloads de caja).
 * Por eso el nivel BODY sólo se activa en compilaciones debug; en release el
 * cliente no registra nada para no filtrar credenciales en el logcat.
 */
object DebugInterceptor {
    fun create(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
        }
    }
}
