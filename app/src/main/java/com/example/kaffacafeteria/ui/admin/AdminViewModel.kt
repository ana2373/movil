package com.example.kaffacafeteria.ui.admin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response

data class AdminUiState(
    val usuarios: List<UsuarioFullDto> = emptyList(),
    val roles: List<RolFullDto> = emptyList(),
    val categorias: List<CategoriaDto> = emptyList(),
    val productos: List<ProductoDto> = emptyList(),
    val insumos: List<InsumoDto> = emptyList(),
    val recetas: List<RecetaDto> = emptyList(),
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

    fun loadInsumos() {
        viewModelScope.launch {
            try {
                val response = catalogApi.getInsumos(perPage = 1000)
                if (response.isSuccessful) {
                    uiState = uiState.copy(insumos = response.body()?.data ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    fun loadRecetas(productoId: Int) {
        viewModelScope.launch {
            try {
                val response = catalogApi.getInsumosProducto(productoId)
                if (response.isSuccessful) {
                    uiState = uiState.copy(recetas = response.body()?.data ?: emptyList())
                }
            } catch (_: Exception) {
                uiState = uiState.copy(recetas = emptyList())
            }
        }
    }

    fun loadMediosPago() {
        viewModelScope.launch {
            try {
                val response = catalogApi.getMediosPago(perPage = 100)
                if (response.isSuccessful) {
                    uiState = uiState.copy(error = null, mediosPago = response.body()?.data ?: emptyList())
                } else {
                    uiState = uiState.copy(error = serverErrorMessage(response, "Error al cargar métodos de pago"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Error al cargar métodos de pago")
            }
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
        if (uiState.mediosPago.any { it.nombre.equals(request.nombre.trim(), ignoreCase = true) }) {
            uiState = uiState.copy(error = "Ya existe un método de pago llamado \"${request.nombre.trim()}\"")
            return
        }
        viewModelScope.launch {
            try {
                val response = catalogApi.createMedioPago(request)
                if (response.isSuccessful) {
                    uiState = uiState.copy(successMessage = "Método de pago creado")
                    loadMediosPago()
                } else {
                    uiState = uiState.copy(error = serverErrorMessage(response, "Error al crear método de pago"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Error al crear método de pago")
            }
        }
    }

    fun deleteMedioPago(id: Int) {
        viewModelScope.launch {
            try {
                val response = catalogApi.deleteMedioPago(id)
                if (response.isSuccessful) {
                    uiState = uiState.copy(successMessage = "Método de pago eliminado")
                    loadMediosPago()
                } else {
                    uiState = uiState.copy(error = serverErrorMessage(response, "Error al eliminar método de pago"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Error al eliminar método de pago")
            }
        }
    }

    fun updateMedioPago(id: Int, request: MedioPagoRequest) {
        if (uiState.mediosPago.any { it.id != id && it.nombre.equals(request.nombre.trim(), ignoreCase = true) }) {
            uiState = uiState.copy(error = "Ya existe un método de pago llamado \"${request.nombre.trim()}\"")
            return
        }
        viewModelScope.launch {
            try {
                val response = catalogApi.updateMedioPago(id, request)
                if (response.isSuccessful) {
                    uiState = uiState.copy(successMessage = "Método de pago actualizado")
                    loadMediosPago()
                } else {
                    uiState = uiState.copy(error = serverErrorMessage(response, "Error al actualizar método de pago"))
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Error al actualizar método de pago")
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

    fun updateProducto(id: Int, request: ProductoRequest) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = catalogApi.updateProducto(id, request)
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, successMessage = "Producto actualizado")
                    loadProductos()
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error al actualizar producto")
                }
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

    fun createInsumo(nombre: String, stock: Double?, unidadMedida: String?) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = catalogApi.createInsumo(InsumoRequest(nombre = nombre, stockActual = stock, unidadMedida = unidadMedida))
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, successMessage = "Insumo creado")
                    loadInsumos()
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error al crear insumo")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateInsumo(id: Int, nombre: String, stock: Double?, unidadMedida: String?) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = catalogApi.updateInsumo(id, InsumoRequest(nombre = nombre, stockActual = stock, unidadMedida = unidadMedida))
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, successMessage = "Insumo actualizado")
                    loadInsumos()
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error al actualizar insumo")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteInsumo(id: Int) {
        viewModelScope.launch {
            try {
                catalogApi.deleteInsumo(id)
                loadInsumos()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun addInsumoToProducto(productoId: Int, insumoId: Int, cantidad: Double) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = catalogApi.addInsumoToProducto(productoId, RecetaRequest(insumoId = insumoId, cantidad = cantidad))
                if (response.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, successMessage = "Insumo agregado al producto")
                    loadRecetas(productoId)
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error al agregar insumo al producto")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun removeReceta(recetaId: Int, productoId: Int) {
        viewModelScope.launch {
            try {
                catalogApi.deleteReceta(recetaId)
                loadRecetas(productoId)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, successMessage = null)
    }

    private fun serverErrorMessage(response: Response<*>, fallback: String): String {
        val code = response.code()
        val body = runCatching { response.errorBody()?.string() }.getOrNull()
        val detail = body?.let { raw ->
            runCatching {
                val map = Gson().fromJson(raw, Map::class.java)
                val msg = map["message"] as? String
                val exception = map["exception"] as? String
                when {
                    msg != null && msg.contains("duplicate key", ignoreCase = true) &&
                        msg.contains("unique constraint", ignoreCase = true) ->
                        "Ya existe un método de pago con ese nombre en el servidor"
                    !msg.isNullOrBlank() && msg != "Server Error" -> msg
                    !exception.isNullOrBlank() -> exception
                    !msg.isNullOrBlank() -> msg
                    else -> null
                }?.take(600)
            }.getOrNull() ?: body.takeIf { it.isNotBlank() }?.take(600)
        }
        return if (!detail.isNullOrBlank()) "$fallback (código $code): $detail"
        else "$fallback (código $code)"
    }
}
