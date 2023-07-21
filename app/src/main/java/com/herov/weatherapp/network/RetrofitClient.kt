package com.herov.weatherapp.network

import com.herov.weatherapp.services.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitClient {
    private val baseUrl: String = "https://api.openweathermap.org/data/2.5/"
    private val token: String = "b6c57a25c2209fdabe98584478c2d14c"
    private val retrofit: Retrofit by lazy {
        val client = okhttp3.OkHttpClient
            .Builder()
            .addInterceptor(TokenInterceptor(token))
            .build()
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}