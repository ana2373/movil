package com.example.kaffacafeteria.ui.splash

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.launch

data class SplashUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val showPromo: Boolean = false
)

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as KaffaApp).container.authRepository

    var uiState by mutableStateOf(SplashUiState())
        private set

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val token = authRepository.getToken()
            var loggedIn = false
            if (token != null) {
                val result = authRepository.getMe()
                loggedIn = result is Resource.Success
            }
            uiState = SplashUiState(
                isLoggedIn = loggedIn,
                isLoading = false,
                showPromo = true
            )
        }
    }
}
