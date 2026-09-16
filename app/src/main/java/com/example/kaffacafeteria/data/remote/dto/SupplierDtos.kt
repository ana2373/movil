package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProveedorDto(
    val id: Int,
    val nombre: String,
    val contacto: String?,
    val telefono: String?,
    @SerializedName("correo") val email: String?,
    val direccion: String?,
    val activo: Boolean?,
    val nit: String?,
    val convenio: String?,
    val created_at: String?,
    val updated_at: String?
)

data class CompraDto(
    val id: Int,
    @SerializedName("proveedor_id") val proveedorId: Int? = null,
    @SerializedName("numero_factura_proveedor") val numeroFactura: String? = null,
    @SerializedName("fecha_compra") val fechaCompra: String? = null,
    @SerializedName("total_compra") val total: String? = null,
    val estado: String? = null,
    val proveedor: ProveedorDto? = null,
    val detalles: List<CompraDetalleDto>? = null,
    val pagos: List<CompraPagoDto>? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class CompraDetalleDto(
    val id: Int?,
    @SerializedName("insumo_id") val insumoId: Int?,
    val cantidad: String? = null,
    @SerializedName("precio_costo") val precioUnitario: String? = null,
    val subtotal: String? = null,
    val insumo: InsumoDto? = null
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
    val categoria: String?,
    @SerializedName("usuario_id") val usuarioId: Int? = null,
    val usuario: UsuarioDto? = null,
    val pagos: List<GastoPagoDto>? = null,
    val created_at: String?,
    val updated_at: String?
) {
    val montoTotal: Double get() = pagos.orEmpty().sumOf { it.monto.toDoubleOrNull() ?: 0.0 }
}

data class GastoPagoDto(
    val id: Int?,
    @SerializedName("gasto_id") val gastoId: Int?,
    @SerializedName("medio_pago_id") val medioPagoId: Int,
    val monto: String,
    @SerializedName("fecha_pago") val fechaPago: String? = null,
    @SerializedName("comprobante_url") val comprobanteUrl: String? = null,
    val medio_pago: MedioPagoDto? = null
)

data class GastoRequest(
    val descripcion: String,
    val monto: Double,
    val categoria: String? = null,
    val pagos: List<GastoPagoRequest>? = null
)

data class GastoPagoRequest(
    @SerializedName("medio_pago_id") val medioPagoId: Int,
    val monto: Double
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
