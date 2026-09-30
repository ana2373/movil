package com.example.kaffacafeteria

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.kaffacafeteria.ui.navigation.AppNavigation
import com.example.kaffacafeteria.ui.theme.KaffaTheme
import kotlinx.coroutines.launch

/**
 * Deep links de la aplicación.
 *
 * `kaffa://reset-password?token=...&correo=...`
 *
 * El enlace llega en el Intent de arranque (app cerrada) o en onNewIntent
 * (app ya abierta), por eso se centraliza el parseo en [parsearDeepLink] y se
 * entrega a Compose como un evento de un solo uso.
 */
data class DeepLink(val destino: String, val argumentos: Map<String, String> = emptyMap())

object DeepLinks {
    const val HOST_RESET_PASSWORD = "reset-password"

    /** Extrae el destino y los parámetros de un intent de la app. */
    fun parsear(intent: Intent?): DeepLink? {
        val data: Uri = intent?.data ?: return null
        if (data.scheme != "kaffa") return null

        val argumentos = data.queryParameterNames
            .associateWith { name -> data.getQueryParameter(name).orEmpty() }

        return when (data.host) {
            HOST_RESET_PASSWORD -> DeepLink("reset_password", argumentos)
            else -> DeepLink(data.host.orEmpty(), argumentos)
        }
    }
}

class MainActivity : ComponentActivity() {

    // Estado de Compose para que MainActivity pueda navegar al llegar un deep link.
    private var deepLinkPendiente by mutableStateOf<DeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkPendiente = DeepLinks.parsear(intent)

        setContent {
            val app = application as KaffaApp
            val scope = rememberCoroutineScope()
            var darkTheme by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                darkTheme = app.container.tokenManager.isDarkMode()
            }

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
                }
            }

            KaffaTheme(darkTheme = darkTheme) {
                AppNavigation(
                    darkTheme = darkTheme,
                    onToggleTheme = { enabled ->
                        darkTheme = enabled
                        scope.launch { app.container.tokenManager.setDarkMode(enabled) }
                    },
                    deepLink = deepLinkPendiente,
                    onDeepLinkHandled = { deepLinkPendiente = null }
                )
            }
        }
    }

    /**
     * Se invoca cuando la app ya está abierta y el usuario toca el enlace
     * del correo: Android reutiliza la actividad existente.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DeepLinks.parsear(intent)?.let { deepLinkPendiente = it }
    }
}
