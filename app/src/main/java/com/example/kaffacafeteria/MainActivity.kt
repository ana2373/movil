package com.example.kaffacafeteria

import android.app.Activity
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                    }
                )
            }
        }
    }
}
