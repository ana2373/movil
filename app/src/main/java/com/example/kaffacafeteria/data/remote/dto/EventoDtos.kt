package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventoImagenDto(
    val id: Int,
    val url: String,
    val orden: Int? = null
)

/** Evento tal como lo devuelve el backend (fecha "YYYY-MM-DD", hora "HH:MM"). */
data class EventoDto(
    val id: Int,
    val titulo: String,
    val descripcion: String = "",
    val lugar: String? = null,
    val fecha: String,
    val hora: String? = null,
    val publicado: Boolean = true,
    val imagenes: List<EventoImagenDto> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
) {
    val portada: String? get() = imagenes.minByOrNull { it.orden ?: 0 }?.url ?: imagenes.firstOrNull()?.url
}

data class EventoRequest(
    val titulo: String,
    val descripcion: String = "",
    val lugar: String? = null,
    val fecha: String,
    val hora: String? = null,
    val publicado: Boolean = true,
    val imagenes: List<String>? = null
)

data class EventoResponse(
    val data: EventoDto
)

data class ImagenSubidaResponse(
    val url: String
)