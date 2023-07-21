package com.herov.weatherapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherModel(
    var coords: Coords,
    var weather: List<Weather>,
    var wind: Wind,
    var main: MainTemp,
    var visibility: Long,
    var name: String,
) : Parcelable

@Parcelize
data class Coords(
    var lon: String,
    var lat: String,
) : Parcelable

@Parcelize
data class Weather(
    var id: Int,
    var main: String,
    var description: String,
    var icon: String,
) : Parcelable

@Parcelize
data class Wind(
    var speed: Double,
    var deg: Double,
) : Parcelable

@Parcelize
data class MainTemp(
    var temp: Double,
    var feels_like: Double,
    var temp_min: Double,
    var temp_max: Double,
    var pressure: Int,
    var humidity: Int,
    var sea_level: Int,
    var grnd_level: Int
) : Parcelable