package com.example.kaffacafeteria.util

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
