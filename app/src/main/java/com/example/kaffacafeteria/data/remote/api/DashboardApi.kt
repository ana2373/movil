package com.example.kaffacafeteria.data.remote.api

import com.example.kaffacafeteria.data.remote.dto.DashboardDto
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApi {
    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardDto>
}
