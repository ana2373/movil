package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PedidoDto(
    val id: Int,
    @SerializedName("cliente_id") val clienteId: Int,
    val total: String,
    val propina: String?,
    val estado: String,
    @SerializedName("caja_id") val cajaId: Int?,
    @SerializedName("barista_id") val baristaId: Int?,
    val detalles: List<PedidoDetalleDto>?,
    val pagos: List<PagoPedidoDto>?,
    @SerializedName("factura_venta") val facturaVenta: FacturaVentaDto?,
    val cliente: UsuarioDto?,
    val barista: UsuarioDto?,
    val created_at: String?,
    val updated_at: String?
)

data class PedidoDetalleDto(
    val id: Int?,
    @SerializedName("producto_id") val productoId: Int,
    val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: String,
    val subtotal: String,
    val producto: ProductoDto?
)

data class PagoPedidoDto(
    val id: Int?,
    @SerializedName("pedido_id") val pedidoId: Int?,
    @SerializedName("medio_pago_id") val medioPagoId: Int,
    val monto: String,
    @SerializedName("comprobante_url") val comprobanteUrl: String?,
    val medio_pago: MedioPagoDto?
)

data class FacturaVentaDto(
    val id: Int?,
    @SerializedName("pedido_id") val pedidoId: Int?,
    @SerializedName("numero_factura") val numeroFactura: String,
    val subtotal: String,
    val impuestos: String,
    val total: String,
    val created_at: String?
)

data class PedidoRequest(
    @SerializedName("cliente_id") val clienteId: Int,
    val total: Double,
    val propina: Double?,
    val estado: String,
    @SerializedName("caja_id") val cajaId: Int?,
    val detalles: List<PedidoDetalleRequest>,
    val pagos: List<PagoPedidoRequest>,
    val factura: FacturaRequest?
)

data class PedidoDetalleRequest(
    @SerializedName("producto_id") val productoId: Int,
    val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    val subtotal: Double
)

data class PagoPedidoRequest(
    @SerializedName("medio_pago_id") val medioPagoId: Int,
    val monto: Double,
    @SerializedName("comprobante_url") val comprobanteUrl: String? = null
)

data class FacturaRequest(
    @SerializedName("numero_factura") val numeroFactura: String,
    val subtotal: Double,
    val impuestos: Double,
    val total: Double
)

data class PedidoUpdateRequest(
    val estado: String? = null,
    @SerializedName("barista_id") val baristaId: Int? = null,
    val total: Double? = null,
    val propina: Double? = null
)
