package com.example.kaffacafeteria.util

<<<<<<< HEAD
import com.example.kaffacafeteria.R
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
import java.text.NumberFormat
import java.util.Locale

fun Double.formatCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    format.maximumFractionDigits = 0
    return format.format(this)
}

fun String?.orEmpty(): String = this ?: ""

fun Int?.orZero(): Int = this ?: 0

fun Double?.orZero(): Double = this ?: 0.0
<<<<<<< HEAD

fun String?.toImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    return if (startsWith("http://") || startsWith("https://")) {
        this
    } else {
        Constants.IMAGE_BASE_URL + trimStart('/')
    }
}

private fun categoriaDrawables(nombre: String?): List<Int>? {
    val n = (nombre ?: "").lowercase(Locale.ROOT)
    return when {
        n.contains("cafe") || n.contains("café") || n.contains("espresso") -> listOf(
            R.drawable.coffee1, R.drawable.coffee2, R.drawable.coffee3, R.drawable.coffee4
        )
        n.contains("pan") -> listOf(
            R.drawable.pan, R.drawable.pan2, R.drawable.pan3, R.drawable.pan4
        )
        n.contains("postre") -> listOf(
            R.drawable.postre, R.drawable.postre2, R.drawable.postre3, R.drawable.postre4
        )
        n.contains("jugo") -> listOf(
            R.drawable.jugo, R.drawable.jugo2
        )
        n.contains("agua") || n.contains("bebida") || n.contains("refresco") -> listOf(
            R.drawable.agua
        )
        n.contains("aromatica") || n.contains("aromática") || n.contains("te") -> listOf(
            R.drawable.aromatica
        )
        else -> null
    }
}

fun ProductoDto.imagenLocal(): Int? {
    val options = categoriaDrawables(categoria?.nombre)
        ?: categoriaDrawables(nombre)
        ?: return null
    return options[((id % options.size) + options.size) % options.size]
}
=======
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
