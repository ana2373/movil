package com.example.kaffacafeteria.ui.splash

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.launch

data class SplashUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val showPromo: Boolean = false,
    val user: User? = null
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
            var user: User? = null
            if (token != null) {
                val result = authRepository.getMe()
                if (result is Resource.Success) {
                    loggedIn = true
                    user = result.data
                }
            }
            uiState = SplashUiState(
                isLoggedIn = loggedIn,
                isLoading = false,
                showPromo = true,
                user = user
            )
        }
    }
}
