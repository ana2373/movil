package com.example.kaffacafeteria.data.local

import com.example.kaffacafeteria.domain.model.Promocion

/**
 * Promoción vigente de la sesión.
 *
 * Se elige una sola vez al abrir la app (rotando entre las promociones
 * activas y no vencidas) para que el Inicio y el Carrito muestren siempre la
 * misma oferta: la "promoción del momento".
 */
class PromoSesion {
    var promo: Promocion? = null
        private set

    fun fijar(promo: Promocion?) {
        this.promo = promo
    }
}
