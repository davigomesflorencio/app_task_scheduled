package ufc.insight.ractivity.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LocationUtils {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationCallback: LocationCallback


    fun initializeLocationClients(context: Context, prefs: SharedPreferences) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        settingsClient = LocationServices.getSettingsClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLocation = locationResult?.lastLocation
                getValuesLocation(currentLocation, prefs)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation(prefs: SharedPreferences) {

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

            getValuesLocation(location, prefs)

            if (SharedVariables.altitude == null && SharedVariables.latitude == null && SharedVariables.longitude == null) {
                startLocationUpdates()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(5000)
                .build()

        val locationSettingsRequest =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
    }

    private fun saveValuesLocation(prefs: SharedPreferences) {
        CoroutineScope(Dispatchers.IO).launch {
            prefs.edit().putString(Constants.ALTITUDE, SharedVariables.altitude).apply()
            prefs.edit().putString(Constants.LATITUDE, SharedVariables.latitude).apply()
            prefs.edit().putString(Constants.LONGITUDE, SharedVariables.longitude).apply()
        }
    }

    private fun getValuesLocation(
        location: Location?,
        prefs: SharedPreferences
    ) {
        if (location != null) {
            SharedVariables.altitude = location.altitude.toString()
            SharedVariables.latitude = location.latitude.toString()
            SharedVariables.longitude = location.longitude.toString()
            saveValuesLocation(prefs)
        }

    }
}