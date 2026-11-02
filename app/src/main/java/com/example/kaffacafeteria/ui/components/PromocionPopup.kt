package com.example.kaffacafeteria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.kaffacafeteria.R
import com.example.kaffacafeteria.data.remote.dto.EventoDto
import com.example.kaffacafeteria.data.remote.dto.ProductoDto
import com.example.kaffacafeteria.domain.model.Promocion
import com.example.kaffacafeteria.domain.model.estaExpirada
import com.example.kaffacafeteria.ui.theme.PromoPalettes
import com.example.kaffacafeteria.ui.theme.White

/**
 * Palabras que identifican los productos "destacados" del catálogo.
 *
 * Se usan como contenido por defecto de una promoción cuando el
 * administrador no elige productos explícitamente.
 */
private val PALABRAS_DESTACADAS = listOf("café", "pan", "postres", "postre", "repostería", "bebida", "jugos")

/**
 * Productos que forman una promoción.
 *
 * Se prefieren los productos configurados por el administrador; si no hay
 * ninguno, se toman los destacados del catálogo (o los primeros si tampoco
 * hay destacados).
 */
fun productosDePromo(promo: Promocion, catalogo: List<ProductoDto>): List<ProductoDto> {
    if (promo.productoIds.isNotEmpty()) {
        val elegidos = catalogo.filter { promo.productoIds.contains(it.id) }
        if (elegidos.isNotEmpty()) return elegidos
    }
    val destacados = catalogo.filter { producto ->
        val categoria = producto.categoria?.nombre?.lowercase() ?: ""
        PALABRAS_DESTACADAS.any { categoria.contains(it) }
    }
    return if (destacados.isNotEmpty()) destacados.take(3) else catalogo.take(3)
}

/** Precio final de los productos de la promoción con su descuento aplicado. */
fun precioPromo(productos: List<ProductoDto>, descuentoPorcentaje: Int): Double {
    val dcto = descuentoPorcentaje.coerceIn(0, 100) / 100.0
    return productos.sumOf { (it.precioVenta.toDoubleOrNull() ?: 0.0) * (1 - dcto) }
}

/**
 * Próximo evento por fecha (el más cercano a hoy).
 *
 * Si todos los eventos ya pasaron, se muestra el primero de la lista.
 */
fun List<EventoDto>.proximoEvento(): EventoDto? {
    if (isEmpty()) return null
    val hoy = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    return filter { it.fecha >= hoy }.minByOrNull { it.fecha } ?: minByOrNull { it.fecha }
}

/**
 * Pantalla emergente con la promoción del momento y el próximo evento.
 *
 * Es un diálogo compacto: la promoción ocupa la parte principal con su botón
 * de compra (el producto se agrega al carrito para pagar allí) y el evento
 * aparece justo debajo, con su botón para ver el detalle.
 */
@Composable
fun PromocionPopup(
    promo: Promocion?,
    evento: EventoDto?,
    productosPromo: List<ProductoDto>,
    onDismiss: () -> Unit,
    onComprarPromo: (Promocion) -> Unit,
    onVerEvento: () -> Unit
) {
    if (promo == null && evento == null) return
    val colorScheme = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 14.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (promo != null) "Promoción de hoy" else "Próximo evento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (promo != null) {
                    PromoPopupCard(
                        promo = promo,
                        productos = productosPromo,
                        onComprar = { onComprarPromo(promo) }
                    )
                }

                if (evento != null) {
                    if (promo != null) Spacer(modifier = Modifier.height(14.dp))
                    EventoPopupCard(evento = evento, onVerEvento = onVerEvento)
                }
            }
        }
    }
}

@Composable
private fun PromoPopupCard(
    promo: Promocion,
    productos: List<ProductoDto>,
    onComprar: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val palette = PromoPalettes.getOrElse(promo.colorIndex) { PromoPalettes.first() }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = palette.cardBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(palette.accent, palette.accentDark))),
                    contentAlignment = Alignment.Center
                ) {
                    val imagen = promo.imagenUri
                    if (!imagen.isNullOrBlank()) {
                        AsyncImage(
                            model = imagen,
                            contentDescription = promo.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(74.dp).clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = White, modifier = Modifier.size(30.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.accent.copy(alpha = 0.18f)
                    ) {
                        Text(
                            promo.tipo,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color = palette.accentDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        promo.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = palette.titleText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (promo.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    promo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.subtitleText,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resumen de lo que incluye la promoción y cuánto queda.
            val descuento = promo.descuentoPorcentaje.coerceIn(0, 100)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (productos.isEmpty()) {
                        "Sin productos configurados"
                    } else {
                        "${productos.size} producto${if (productos.size == 1) "" else "s"}" +
                            if (descuento > 0) " · $descuento% dcto" else ""
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.highlightText
                )
                Spacer(modifier = Modifier.weight(1f))
                if (productos.isNotEmpty()) {
                    Text(
                        text = "$" + "%.2f".format(precioPromo(productos, descuento)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = palette.accentDark
                    )
                }
            }

            promo.expiracionMillis?.let { limite ->
                if (!promo.estaExpirada()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Vence el " + java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date(limite)),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.subtitleText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onComprar,
                enabled = productos.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accentDark,
                    contentColor = White
                )
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Comprar promo y pagar en el carrito", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun EventoPopupCard(evento: EventoDto, onVerEvento: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(colorScheme.primary, colorScheme.secondary))),
                contentAlignment = Alignment.Center
            ) {
                val portada = evento.portada
                if (!portada.isNullOrBlank()) {
                    AsyncImage(
                        model = portada,
                        contentDescription = evento.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Icon(Icons.Default.Event, contentDescription = null, tint = White, modifier = Modifier.size(26.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        textoFechaCorta(evento.fecha),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    evento.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val lugar = evento.lugar?.takeIf { it.isNotBlank() }
                if (lugar != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            lugar,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            TextButton(onClick = onVerEvento) {
                Text("Ver", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** "2025-06-25" → "25/06/2025". */
private fun textoFechaCorta(fecha: String): String {
    if (fecha.isBlank()) return ""
    return try {
        val parsed = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(fecha)
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(parsed!!)
    } catch (_: Exception) {
        fecha
    }
}
