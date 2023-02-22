package ufc.insight.ractivity.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Environment
import androidx.core.content.ContextCompat
import com.opencsv.CSVWriter
import kotlinx.coroutines.*
import ufc.insight.ractivity.database.TaskDatabase
import ufc.insight.ractivity.model.TaskModel
import ufc.insight.ractivity.util.*
import ufc.insight.ractivity.util.Constants.NOTIFICATION_INTENT_ACTION
import java.io.BufferedWriter
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

        if (intent.action == NOTIFICATION_INTENT_ACTION && isNetworkAvailable(context)) {

            taskId = intent.extras?.getLong(Constants.ID_TASK)

            GlobalScope.launch(Dispatchers.IO) {
                if (taskId != null) {
                    task = db.taskDao().findById(taskId!!)

                    val diff = (Date(task!!.timeEnd).time - Date(task!!.timeStart).time) / 5000

                    if (isNetworkAvailable(context)) {
                        prefs = Preferences.getPrefs(context)

                        sendNotification(context, task!!.name)

                        db.taskDao().scheduled(taskId!!)

                        CoroutineScope(Dispatchers.IO).launch {
                            withTimeout(timeMillis = diff * 5000) {
                            repeat(diff.toInt()) {
                                writeCSV(task!!.id)
                                delay(5000)
                            }
                            }
                            db.taskDao().finish(taskId!!)
                        }

                    }
                } else {
                    sendNotification(context,"A tarefa agendada foi excluida!")
                }
            }
        }

    }

    fun sendNotification(context: Context, nameTask: String) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        CoroutineScope(Dispatchers.IO).launch {
            notificationManager.sendNotification(
                "Iniciando tarefa " + nameTask,
                context
            )
        }
    }

    fun writeCSV(id: Long?) {
        val lastAltitude = prefs.getString(Constants.ALTITUDE, null)
        val lastLatitude = prefs.getString(Constants.LATITUDE, null)
        val lastLongitude = prefs.getString(Constants.LONGITUDE, null)

        if (lastAltitude == null || lastLatitude == null || lastLongitude == null) {
            LocationUtils.getLocation(prefs)
        } else {
            val csv =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + id + ".csv"

            writer = CSVWriter(BufferedWriter(FileWriter(csv, true)))
            val dados: Array<String> =
                arrayOf(
                    lastAltitude.toString(),
                    lastLatitude.toString(),
                    lastLongitude.toString(),
                    TimeUtils.updateTimeSeconds(System.currentTimeMillis()),
                    task!!.name
                )
            writer!!.writeNext(dados)
            writer!!.flush()
        }


    }
}