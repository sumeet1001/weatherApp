package com.herov.weatherapp.activities

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.herov.weatherapp.R
import com.herov.weatherapp.databinding.ActivityMainBinding
import com.herov.weatherapp.model.WeatherModel
import com.herov.weatherapp.network.RetrofitClient
import com.herov.weatherapp.services.ApiService
import com.herov.weatherapp.services.DataStoreManager
import com.herov.weatherapp.services.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), LocationManager.LocationPermissionCallback,
    LocationManager.OnLatLonFetchedCallback, SwipeRefreshLayout.OnRefreshListener {
    private var binding: ActivityMainBinding? = null
    private lateinit var loadingSpinner: ProgressBar
    private var apiService: ApiService? = null
    private lateinit var locationManager: LocationManager
    private var locationSettingsLauncher: ActivityResultLauncher<Intent>? = null
    private lateinit var dataStoreManager: DataStoreManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        dataStoreManager = DataStoreManager(applicationContext)
        setContentView(binding?.root)
        setSupportActionBar(binding?.myToolbar)
        binding?.swipeRefreshLayout?.setOnRefreshListener(this)
        loadingSpinner = binding?.loadingProgress!!
        locationManager = LocationManager(this, this, this)
        apiService = RetrofitClient().apiService
        setupLocationSettingLauncher()
        checkPermissions()
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            val capabilities = it.getNetworkCapabilities(connectivityManager.activeNetwork)
            capabilities?.let {
                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        true
                    }

                    else -> false
                }
            }
        }
        return false
    }


    private fun setupLocationSettingLauncher() {
        locationSettingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                checkPermissions()
            }
    }

    private fun checkPermissions() {
        if (isOnline(this)) {
            if (locationManager.isGpsEnable(this)) {
                if (locationManager.areLocationPermissionsGranted()) {
                    locationManager.getCurrentLocation()
                } else {
                    locationManager.requestLocationPermission()
                }
            } else {
                locationManager.getLastKnownLocation()
                createAlert()
            }
        } else {
            Toast.makeText(this, "Internet not connected", Toast.LENGTH_LONG).show()
            binding?.noInternet?.setImageResource(R.drawable.no_internet)
            binding?.noInternet?.visibility = View.VISIBLE
            loadingSpinner.visibility = ProgressBar.GONE
            binding?.swipeRefreshLayout?.isRefreshing = false
        }

    }

    private fun createAlert() {
        AlertDialog.Builder(this)
            .setTitle("Gps disabled")
            .setMessage("Please turn on gps in settings")
            .setPositiveButton("Go to setting") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                locationSettingsLauncher?.launch(intent)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch {
                    val cords = dataStoreManager.getLatLon()
                    Toast.makeText(
                        this@MainActivity,
                        "Showing last saved location or default location data",
                        Toast.LENGTH_LONG
                    ).show()
                    getWeatherData(cords.lon, cords.lat)
                }
            }
            .create()
            .show()
    }


    private fun getWeatherData(lat: String, lon: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val data = apiService?.getWeather(lat, lon)
                withContext(Dispatchers.Main) {
                    setUiValues(data)
                }
                if (data == null) {
                    binding?.noDataFound?.visibility = View.VISIBLE
                }
                Log.e("err", data.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setUiValues(data: WeatherModel?) {
        loadingSpinner.visibility = ProgressBar.GONE
        binding?.swipeRefreshLayout?.isRefreshing = false
        binding?.noInternet?.visibility = View.GONE
        if (data != null) {
            binding?.card?.visibility = View.VISIBLE
            binding?.grid?.visibility = View.VISIBLE
            binding?.cityName?.text = data.name
            binding?.temp?.text = convertTemp(data.main.temp)
            binding?.windText?.text = data.wind.speed.toString()
            binding?.humidityText?.text = data.main.humidity.toString()
            binding?.feelsLikeText?.text = convertTemp(data.main.feels_like)
            if (data.weather.isNotEmpty()) {
                binding?.rainText?.text = data.weather[0].description
            } else {
                binding?.rainText?.text = "N/A"
            }
        }
    }

    private fun convertTemp(temp: Double): String {
        binding?.degreeTypeTop?.text = "\u2103"
        binding?.degreeTypeGrid?.text = "\u2103"
        return (temp - 273.15).roundToInt().toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        apiService = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationManager.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionDenied() {
        Toast.makeText(this@MainActivity, "Please enable location", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionGranted() {
        if (locationManager.isGpsEnable(this)) {
            locationManager.getCurrentLocation()
        } else {
            createAlert()
        }
    }

    override fun addOnSuccessListener(location: Location) {
        val lat = location.latitude.toString()
        val lon = location.longitude.toString()
        lifecycleScope.launch {
            dataStoreManager.saveLatLon(lat, lon)
        }
        getWeatherData(lat, lon)
    }

    override fun addOnFailureListener() {

    }

    override fun onRefresh() {
        binding?.card?.visibility = View.INVISIBLE
        binding?.grid?.visibility = View.INVISIBLE
        checkPermissions()
    }
}