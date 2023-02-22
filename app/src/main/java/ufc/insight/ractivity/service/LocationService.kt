package ufc.insight.ractivity.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ufc.insight.ractivity.util.Constants
import ufc.insight.ractivity.util.Preferences
import ufc.insight.ractivity.util.SharedVariables

class LocationService : Service() {

    companion object {
        const val TAG = "LocationTracker"
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var prefs: SharedPreferences

    override fun onBind(intent: Intent): IBinder {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    ACTION_START_FOREGROUND_SERVICE -> startForegroundService()
                    ACTION_STOP_FOREGROUND_SERVICE -> stopForegroundService()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun startForegroundService() {
        prefs = Preferences.getPrefs(this@LocationService)
        initializeLocationClients(this@LocationService, prefs)
        getLastLocation(prefs)
    }

    private fun stopForegroundService() {
        Log.i(TAG, "Stop Foreground Service")

        fusedLocationClient.removeLocationUpdates(locationCallback)
        // Stop Foreground Service and Remove Notification
        stopForeground(STOP_FOREGROUND_DETACH)
        // Stop the service
        stopSelf()
    }

    fun initializeLocationClients(context: Context, prefs: SharedPreferences) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        settingsClient = LocationServices.getSettingsClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLocation = locationResult?.lastLocation
                setValuesLocationInSharedVariables(currentLocation, prefs)
            }
        }
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(prefs: SharedPreferences) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            setValuesLocationInSharedVariables(location, prefs)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2500)
                .setMinUpdateDistanceMeters(0F)
                .setIntervalMillis(5000)
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

    private fun saveValuesInSharedPreferences(prefs: SharedPreferences) {
        CoroutineScope(Dispatchers.IO).launch {
            prefs.edit().putString(Constants.ALTITUDE, SharedVariables.altitude).apply()
            prefs.edit().putString(Constants.LATITUDE, SharedVariables.latitude).apply()
            prefs.edit().putString(Constants.LONGITUDE, SharedVariables.longitude).apply()
        }
    }

    private fun setValuesLocationInSharedVariables(
        location: Location?,
        prefs: SharedPreferences
    ) {
        if (location != null) {
            SharedVariables.altitude = location.altitude.toString()
            SharedVariables.latitude = location.latitude.toString()
            SharedVariables.longitude = location.longitude.toString()
            saveValuesInSharedPreferences(prefs)
        }
    }
}