package com.example.kaffacafeteria.ui.productos

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.CategoriaDto
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
import kotlinx.coroutines.launch

data class ProductListUiState(
    val productos: List<ProductoDto> = emptyList(),
    val categorias: List<CategoriaDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategoriaId: Int? = null,
    val searchQuery: String = ""
) {
    val filteredProductos: List<ProductoDto>
        get() = productos.filter { p ->
            (selectedCategoriaId == null || p.categoriaId == selectedCategoriaId) &&
                    (searchQuery.isBlank() || p.nombre.contains(searchQuery, ignoreCase = true))
        }
}

class ProductListViewModel(application: Application) : AndroidViewModel(application) {
    private val catalogApi = (application as KaffaApp).container.catalogApi

    var uiState by mutableStateOf(ProductListUiState())
        private set

    init { loadData() }

    private fun loadData() {
        loadProductos()
        loadCategorias()
    }

    private fun loadProductos() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = catalogApi.getProductos(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(productos = response.body()?.data ?: emptyList(), isLoading = false)
<<<<<<< HEAD
                } else {
                    uiState = uiState.copy(
                        error = "Error al cargar productos (${response.code()})",
                        isLoading = false
                    )
=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, isLoading = false)
            }
        }
    }

    private fun loadCategorias() {
        viewModelScope.launch {
            try {
                val response = catalogApi.getCategorias(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(categorias = response.body()?.data ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    fun selectCategoria(id: Int?) { uiState = uiState.copy(selectedCategoriaId = id) }

    fun updateSearchQuery(query: String) { uiState = uiState.copy(searchQuery = query) }

    fun refresh() { loadData() }
}
