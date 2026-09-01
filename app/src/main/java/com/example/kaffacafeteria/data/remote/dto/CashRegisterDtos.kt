package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CajaDto(
    val id: Int,
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("fecha_apertura") val fechaApertura: String?,
    @SerializedName("fecha_cierre") val fechaCierre: String?,
    @SerializedName("monto_inicial") val montoInicial: String,
    @SerializedName("monto_final") val montoFinal: String?,
    val estado: String,
    val observaciones: String?,
    val usuario: UsuarioDto?,
    val movimientos: List<MovimientoCajaDto>?,
    val created_at: String?,
    val updated_at: String?
)

data class MovimientoCajaDto(
    val id: Int,
    @SerializedName("caja_id") val cajaId: Int,
    val tipo: String,
    val concepto: String?,
    val monto: String,
    val created_at: String?
)

data class CajaRequest(
    @SerializedName("monto_inicial") val montoInicial: Double,
    val observaciones: String? = null
)

data class CajaCierreRequest(
    @SerializedName("monto_final") val montoFinal: Double,
    val observaciones: String? = null
)

data class MovimientoCajaRequest(
    val tipo: String,
    val concepto: String,
    val monto: Double
)
