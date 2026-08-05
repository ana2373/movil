package com.example.kaffacafeteria.ui.profile

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

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as KaffaApp).container.authRepository

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            when (val result = authRepository.getMe()) {
                is Resource.Success -> uiState = uiState.copy(user = result.data, isLoading = false)
                else -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
