package com.example.kaffacafeteria.data.repository

import com.example.kaffacafeteria.data.remote.api.CatalogApi
import com.example.kaffacafeteria.data.remote.dto.*
import com.example.kaffacafeteria.domain.model.MedioPago
import com.example.kaffacafeteria.domain.repository.CatalogRepository
import com.example.kaffacafeteria.util.Resource

class CatalogRepositoryImpl(
    private val catalogApi: CatalogApi
) : CatalogRepository {

    override suspend fun getCategorias(perPage: Int?, nombre: String?): Resource<PaginatedResponse<CategoriaDto>> {
        return apiCall { catalogApi.getCategorias(perPage, nombre) }
    }

    override suspend fun createCategoria(nombre: String): Resource<CategoriaDto> {
        return apiCall { catalogApi.createCategoria(CategoriaRequest(nombre)) }
    }

    override suspend fun updateCategoria(id: Int, nombre: String): Resource<CategoriaDto> {
        return apiCall { catalogApi.updateCategoria(id, CategoriaRequest(nombre)) }
    }

    override suspend fun deleteCategoria(id: Int): Resource<Any> {
        return apiCall { catalogApi.deleteCategoria(id) }
    }

    override suspend fun getProductos(
        perPage: Int?, nombre: String?, categoriaId: Int?
    ): Resource<PaginatedResponse<ProductoDto>> {
        return apiCall { catalogApi.getProductos(perPage, nombre, categoriaId) }
    }

    override suspend fun getProducto(id: Int): Resource<ProductoDto> {
        return apiCall { catalogApi.getProducto(id) }
    }

    override suspend fun createProducto(request: ProductoRequest): Resource<ProductoDto> {
        return apiCall { catalogApi.createProducto(request) }
    }

    override suspend fun updateProducto(id: Int, request: ProductoRequest): Resource<ProductoDto> {
        return apiCall { catalogApi.updateProducto(id, request) }
    }

    override suspend fun deleteProducto(id: Int): Resource<Any> {
        return apiCall { catalogApi.deleteProducto(id) }
    }

    override suspend fun getMediosPago(): Resource<List<MedioPago>> {
        return try {
            val response = catalogApi.getMediosPago(perPage = 100)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.data?.map {
                    MedioPago(it.id, it.nombre, it.esVirtual, it.activo)
                } ?: emptyList())
            } else {
                Resource.Error("Error al obtener medios de pago", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    override suspend fun createMedioPago(request: MedioPagoRequest): Resource<MedioPagoDto> {
        return apiCall { catalogApi.createMedioPago(request) }
    }

    override suspend fun updateMedioPago(id: Int, request: MedioPagoRequest): Resource<MedioPagoDto> {
        return apiCall { catalogApi.updateMedioPago(id, request) }
    }

    override suspend fun deleteMedioPago(id: Int): Resource<Any> {
        return apiCall { catalogApi.deleteMedioPago(id) }
    }

    private suspend fun <T> apiCall(call: suspend () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error en la petición", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }
}
