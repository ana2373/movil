package com.example.kaffacafeteria.data.local

import androidx.compose.runtime.mutableStateListOf
import com.example.kaffacafeteria.data.remote.dto.ProductoDto

data class CartItem(
    val producto: ProductoDto,
    val cantidad: Int = 1
) {
    val subtotal: Double get() = (producto.precioVenta.toDoubleOrNull() ?: 0.0) * cantidad
}

class CartStore {
    private val _items = mutableStateListOf<CartItem>()
    val items: List<CartItem> get() = _items

    val total: Double get() = _items.sumOf { it.subtotal }
    val count: Int get() = _items.size

    fun add(producto: ProductoDto, cantidad: Int = 1) {
        val index = _items.indexOfFirst { it.producto.id == producto.id }
        if (index >= 0) {
            _items[index] = _items[index].copy(cantidad = _items[index].cantidad + cantidad)
        } else {
            _items.add(CartItem(producto, cantidad))
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
    }
}
