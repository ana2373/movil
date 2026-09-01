package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MensajeApi {
    @GET("mensajes/contactos")
    suspend fun getContactos(): Response<List<ContactoUsuarioDto>>

    @GET("mensajes")
    suspend fun getConversaciones(): Response<ContactosResponse>

    @GET("mensajes")
    suspend fun getHilo(@Query("con") con: Int): Response<HiloResponse>

    @POST("mensajes")
    suspend fun enviarMensaje(@Body request: MensajeEnviarRequest): Response<MensajeDto>

    @POST("mensajes/leer")
    suspend fun marcarLeidos(@Body request: MarcarLeidosRequest): Response<Any>
}

data class MarcarLeidosRequest(val con: Int)
