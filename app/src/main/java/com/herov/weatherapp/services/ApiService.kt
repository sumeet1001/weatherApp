package com.herov.weatherapp.services

import com.herov.weatherapp.model.WeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
    ): WeatherModel
}