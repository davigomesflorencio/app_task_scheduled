package ufc.insight.ractivity.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.opencsv.CSVWriter
import kotlinx.coroutines.*
import ufc.insight.ractivity.data.database.TaskDatabase
import ufc.insight.ractivity.data.model.TaskModel
import ufc.insight.ractivity.service.LocationService
import ufc.insight.ractivity.util.*
import ufc.insight.ractivity.util.Constants.NOTIFICATION_INTENT_ACTION
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    private var task: TaskModel? = null
    private var taskId: Long? = null
    private lateinit var prefs: SharedPreferences
    private var writer: CSVWriter? = null

    override fun onReceive(context: Context, intent: Intent) {

        val db by lazy {
            TaskDatabase.getDatabase(context)
        }

        if (intent.action == NOTIFICATION_INTENT_ACTION && NetworkUtils.checkConnectivity(context)) {

            taskId = intent.extras?.getLong(Constants.ID_TASK)

            prefs = Preferences.getPrefs(context)

            CoroutineScope(Dispatchers.IO).launch {
                if (taskId != null) {
                    task = db.taskDao().findById(taskId!!)

                    if (task != null) {
                        val diff = (Date(task!!.timeEnd).time - Date(task!!.timeStart).time) / 5000

                        sendNotification(context, "Iniciando tarefa " + task!!.name)

                        db.taskDao().scheduled(taskId!!)

                        withTimeout(timeMillis = diff * 5000) {
                            repeat(diff.toInt()) {
                                writeCSV(task!!.id)
                                delay(5000)
                            }
                        }

                        db.taskDao().finishScheduled(taskId!!)
                        db.taskDao().finish(taskId!!)

                        sendNotification(context, "Tarefa " + task!!.name + " finalizada!")

                    } else {
                        sendNotification(context, "Dados da tarefa agendada não foram encontrados!")
                    }
                } else {
                    sendNotification(context, "Dados da tarefa agendada não foram encontrados!")
                }
            }
        }
    }

    fun sendNotification(context: Context, messageBody: String) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        CoroutineScope(Dispatchers.IO).launch {
            notificationManager.sendNotification(
                messageBody,
                context
            )
        }
    }

    fun writeCSV(id: Long?) {
        val lastAltitude = prefs.getString(Constants.ALTITUDE, null)
        val lastLatitude = prefs.getString(Constants.LATITUDE, null)
        val lastLongitude = prefs.getString(Constants.LONGITUDE, null)

        if (lastAltitude == null || lastLatitude == null || lastLongitude == null) {
            Log.d("LocationTracker", "prefs not found")
        } else {
            val csv =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + id + ".csv"
            var dados: Array<String>
            if (!File(csv).exists()) {
                dados =
                    arrayOf(
                        "Altitude", "Latitude", "Longitude", "Tempo", "Tarefa"
                    )
            } else {
                dados =
                    arrayOf(
                        lastAltitude.toString(),
                        lastLatitude.toString(),
                        lastLongitude.toString(),
                        TimeUtils.updateTimeSeconds(System.currentTimeMillis()),
                        task!!.name
                    )
            }
            writer = CSVWriter(BufferedWriter(FileWriter(csv, true)))
            writer!!.writeNext(dados)
            writer!!.flush()
        }
    }
}