package com.example.kaffacafeteria

import android.app.Application
import com.example.kaffacafeteria.data.local.TokenManager
import com.example.kaffacafeteria.data.remote.api.*
import com.example.kaffacafeteria.data.remote.interceptor.AuthInterceptor
import com.example.kaffacafeteria.data.remote.interceptor.DebugInterceptor
import com.example.kaffacafeteria.data.repository.AuthRepositoryImpl
import com.example.kaffacafeteria.data.repository.CatalogRepositoryImpl
import com.example.kaffacafeteria.data.repository.OrderRepositoryImpl
import com.example.kaffacafeteria.domain.repository.AuthRepository
import com.example.kaffacafeteria.domain.repository.CatalogRepository
import com.example.kaffacafeteria.domain.repository.OrderRepository
import com.example.kaffacafeteria.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Application) {
    val tokenManager = TokenManager(context)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenManager))
        .addInterceptor(DebugInterceptor.create())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val catalogApi: CatalogApi = retrofit.create(CatalogApi::class.java)
    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
    val cashRegisterApi: CashRegisterApi = retrofit.create(CashRegisterApi::class.java)
    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val transactionApi: TransactionApi = retrofit.create(TransactionApi::class.java)
    val dashboardApi: DashboardApi = retrofit.create(DashboardApi::class.java)
    val mensajeApi: MensajeApi = retrofit.create(MensajeApi::class.java)

    val authRepository: AuthRepository = AuthRepositoryImpl(authApi, tokenManager)
    val catalogRepository: CatalogRepository = CatalogRepositoryImpl(catalogApi)
    val orderRepository: OrderRepository = OrderRepositoryImpl(orderApi)
}

class KaffaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
