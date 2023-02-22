package ufc.insight.ractivity.ui

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ufc.insight.ractivity.R
import ufc.insight.ractivity.data.database.TaskDatabase
import ufc.insight.ractivity.databinding.ActivityAddTaskBinding
import ufc.insight.ractivity.data.model.TaskModel
import ufc.insight.ractivity.util.ToastUtils
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class AddTaskActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var myCalendar: Calendar
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener
    private lateinit var saveBtn: Button
    private lateinit var timeInitEdt: TextInputEditText
    private lateinit var timeEndEdt: TextInputEditText
    private lateinit var title: TextInputEditText

    private var initHour: Int = 0
    private var initMinuteOfHour: Int = 0
    private var endHour: Int = 0
    private var endMinuteOfHour: Int = 0

    private var initTime = 0L
    private var endTime = 0L

    val db by lazy {
        TaskDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saveBtn = binding.saveBtn
        timeInitEdt = binding.timeInitEdt
        timeEndEdt = binding.timeEndEdt
        title = binding.nameAtividade

        timeInitEdt.setOnClickListener(this)
        timeEndEdt.setOnClickListener(this)
        saveBtn.setOnClickListener(this)

    }

    private fun saveAtividade() {
        val title = title.text.toString()

        if (title.isEmpty() || initTime == 0L || endTime == 0L) {
            ToastUtils.showToast(this, "Preencha os campos por favor!")
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    return@withContext db.taskDao().insert(
                        TaskModel(name = title, timeStart = initTime, timeEnd = endTime)
                    )
                }
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTimeListener(type: Int) {
        myCalendar = Calendar.getInstance()

        timeSetListener =
            TimePickerDialog.OnTimeSetListener() { _: TimePicker, hourOfDay: Int, min: Int ->
                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                myCalendar.set(Calendar.MINUTE, min)
                if (type == 1) {
                    initHour = hourOfDay
                    initMinuteOfHour = min
                } else {
                    endHour = hourOfDay
                    endMinuteOfHour = min
                }
                updateTime(type)

            }

        val timePickerDialog = TimePickerDialog(
            this, timeSetListener, myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE), true
        )
        timePickerDialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTime(type: Int) {
        val sdf = SimpleDateFormat("HH:mm")
        when (type) {
            0 -> {
                initTime = myCalendar.time.time
                timeInitEdt.setText(sdf.format(myCalendar.time))
            }
            1 -> {
                if (initTime != 0L) {
                    if (LocalTime.of(initHour, initMinuteOfHour)
                            .isAfter(LocalTime.of(endHour, endMinuteOfHour))
                    ) {
                        endTime = myCalendar.time.time
                        timeEndEdt.setText(sdf.format(myCalendar.time))
                    } else {
                        ToastUtils.showToast(this, "Selecione um horário após o horario inicial")
                    }
                } else {
                    ToastUtils.showToast(this, "Selecione primeiramente o horario inicial")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.timeInitEdt -> {
                    setTimeListener(0)
                }
                R.id.timeEndEdt -> {
                    setTimeListener(1)
                }
                R.id.saveBtn -> {
                    saveAtividade()
                }
            }
        }
    }

}