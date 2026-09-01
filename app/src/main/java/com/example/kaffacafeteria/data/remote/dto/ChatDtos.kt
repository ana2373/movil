package com.example.kaffacafeteria.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MensajeDto(
    val id: Int,
    @SerializedName("remitente_id") val remitenteId: Int,
    @SerializedName("destinatario_id") val destinatarioId: Int,
    val mensaje: String,
    val leido: Boolean?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class ContactoUsuarioDto(
    val id: Int,
    val nombre: String,
    val correo: String
)

data class ContactoResumenDto(
    val contacto: ContactoUsuarioDto,
    @SerializedName("ultimo_mensaje") val ultimoMensaje: String?,
    @SerializedName("ultima_hora") val ultimaHora: String?,
    @SerializedName("no_leidos") val noLeidos: Int?
)

data class ContactosResponse(
    val contactos: List<ContactoResumenDto>
)

data class HiloResponse(
    val contacto: ContactoUsuarioDto?,
    val mensajes: PaginatedResponse<MensajeDto>
)

data class MensajeEnviarRequest(
    @SerializedName("destinatario_id") val destinatarioId: Int,
    val mensaje: String
)
