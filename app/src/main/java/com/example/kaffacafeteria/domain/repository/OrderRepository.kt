package com.example.kaffacafeteria.domain.repository

import com.example.kaffacafeteria.data.remote.dto.*
import com.example.kaffacafeteria.util.Resource

interface OrderRepository {
    suspend fun getPedidos(perPage: Int? = null, estado: String? = null): Resource<PaginatedResponse<PedidoDto>>
    suspend fun getPedido(id: Int): Resource<PedidoDto>
    suspend fun createPedido(request: PedidoRequest): Resource<PedidoDto>
    suspend fun updatePedido(id: Int, request: PedidoUpdateRequest): Resource<PedidoDto>
    suspend fun deletePedido(id: Int): Resource<Any>
}
