package com.herov.weatherapp.network

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalReq = chain.request()
        val modifiedUrl = originalReq.url()
            .newBuilder()
            .addQueryParameter("appid", token)
            .build()
        val modifiedReq = originalReq
            .newBuilder()
            .url(modifiedUrl)
            .build()
        return chain.proceed(modifiedReq)
    }
}