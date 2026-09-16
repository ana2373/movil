package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CajaDto(
    val id: Int,
    @SerializedName("abierta_por") val abiertaPor: Int? = null,
    @SerializedName("cerrada_por") val cerradaPor: Int? = null,
    @SerializedName("turno_id") val turnoId: Int? = null,
    val estado: String,
    @SerializedName("monto_apertura_sistema") val montoAperturaSistema: String? = null,
    @SerializedName("monto_apertura_fisico") val montoAperturaFisico: String? = null,
    @SerializedName("monto_apertura_efectivo") val montoAperturaEfectivo: String? = null,
    @SerializedName("monto_apertura_digital") val montoAperturaDigital: String? = null,
    @SerializedName("monto_cierre_sistema") val montoCierreSistema: String? = null,
    @SerializedName("monto_cierre_fisico") val montoCierreFisico: String? = null,
    @SerializedName("monto_cierre_efectivo") val montoCierreEfectivo: String? = null,
    @SerializedName("monto_cierre_digital") val montoCierreDigital: String? = null,
    @SerializedName("fecha_apertura") val fechaApertura: String? = null,
    @SerializedName("fecha_cierre") val fechaCierre: String? = null,
    @SerializedName("usuario_apertura") val usuarioApertura: UsuarioDto? = null,
    @SerializedName("usuario_cierre") val usuarioCierre: UsuarioDto? = null,
    val movimientos: List<MovimientoCajaDto>? = null,
    val created_at: String? = null,
    val updated_at: String? = null
) {
    val estaAbierta: Boolean get() = estado == "abierta"

    val montoApertura: Double
        get() = montoAperturaSistema?.toDoubleOrNull() ?: montoAperturaFisico?.toDoubleOrNull() ?: 0.0

    val montoCierre: Double?
        get() = montoCierreSistema?.toDoubleOrNull() ?: montoCierreFisico?.toDoubleOrNull()

    val totalIngresos: Double
        get() = movimientos.orEmpty().filter { it.tipo == "ingreso" }.sumOf { it.monto.toDoubleOrNull() ?: 0.0 }

    val totalEgresos: Double
        get() = movimientos.orEmpty().filter { it.tipo == "egreso" }.sumOf { it.monto.toDoubleOrNull() ?: 0.0 }

    val saldoActual: Double
        get() = montoCierre ?: (montoApertura + totalIngresos - totalEgresos)
}

data class MovimientoCajaDto(
    val id: Int,
    @SerializedName("caja_id") val cajaId: Int,
    val tipo: String,
    val descripcion: String?,
    val monto: String,
    val created_at: String?
)

data class CajaRequest(
    @SerializedName("monto_apertura_fisico") val montoAperturaFisico: Double,
    @SerializedName("monto_apertura_efectivo") val montoAperturaEfectivo: Double? = null,
    @SerializedName("monto_apertura_digital") val montoAperturaDigital: Double? = null
)

data class CajaCierreRequest(
    val estado: String = "cerrada",
    @SerializedName("monto_cierre_fisico") val montoCierreFisico: Double,
    @SerializedName("monto_cierre_efectivo") val montoCierreEfectivo: Double? = null,
    @SerializedName("monto_cierre_digital") val montoCierreDigital: Double? = null
)

data class MovimientoCajaRequest(
    val tipo: String,
    val descripcion: String,
    val monto: Double
)
