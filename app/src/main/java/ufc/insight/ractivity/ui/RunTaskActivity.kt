package ufc.insight.ractivity.ui

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import ufc.insight.ractivity.R
import ufc.insight.ractivity.data.database.TaskDatabase
import ufc.insight.ractivity.data.model.TaskModel
import ufc.insight.ractivity.databinding.ActivityRunTaskBinding
import ufc.insight.ractivity.receiver.AlarmReceiver
import ufc.insight.ractivity.service.LocationService
import ufc.insight.ractivity.util.Constants.ID_TASK
import ufc.insight.ractivity.util.Constants.NOTIFICATION_INTENT_ACTION
import ufc.insight.ractivity.util.LocationUtils
import ufc.insight.ractivity.util.NetworkUtils
import ufc.insight.ractivity.util.TimeUtils
import ufc.insight.ractivity.util.ToastUtils
import java.util.*


/**
 * Valores Constantes
 */
private const val TAG = "RunTaskActivity"
const val LOCATION_PERMISSION_REQUEST = 101

class RunTaskActivity : AppCompatActivity(),
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks, View.OnClickListener {

    private val activityScope = CoroutineScope(Dispatchers.Default)

    private lateinit var binding: ActivityRunTaskBinding
    private lateinit var btnInit: Button
    private var task: TaskModel? = null

    val db by lazy {
        TaskDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnInit = binding.iniciarAtividade

        var id = intent.extras?.getLong("id")

        GlobalScope.launch(Dispatchers.IO) {
            if (id != null) {
                task = db.taskDao().findById(id.toLong())
                binding.nameTask.text = task?.name
                binding.txtHoraFinal.text = TimeUtils.updateTime(task?.timeEnd)
                binding.txtHoraInicial.text = TimeUtils.updateTime(task?.timeStart)
                if (task!!.scheduled || task!!.done) {
                    btnInit.visibility = View.GONE
                }
            }
        }

        btnInit.setOnClickListener {

            dialogCheckPermissions()
            btnInit.visibility = View.GONE

            if (id != null) {
                activityScope.launch {
                    db.taskDao().scheduled(id)
                }
                setupAlarmManagerByTask(
                    id,
                    TimeUtils.getHour(task!!.timeStart).toInt(),
                    TimeUtils.getMinute(task!!.timeStart).toInt()
                )
            }
        }

        if (NetworkUtils.checkConnectivity(this)) {
            createChannel(
                getString(R.string.task_channel_id),
                getString(R.string.task_channel_name)
            )
            if (!checkLocationPermissions()) {
                requestPermissions()
            }
        } else {
            ToastUtils.showToast(this, "Offline!")
        }
    }

    private fun setupAlarmManagerByTask(id: Long, hour: Int, minute: Int) {
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val REQUEST_CODE = id
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        notifyIntent.action = NOTIFICATION_INTENT_ACTION
        notifyIntent.putExtra(ID_TASK, id)

        val notifyPendingIntent = PendingIntent.getBroadcast(
            application,
            REQUEST_CODE.toInt(),
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            notifyPendingIntent
        )

    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions())
            requestPermissions()
        else startService()
    }

    private fun startService() {
        Intent(
            this@RunTaskActivity,
            LocationService::class.java
        ).also { service ->
            service.action = LocationService.ACTION_START_FOREGROUND_SERVICE
            startService(service)
        }
    }

    private fun dialogCheckPermissions() {
        if (checkPermissions()) {
            if (!LocationUtils.checkGPSEnable(this)) {
                LocationUtils.soliciteGpsActived(this)
            }
        } else {
            requestPermissions()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return EasyPermissions.hasPermissions(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun checkPermissions(): Boolean {
        return EasyPermissions.hasPermissions(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && EasyPermissions.hasPermissions(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) && EasyPermissions.hasPermissions(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ))
    }

    private fun requestPermissions() {
        EasyPermissions.requestPermissions(
            this,
            TAG,
            LOCATION_PERMISSION_REQUEST,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        EasyPermissions.requestPermissions(
            this,
            TAG,
            LOCATION_PERMISSION_REQUEST,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        EasyPermissions.requestPermissions(
            this,
            TAG,
            LOCATION_PERMISSION_REQUEST,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun createChannel(channelId: String, channelName: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Notificação"

            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Log.d(TAG, "onRationaleAccepted:" + requestCode)
    }

    override fun onRationaleDenied(requestCode: Int) {
        Log.d(TAG, "onRationaleDenied:" + requestCode)
    }

    override fun onClick(v: View?) {
        if (!LocationUtils.checkGPSEnable(this)) {
            LocationUtils.soliciteGpsActived(this)
        }
    }
}