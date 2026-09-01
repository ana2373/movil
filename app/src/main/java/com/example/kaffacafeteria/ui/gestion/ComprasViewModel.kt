package com.example.kaffacafeteria.ui.gestion

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.CompraDto
import kotlinx.coroutines.launch

data class ComprasUiState(
    val compras: List<CompraDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ComprasViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionApi = (application as KaffaApp).container.transactionApi

    var uiState by mutableStateOf(ComprasUiState())
        private set

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = transactionApi.getCompras(perPage = 1000)
                uiState = uiState.copy(compras = response.body()?.data ?: emptyList(), isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }
}
