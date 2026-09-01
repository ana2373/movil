package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DashboardDto(
    @SerializedName("total_ventas_hoy") val totalVentasHoy: String?,
    @SerializedName("total_pedidos_hoy") val totalPedidosHoy: Int?,
    @SerializedName("pedidos_pendientes") val pedidosPendientes: Int?,
    @SerializedName("pedidos_en_preparacion") val pedidosEnPreparacion: Int?,
    @SerializedName("productos_mas_vendidos") val productosMasVendidos: List<ProductoDto>?,
    @SerializedName("caja_abierta") val cajaAbierta: CajaDto?
)
