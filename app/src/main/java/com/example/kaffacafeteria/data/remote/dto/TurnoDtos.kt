package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TurnoDto(
    val id: Int,
    val fecha: String,
    val tipo: String,
    val baristas: List<UsuarioFullDto>? = null,
    val created_at: String?,
    val updated_at: String?
)

data class TurnoResponse(
    val data: TurnoDto
)

data class TurnoRequest(
    val fecha: String,
    val tipo: String,
    @SerializedName("barista_ids") val baristaIds: List<Int>
)

data class TurnoUpdateRequest(
    val fecha: String? = null,
    val tipo: String? = null,
    @SerializedName("barista_ids") val baristaIds: List<Int>? = null
)

/** Resumen del turno en curso que devuelve `GET /turnos/activo`. */
data class TurnoInfoDto(
    val id: Int? = null,
    val fecha: String? = null,
    val tipo: String? = null,
    @SerializedName("hora_inicio") val horaInicio: String? = null,
    @SerializedName("hora_fin") val horaFin: String? = null,
    @SerializedName("minutos_restantes") val minutosRestantes: Int? = null
)

/**
 * Respuesta de `GET /turnos/activo`.
 *
 * Es la fuente de verdad del bloqueo operativo del barista:
 *  - `turno_activo`  → tiene turno asignado y vigente.
 *  - `caja_abierta`  → hay una caja abierta en ese turno.
 *  - `puede_operar`  → puede registrar ventas, mermas, gastos o compras.
 *  - `motivo_bloqueo` → mensaje listo para mostrar (CAJA_CERRADA,
 *                      FUERA_HORARIO, TURNO_INACTIVO, TURNO_EXPIRADO...).
 *
 * El backend aplica además el middleware `turno.activo` en los endpoints de
 * operación: la app usa estos campos para explicar el bloqueo antes de que la
 * petición falle.
 */
data class TurnoActivoDto(
    @SerializedName("turno_activo") val turnoActivo: Boolean = false,
    @SerializedName("turno_info") val turnoInfo: TurnoInfoDto? = null,
    @SerializedName("caja_abierta") val cajaAbierta: Boolean = false,
    @SerializedName("caja_id") val cajaId: Int? = null,
    @SerializedName("puede_operar") val puedeOperar: Boolean = false,
    @SerializedName("motivo_bloqueo") val motivoBloqueo: String? = null
)
