package com.example.kaffacafeteria.domain.repository

import com.example.kaffacafeteria.data.remote.dto.*
import com.example.kaffacafeteria.domain.model.*
import com.example.kaffacafeteria.util.Resource

interface CatalogRepository {
    suspend fun getCategorias(perPage: Int? = null, nombre: String? = null): Resource<PaginatedResponse<CategoriaDto>>
    suspend fun createCategoria(nombre: String): Resource<CategoriaDto>
    suspend fun updateCategoria(id: Int, nombre: String): Resource<CategoriaDto>
    suspend fun deleteCategoria(id: Int): Resource<Any>

    suspend fun getProductos(perPage: Int? = null, nombre: String? = null, categoriaId: Int? = null): Resource<PaginatedResponse<ProductoDto>>
    suspend fun getProducto(id: Int): Resource<ProductoDto>
    suspend fun createProducto(request: ProductoRequest): Resource<ProductoDto>
    suspend fun updateProducto(id: Int, request: ProductoRequest): Resource<ProductoDto>
    suspend fun deleteProducto(id: Int): Resource<Any>

    suspend fun getMediosPago(): Resource<List<MedioPago>>
    suspend fun createMedioPago(request: MedioPagoRequest): Resource<MedioPagoDto>
    suspend fun updateMedioPago(id: Int, request: MedioPagoRequest): Resource<MedioPagoDto>
    suspend fun deleteMedioPago(id: Int): Resource<Any>
}
