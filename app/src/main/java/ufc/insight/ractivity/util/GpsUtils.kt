package ufc.insight.ractivity.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity

object GpsUtils {

    fun checkGPSEnable(ctx: Context): Boolean {

        val mLocationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Checking GPS is enabled
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun soliciteGpsActived(ctx: Context) {
        val dialogBuilder = AlertDialog.Builder(ctx)
        dialogBuilder.setMessage("Seu GPS parece estar desativado, deseja ativá-lo?")
            .setCancelable(false)
            .setPositiveButton("Sim", DialogInterface.OnClickListener { dialog, id
                ->
                startActivity(ctx, Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), null)
            })
            .setNegativeButton("Não", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.show()
    }
}