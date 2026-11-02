package com.example.kaffacafeteria.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Promoción de la cafetería.
 *
 * Las promociones son un **CRUD local** (DataStore `promociones_store`): el
 * backend no expone un endpoint de promociones, por eso cada dispositivo
 * guarda su propia copia.
 *
 * - `productoIds`: productos que incluye la promoción (los elige el
 *   administrador). Si la lista está vacía, la promoción usa los productos
 *   destacados del catálogo.
 * - `descuentoPorcentaje`: descuento que se aplica sobre esos productos al
 *   cobrar el carrito (0 = sin descuento). El precio final se calcula en el
 *   dispositivo, sin necesidad de cambios en el backend.
 */
data class Promocion(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tipo: String,
    val colorIndex: Int,
    val imagenUri: String? = null,
    val expiracionMillis: Long? = null,
    val activa: Boolean = true,
    val productoIds: List<Int> = emptyList(),
    val descuentoPorcentaje: Int = 0
)

/** Instante en que deja de mostrarse la promoción, o null si no caduca. */
fun Promocion.fechaLimiteMillis(): Long? = expiracionMillis

fun Promocion.estaExpirada(now: Long = System.currentTimeMillis()): Boolean {
    val limite = fechaLimiteMillis() ?: return false
    return now >= limite
}

/** Vigencia en formato 12 h (am/pm) para la lista: "25/06/2025, 03:30 p. m." */
fun formatExpiracion(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale("es", "CO")).format(Date(millis))

/** Combina el día elegido con la hora (formato 24 h) en un único instante. */
fun buildExpiracionMillis(diaMillis: Long, hora: Int, minuto: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = diaMillis
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
