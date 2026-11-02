package com.example.kaffacafeteria.util

import android.os.Build
import com.example.kaffacafeteria.BuildConfig

object Constants {
    private val isEmulator: Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("emulator") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
            Build.PRODUCT.contains("sdk")

    private const val HOST_EMULATOR = "10.0.2.2"
    private const val HOST_PHYSICAL = "127.0.0.1"
    private const val DEFAULT_PORT = 8000

    /** Anfitrión del backend. Configurable con KAFFA_API_HOST en local.properties. */
    private val host: String = BuildConfig.API_HOST.ifBlank {
        if (isEmulator) HOST_EMULATOR else HOST_PHYSICAL
    }

    /** Puerto del backend. Configurable con KAFFA_API_PORT en local.properties. */
    private val port: String = BuildConfig.API_PORT.ifBlank { DEFAULT_PORT.toString() }

    /** Se omite el puerto en la URL cuando es el estándar del esquema HTTP. */
    private val authority: String = if (port == "80") host else "$host:$port"

    val BASE_URL: String = "http://$authority/api/v1/"
    val IMAGE_BASE_URL: String = "http://$authority/"

    const val PREF_NAME = "kaffa_prefs"
    const val TOKEN_KEY = "auth_token"
    const val USER_DATA_KEY = "user_data"
    const val PAGE_SIZE = 15
}
