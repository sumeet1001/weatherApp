package com.herov.weatherapp.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.herov.weatherapp.model.Coords
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "my_data_store"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(DATA_STORE_NAME)

class DataStoreManager(private val context: Context) {
    private val lat = stringPreferencesKey("lat")
    private val lon = stringPreferencesKey("lon")
    suspend fun saveLatLon(latitude: String, longitude: String) {
        context.dataStore.edit { pref ->
            pref[lat] = latitude
            pref[lon] = longitude
        }
    }

    suspend fun getLatLon(): Coords {
        val lat: Flow<String> = context.dataStore.data.map { pref -> pref[lat] ?: "0.0" }
        val lon: Flow<String> = context.dataStore.data.map { pref -> pref[lon] ?: "0.0" }
        return Coords(lat.first().toString(), lon.first().toString())
    }
}