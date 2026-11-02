package com.example.kaffacafeteria.data.local

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
import com.example.kaffacafeteria.domain.model.Promocion

/**
 * Línea del carrito.
 *
 * `descuentoPorcentaje` guarda el descuento de la promoción vigente: el
 * precio que se cobra es el del producto menos ese porcentaje.
 */
data class CartItem(
    val producto: ProductoDto,
    val cantidad: Int = 1,
    val descuentoPorcentaje: Int = 0
) {
    /** Precio de lista del producto, sin descuentos. */
    val precioBase: Double get() = producto.precioVenta.toDoubleOrNull() ?: 0.0

    /** Precio unitario que se cobra (con el descuento de la promo aplicado). */
    val precioUnitario: Double get() =
        precioBase * (1 - descuentoPorcentaje.coerceIn(0, 100) / 100.0)

    val subtotal: Double get() = precioUnitario * cantidad

    /** Ahorro que aporta esta línea por el descuento de la promoción. */
    val ahorro: Double get() = (precioBase - precioUnitario) * cantidad

    val tieneDescuento: Boolean get() = descuentoPorcentaje > 0
}

class CartStore {
    private val _items = mutableStateListOf<CartItem>()
    val items: List<CartItem> get() = _items

    /** Promoción aplicada al carrito (la que originó el descuento). */
    var promoAplicada by mutableStateOf<Promocion?>(null)
        private set

    /** Productos que tienen descuento por la promoción aplicada. */
    private var idsConDescuento: Set<Int> = emptySet()

    val total: Double get() = _items.sumOf { it.subtotal }
    val count: Int get() = _items.size

    /** Total que el cliente ahorra con la promoción. */
    val ahorroPromo: Double get() = _items.sumOf { it.ahorro }

    fun add(producto: ProductoDto, cantidad: Int = 1, descuentoPorcentaje: Int = 0) {
        val index = _items.indexOfFirst { it.producto.id == producto.id }
        if (index >= 0) {
            val actual = _items[index]
            _items[index] = actual.copy(
                cantidad = actual.cantidad + cantidad,
                // Si el producto ya estaba con descuento, se conserva el mayor.
                descuentoPorcentaje = maxOf(actual.descuentoPorcentaje, descuentoPorcentaje)
            )
        } else {
            _items.add(CartItem(producto, cantidad, descuentoPorcentaje))
        }
    }

    fun updateCantidad(productoId: Int, cantidad: Int) {
        if (cantidad <= 0) {
            remove(productoId)
            return
        }
        val index = _items.indexOfFirst { it.producto.id == productoId }
        if (index >= 0) _items[index] = _items[index].copy(cantidad = cantidad)
    }

    fun remove(productoId: Int) {
        _items.removeAll { it.producto.id == productoId }
    }

    fun clear() {
        _items.clear()
        idsConDescuento = emptySet()
        promoAplicada = null
    }

    /**
     * Agrega al carrito los productos de una promoción y aplica su descuento.
     *
     * Los productos se cobran como cualquier otra línea, pero con el precio de
     * la promoción; así el pedido se registra con los `producto_id` reales y
     * el total cuadra con el detalle (sin tocar el backend).
     *
     * Si el producto ya estaba en el carrito no se duplica la cantidad: sólo
     * se le aplica el descuento.
     */
    fun aplicarPromo(promo: Promocion, productos: List<ProductoDto>) {
        if (productos.isEmpty()) return
        val descuento = promo.descuentoPorcentaje.coerceIn(0, 100)
        val idsPromo = productos.map { it.id }.toSet()
        idsConDescuento = idsPromo
        productos.forEach { producto ->
            val index = _items.indexOfFirst { it.producto.id == producto.id }
            if (index >= 0) {
                val actual = _items[index]
                _items[index] = actual.copy(
                    descuentoPorcentaje = maxOf(actual.descuentoPorcentaje, descuento)
                )
            } else {
                _items.add(CartItem(producto, 1, descuento))
            }
        }
        promoAplicada = promo
    }

    /** Quita la promoción y devuelve los productos a su precio de lista. */
    fun quitarPromo() {
        if (idsConDescuento.isNotEmpty()) {
            _items.replaceAll { item ->
                if (idsConDescuento.contains(item.producto.id)) item.copy(descuentoPorcentaje = 0) else item
            }
        }
        idsConDescuento = emptySet()
        promoAplicada = null
    }
}
