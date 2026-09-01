package com.example.kaffacafeteria.ui.splash

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
<<<<<<< HEAD
import com.example.kaffacafeteria.domain.model.User
=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.launch

data class SplashUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
<<<<<<< HEAD
    val showPromo: Boolean = false,
    val user: User? = null
=======
    val showPromo: Boolean = false
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
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
<<<<<<< HEAD
            var user: User? = null
            if (token != null) {
                val result = authRepository.getMe()
                if (result is Resource.Success) {
                    loggedIn = true
                    user = result.data
                }
=======
            if (token != null) {
                val result = authRepository.getMe()
                loggedIn = result is Resource.Success
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
            }
            uiState = SplashUiState(
                isLoggedIn = loggedIn,
                isLoading = false,
<<<<<<< HEAD
                showPromo = true,
                user = user
=======
                showPromo = true
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
            )
        }
    }
}
