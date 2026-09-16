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
