package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProveedorDto(
    val id: Int,
    val nombre: String,
    val contacto: String?,
    val telefono: String?,
    val email: String?,
    val direccion: String?,
    val activo: Boolean?,
    val created_at: String?,
    val updated_at: String?
)

data class CompraDto(
    val id: Int,
    @SerializedName("proveedor_id") val proveedorId: Int,
    @SerializedName("numero_factura") val numeroFactura: String?,
    val total: String,
    val estado: String?,
    val proveedor: ProveedorDto?,
    val detalles: List<CompraDetalleDto>?,
    val pagos: List<CompraPagoDto>?,
    val created_at: String?,
    val updated_at: String?
)

data class CompraDetalleDto(
    val id: Int?,
    @SerializedName("insumo_id") val insumoId: Int?,
    val cantidad: String,
    @SerializedName("precio_unitario") val precioUnitario: String,
    val subtotal: String,
    val insumo: InsumoDto?
)

data class CompraPagoDto(
    val id: Int?,
    @SerializedName("compra_id") val compraId: Int?,
    @SerializedName("medio_pago_id") val medioPagoId: Int,
    val monto: String,
    val medio_pago: MedioPagoDto?
)

data class GastoDto(
    val id: Int,
    val descripcion: String,
    val monto: String,
    val categoria: String?,
    @SerializedName("caja_id") val cajaId: Int?,
    val created_at: String?,
    val updated_at: String?
)

data class MermaDto(
    val id: Int,
    val descripcion: String,
    val cantidad: String,
    @SerializedName("insumo_id") val insumoId: Int?,
    val insumo: InsumoDto?,
    val motivo: String?,
    val created_at: String?,
    val updated_at: String?
)
