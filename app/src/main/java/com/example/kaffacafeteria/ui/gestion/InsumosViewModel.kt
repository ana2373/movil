package com.example.kaffacafeteria.ui.gestion

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.InsumoDto
import kotlinx.coroutines.launch

data class InsumosUiState(
    val insumos: List<InsumoDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class InsumosViewModel(application: Application) : AndroidViewModel(application) {
    private val catalogApi = (application as KaffaApp).container.catalogApi

    var uiState by mutableStateOf(InsumosUiState())
        private set

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = catalogApi.getInsumos(perPage = 1000)
                uiState = uiState.copy(insumos = response.body()?.data ?: emptyList(), isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }
}
