package com.herov.weatherapp.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationManager(
    private val activity: Activity,
    private val permissionCallback: LocationPermissionCallback,
    private val onLatLonFetchedCallback: OnLatLonFetchedCallback?,
) {
    private val locationPermissionCode = 123
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    interface LocationPermissionCallback {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

    interface OnLatLonFetchedCallback {
        fun addOnSuccessListener(location: Location)
        fun addOnFailureListener()
    }

    fun areLocationPermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            locationPermissionCode
        )
    }

    fun onRequestPermissionResult(
        requestCode: Int,
        permission: Array<out String>,
        grantedResult: IntArray
    ) {
        if (requestCode == locationPermissionCode) {
            if (grantedResult.isNotEmpty() && grantedResult[0] == PackageManager.PERMISSION_GRANTED) {
                permissionCallback.onPermissionGranted()
            } else {
                permissionCallback.onPermissionDenied()
            }
        }
    }

    fun isGpsEnable(context: Context): Boolean {
        val locationManagerGps =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManagerGps.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            onLatLonFetchedCallback?.addOnSuccessListener(location)
        }.addOnFailureListener { e ->
            onLatLonFetchedCallback?.addOnFailureListener()
            Toast.makeText(activity, "${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLatLonFetchedCallback?.addOnSuccessListener(location)
            }
        }.addOnFailureListener {
            onLatLonFetchedCallback?.addOnFailureListener()
        }
    }
}