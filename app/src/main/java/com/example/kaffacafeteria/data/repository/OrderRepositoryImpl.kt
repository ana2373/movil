package com.example.kaffacafeteria.data.repository

import com.example.kaffacafeteria.data.remote.api.OrderApi
import com.example.kaffacafeteria.data.remote.dto.*
import com.example.kaffacafeteria.domain.repository.OrderRepository
import com.example.kaffacafeteria.util.Resource

class OrderRepositoryImpl(
    private val orderApi: OrderApi
) : OrderRepository {

    override suspend fun getPedidos(
        perPage: Int?, estado: String?
    ): Resource<PaginatedResponse<PedidoDto>> {
        return apiCall { orderApi.getPedidos(perPage, estado) }
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
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error en la petición", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }
}
