package com.example.kaffacafeteria.data.remote.interceptor

import okhttp3.logging.HttpLoggingInterceptor

object DebugInterceptor {
    fun create(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
}
