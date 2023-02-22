package ufc.insight.ractivity.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

object NetworkUtils {
    fun checkConnectivity(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo != null &&
                    connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting
        }
    }

}
