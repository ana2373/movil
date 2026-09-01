package com.example.kaffacafeteria.ui.admin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.*
import kotlinx.coroutines.launch

data class AdminUiState(
    val usuarios: List<UsuarioFullDto> = emptyList(),
    val roles: List<RolFullDto> = emptyList(),
    val categorias: List<CategoriaDto> = emptyList(),
    val productos: List<ProductoDto> = emptyList(),
    val mediosPago: List<MedioPagoDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class UserFormState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val selectedRoles: Set<Int> = emptySet(),
    val isEditing: Boolean = false,
    val editingUserId: Int? = null
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val userApi = (application as KaffaApp).container.userApi
    private val catalogApi = (application as KaffaApp).container.catalogApi

    var uiState by mutableStateOf(AdminUiState())
        private set
    var userForm by mutableStateOf(UserFormState())
        private set

    fun loadUsuarios() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = userApi.getUsuarios(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(usuarios = response.body()?.data ?: emptyList(), isLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadRoles() {
        viewModelScope.launch {
            try {
                val response = userApi.getRoles()
                if (response.isSuccessful) {
                    uiState = uiState.copy(roles = response.body() ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    fun loadCategorias() {
        viewModelScope.launch {
            try {
                val response = catalogApi.getCategorias(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(categorias = response.body()?.data ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    fun loadProductos() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = catalogApi.getProductos(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(productos = response.body()?.data ?: emptyList(), isLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadMediosPago() {
        viewModelScope.launch {
            try {
                val response = catalogApi.getMediosPago()
                if (response.isSuccessful) {
                    uiState = uiState.copy(mediosPago = response.body() ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    fun initCreateUser() {
        userForm = UserFormState()
    }

    fun initEditUser(usuario: UsuarioFullDto) {
        userForm = UserFormState(
            nombre = usuario.nombre,
            correo = usuario.correo,
            selectedRoles = usuario.roles?.map { it.id }?.toSet() ?: emptySet(),
            isEditing = true,
            editingUserId = usuario.id
        )
    }

    fun updateUserFormNombre(value: String) { userForm = userForm.copy(nombre = value) }
    fun updateUserFormCorreo(value: String) { userForm = userForm.copy(correo = value) }
    fun updateUserFormPassword(value: String) { userForm = userForm.copy(password = value) }
    fun toggleUserRole(roleId: Int) {
        val roles = userForm.selectedRoles.toMutableSet()
        if (roles.contains(roleId)) roles.remove(roleId) else roles.add(roleId)
        userForm = userForm.copy(selectedRoles = roles)
    }

    fun setUserRoles(roleIds: List<Int>) {
        userForm = userForm.copy(selectedRoles = roleIds.toSet())
    }

    fun saveUser() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                if (userForm.isEditing && userForm.editingUserId != null) {
                    val request = UsuarioUpdateRequest(
                        nombre = userForm.nombre.ifBlank { null },
                        correo = userForm.correo.ifBlank { null },
                        password = userForm.password.ifBlank { null },
                        roles = userForm.selectedRoles.toList()
                    )
                    val response = userApi.updateUsuario(userForm.editingUserId!!, request)
                    if (response.isSuccessful) {
                        uiState = uiState.copy(isLoading = false, successMessage = "Usuario actualizado")
                        loadUsuarios()
                    } else {
                        uiState = uiState.copy(isLoading = false, error = "Error al actualizar usuario")
                    }
                } else {
                    val request = UsuarioCreateRequest(
                        nombre = userForm.nombre,
                        correo = userForm.correo,
                        password = userForm.password,
                        passwordConfirmation = userForm.password,
                        roles = userForm.selectedRoles.toList()
                    )
                    val response = userApi.createUsuario(request)
                    if (response.isSuccessful) {
                        uiState = uiState.copy(isLoading = false, successMessage = "Usuario creado")
                        loadUsuarios()
                    } else {
                        uiState = uiState.copy(isLoading = false, error = "Error al crear usuario")
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            try {
                userApi.deleteUsuario(id)
                loadUsuarios()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun createCategoria(nombre: String) {
        viewModelScope.launch {
            try {
                catalogApi.createCategoria(CategoriaRequest(nombre))
                loadCategorias()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun createMedioPago(request: MedioPagoRequest) {
        viewModelScope.launch {
            try {
                catalogApi.createMedioPago(request)
                loadMediosPago()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun deleteCategoria(id: Int) {
        viewModelScope.launch {
            try {
                catalogApi.deleteCategoria(id)
                loadCategorias()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun createProducto(request: ProductoRequest) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                catalogApi.createProducto(request)
                loadProductos()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun deleteProducto(id: Int) {
        viewModelScope.launch {
            try {
                catalogApi.deleteProducto(id)
                loadProductos()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, successMessage = null)
    }
}
