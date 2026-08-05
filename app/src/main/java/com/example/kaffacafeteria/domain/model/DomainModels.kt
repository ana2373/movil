package com.example.kaffacafeteria.domain.model

data class User(
    val id: Int,
    val nombre: String,
    val correo: String,
    val activo: Boolean,
    val roles: List<Role>
) {
    val isAdmin: Boolean get() = roles.any { it.nombre == "admin" }
    val isBarista: Boolean get() = roles.any { it.nombre == "barista" }
    val isCliente: Boolean get() = roles.any { it.nombre == "cliente" }
}

data class Role(
    val id: Int,
    val nombre: String
)

data class Product(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precioVenta: Double,
    val categoria: Category?,
    val categoriaId: Int?,
    val activo: Boolean?,
    val imagen: String?
)

data class Category(
    val id: Int,
    val nombre: String,
    val activo: Boolean?
)

data class MedioPago(
    val id: Int,
    val nombre: String,
    val esVirtual: Boolean,
    val activo: Boolean?
)

data class Order(
    val id: Int,
    val clienteId: Int,
    val total: Double,
    val propina: Double?,
    val estado: String,
    val cajaId: Int?,
    val baristaId: Int?,
    val detalles: List<OrderDetail>?,
    val pagos: List<Payment>?,
    val factura: Invoice?,
    val cliente: User?,
    val barista: User?,
    val createdAt: String?
)

data class OrderDetail(
    val id: Int?,
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,
    val producto: Product?
)

data class Payment(
    val id: Int?,
    val pedidoId: Int?,
    val medioPagoId: Int,
    val monto: Double,
    val comprobanteUrl: String?,
    val medioPago: MedioPago?
)

data class Invoice(
    val id: Int?,
    val pedidoId: Int?,
    val numeroFactura: String,
    val subtotal: Double,
    val impuestos: Double,
    val total: Double
)
