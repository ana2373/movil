package com.example.kaffacafeteria.data.repository

import com.example.kaffacafeteria.data.remote.api.OrderApi
import com.example.kaffacafeteria.data.remote.dto.*
import com.example.kaffacafeteria.domain.repository.OrderRepository
import com.example.kaffacafeteria.util.ApiErrors
import com.example.kaffacafeteria.util.Resource
import com.example.kaffacafeteria.util.emptyBody

class OrderRepositoryImpl(
    private val orderApi: OrderApi
) : OrderRepository {

    override suspend fun getPedidos(
        perPage: Int?, estado: String?
    ): Resource<PaginatedResponse<PedidoDto>> {
        return apiCall { orderApi.getPedidos(perPage = perPage, estado = estado) }
    }

    override suspend fun getPedido(id: Int): Resource<PedidoDto> {
        return apiCall { orderApi.getPedido(id) }
    }

    override suspend fun createPedido(request: PedidoRequest): Resource<PedidoDto> {
        return apiCall { orderApi.createPedido(request) }
    }

    override suspend fun updatePedido(id: Int, request: PedidoUpdateRequest): Resource<PedidoDto> {
        return apiCall { orderApi.updatePedido(id, request) }
    }

    override suspend fun deletePedido(id: Int): Resource<Any> {
        return apiCall { orderApi.deletePedido(id) }
    }

    private suspend fun <T> apiCall(call: suspend () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                // 2xx sin cuerpo (típico en DELETE/204): no hay nada que mapear
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Success(emptyBody())
            } else {
                Resource.Error(
                    ApiErrors.parse(response, "Error en la petición"),
                    response.code(),
                    ApiErrors.code(response)
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }
}
