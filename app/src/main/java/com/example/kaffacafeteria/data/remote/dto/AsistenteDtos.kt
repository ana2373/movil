package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Turno del chat del asistente: rol "user" (pregunta) o "assistant" (respuesta). */
data class AsistenteMensajeDto(
    val id: Int,
    val rol: String,
    val contenido: String,
    @SerializedName("created_at") val createdAt: String? = null
) {
    val esAsistente: Boolean get() = rol == "assistant"
}

data class AsistenteChatRequest(
    val mensaje: String
)

data class AsistenteChatResponse(
    val respuesta: String,
    @SerializedName("mensaje_id") val mensajeId: Int,
    @SerializedName("created_at") val createdAt: String? = null
)