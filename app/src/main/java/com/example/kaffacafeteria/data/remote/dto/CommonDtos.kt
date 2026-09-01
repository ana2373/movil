package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaginatedResponse<T>(
    val data: List<T>,
    val meta: MetaData
)

data class MetaData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    val total: Int,
    val perPage: Int?
)

data class CategoriaDto(
    val id: Int,
    val nombre: String,
    val activo: Boolean?,
    val created_at: String?,
    val updated_at: String?
)

data class CategoriaRequest(
    val nombre: String
)

data class ProductoDto(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    @SerializedName("precio_venta") val precioVenta: String,
    val categoria: CategoriaDto?,
    @SerializedName("categoria_id") val categoriaId: Int?,
    val activo: Boolean?,
    val imagen: String?,
    val created_at: String?,
    val updated_at: String?
)

data class ProductoRequest(
    val nombre: String,
    val descripcion: String?,
    @SerializedName("precio_venta") val precioVenta: Double,
    @SerializedName("categoria_id") val categoriaId: Int,
    val imagen: String? = null
)

data class InsumoDto(
    val id: Int,
    val nombre: String,
    val stock_actual: String?,
    val unidad_medida: String?,
    val activo: Boolean?,
    val created_at: String?,
    val updated_at: String?
)

data class RecetaDto(
    val id: Int,
    val producto: ProductoDto?,
    val insumo: InsumoDto?,
    val cantidad: String?,
    val created_at: String?,
    val updated_at: String?
)

data class MedioPagoDto(
    val id: Int,
    val nombre: String,
    @SerializedName("es_virtual") val esVirtual: Boolean,
    val activo: Boolean?,
    val created_at: String?,
    val updated_at: String?
)

data class MedioPagoRequest(
    val nombre: String,
    @SerializedName("es_virtual") val esVirtual: Boolean
)
