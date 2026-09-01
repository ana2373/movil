package com.example.kaffacafeteria.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.DashboardDto
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.util.Resource
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: User? = null,
    val isGuest: Boolean = false,
    val isLoading: Boolean = false,
    val dashboard: DashboardDto? = null,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as KaffaApp).container.authRepository
    private val dashboardApi = (application as KaffaApp).container.dashboardApi

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        viewModelScope.launch {
            authRepository.getTokenFlow().collect { token ->
                if (token == null) {
                    uiState = HomeUiState(isGuest = true, isLoading = false)
                } else {
                    loadUser()
                }
            }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, isGuest = false)
            when (val result = authRepository.getMe()) {
                is Resource.Success -> {
                    uiState = uiState.copy(user = result.data, isLoading = false)
                    loadDashboard()
                }
                is Resource.Error -> {
                    uiState = uiState.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private suspend fun loadDashboard() {
        try {
            val response = dashboardApi.getDashboard()
            if (response.isSuccessful) {
                uiState = uiState.copy(dashboard = response.body())
            }
        } catch (_: Exception) {}
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogout()
        }
    }

    fun refresh() {
        loadUser()
    }
}
