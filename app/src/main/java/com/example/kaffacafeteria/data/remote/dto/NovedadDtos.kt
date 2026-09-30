package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Reporte de novedad ("lo que el barista reporta al administrador").
 *
 * El backend expone:
 *  - POST   /reportes                  → admin o barista crea
 *  - GET    /reportes, /reportes/{id}  → sólo admin
 *  - PUT    /reportes/{id}             → sólo admin
 *  - DELETE /reportes/{id}             → sólo admin
 */
data class ReporteDto(
    val id: Int,
    @SerializedName("barista_id") val baristaId: Int? = null,
    val mensaje: String,
    val prioridad: String? = null,
    val leido: Boolean? = null,
    val barista: UsuarioFullDto? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

/** Cuerpo de creación; la autoría la toma el backend del token. */
data class ReporteRequest(
    val mensaje: String,
    val prioridad: String? = null
)

/** Cuerpo de actualización (sólo管理员). */
data class ReporteUpdateRequest(
    val mensaje: String? = null,
    val prioridad: String? = null,
    val leido: Boolean? = null
)

data class ReporteResponse(val data: ReporteDto)
