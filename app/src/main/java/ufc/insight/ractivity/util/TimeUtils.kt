package ufc.insight.ractivity.util

import java.text.SimpleDateFormat
import java.util.*

class TimeUtils {
    companion object {
        fun updateTime(time: Long?): String {
            val sdf = SimpleDateFormat("HH:mm")
            return sdf.format(Date(time!!))
        }

        fun updateTimeSeconds(time: Long?): String {
            val sdf = SimpleDateFormat("HH:mm:ss")
            return sdf.format(Date(time!!))
        }

        fun getHour(time: Long?): String {
            val sdf = SimpleDateFormat("HH")
            return sdf.format(Date(time!!))
        }

        fun getMinute(time: Long?): String {
            val sdf = SimpleDateFormat("mm")
            return sdf.format(Date(time!!))
        }
    }
}